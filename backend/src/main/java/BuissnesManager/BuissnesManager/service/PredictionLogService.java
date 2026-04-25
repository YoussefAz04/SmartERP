package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.dto.PredictionAccuracyDto;
import BuissnesManager.BuissnesManager.dto.PredictionLogDto;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.PredictionLog;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.PredictionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Stores every AI forecast issued for an enterprise and reconciles each
 * prediction against the real CA once the target month is over.
 *
 * <p>The goal is "scientific honesty": we cannot judge an AI model's value by
 * its in-sample fit alone. Logging predictions before reality is observed and
 * comparing them later produces a true backtest record (MAE, MAPE, sMAPE,
 * bias, interval coverage) that can be surfaced in the dashboard.
 *
 * <p>Idempotency: {@link #recordForecast(Long, ForecastDto)} can be safely
 * called many times per day — there is one row per
 * (entreprise, predictionMonth, targetMonth, horizon) thanks to a unique
 * constraint at the entity level. The first call within a calendar month
 * inserts; subsequent calls update the predicted/lower/upper bounds in case
 * the model is retrained.
 */
@Service
public class PredictionLogService {

    private static final Logger log = LoggerFactory.getLogger(PredictionLogService.class);

    /** Tiny epsilon used when computing percentage errors against tiny actuals,
     * so a near-zero CA doesn't blow up the MAPE summary. */
    private static final double EPS = 1.0;

    @Autowired
    private PredictionLogRepository predictionLogRepository;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    /**
     * Persists the future-horizon points from a {@link ForecastDto}. Existing
     * rows for the same (entreprise, predictionMonth, targetMonth, horizon)
     * are updated in place rather than duplicated.
     */
    @Transactional
    public void recordForecast(Long entrepriseId, ForecastDto dto) {
        if (entrepriseId == null || dto == null || dto.getForecastPoints() == null) {
            return;
        }
        Optional<Entreprise> entrepriseOpt = entrepriseRepo.findById(entrepriseId);
        if (entrepriseOpt.isEmpty()) {
            return;
        }
        Entreprise entreprise = entrepriseOpt.get();

        LocalDate today = LocalDate.now();
        LocalDate predictionMonth = today.withDayOfMonth(1);

        int horizon = 0;
        for (ForecastDto.ForecastPointDto p : dto.getForecastPoints()) {
            // Only future points are predictions; historical points have a non-null actual.
            if (p.getActual() != null || p.getForecast() == null) {
                continue;
            }
            horizon++;
            LocalDate targetMonth = predictionMonth.plusMonths(horizon);

            try {
                PredictionLog row = predictionLogRepository
                    .findByEntrepriseIdAndPredictionMonthAndTargetMonthAndHorizonMonths(
                        entrepriseId, predictionMonth, targetMonth, horizon)
                    .orElseGet(PredictionLog::new);

                if (row.getId() == null) {
                    row.setEntreprise(entreprise);
                    row.setPredictionDate(today);
                    row.setPredictionMonth(predictionMonth);
                    row.setTargetMonth(targetMonth);
                    row.setHorizonMonths(horizon);
                }
                row.setPredictedValue(p.getForecast());
                row.setLowerBound(p.getLowerBound());
                row.setUpperBound(p.getUpperBound());
                row.setConfidenceScore(dto.getConfidenceScore());
                row.setMethodology(truncate(dto.getMethodology(), 512));
                row.setModelName(truncate(dto.getSelectedModel(), 128));

                // Don't overwrite a settled actual if reality already arrived
                // and reconciliation ran in between. (Should be rare since we
                // only store future horizons, but defensive.)
                predictionLogRepository.save(row);
            } catch (RuntimeException ex) {
                // Logging the prediction must never break the forecast endpoint.
                log.warn("Failed to log prediction (entreprise={}, target={}, horizon={}): {}",
                        entrepriseId, targetMonth, horizon, ex.toString());
            }
        }
    }

    /**
     * Fills in {@code actualValue} (and derived error metrics) for every
     * unsettled prediction whose target month is fully in the past for the
     * given enterprise.
     *
     * @return number of predictions newly settled.
     */
    @Transactional
    public int reconcile(Long entrepriseId) {
        LocalDate today = LocalDate.now();
        // A target month is reconcilable once the month after it has started:
        // i.e. its last day is strictly before "today".
        LocalDate cutoff = today.withDayOfMonth(1).minusMonths(1);
        List<PredictionLog> pending = predictionLogRepository
                .findPendingReconciliation(entrepriseId, cutoff);
        if (pending.isEmpty()) {
            return 0;
        }
        return reconcileRows(pending);
    }

    @Transactional
    public int reconcileAll() {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.withDayOfMonth(1).minusMonths(1);
        List<PredictionLog> pending = predictionLogRepository.findAllPendingReconciliation(cutoff);
        if (pending.isEmpty()) {
            return 0;
        }
        return reconcileRows(pending);
    }

    private int reconcileRows(List<PredictionLog> pending) {
        // Cache real-CA monthly arrays per (entreprise, year) to avoid hammering DB.
        Map<String, double[]> cache = new HashMap<>();
        int settled = 0;

        for (PredictionLog row : pending) {
            if (row.getEntreprise() == null || row.getTargetMonth() == null) {
                continue;
            }
            Long eid = row.getEntreprise().getId();
            int year = row.getTargetMonth().getYear();
            int month = row.getTargetMonth().getMonthValue();
            String key = eid + "-" + year;
            double[] monthly = cache.computeIfAbsent(key, k -> loadMonthly(eid, year));
            double actual = monthly[month - 1];
            applyActual(row, actual);
            try {
                predictionLogRepository.save(row);
                settled++;
            } catch (RuntimeException ex) {
                log.warn("Failed to settle prediction id={}: {}", row.getId(), ex.toString());
            }
        }
        return settled;
    }

    private double[] loadMonthly(Long entrepriseId, int year) {
        double[] arr = new double[12];
        List<Object[]> rows = commandeVenteRepository.getMonthlySales(entrepriseId, year);
        if (rows != null) {
            for (Object[] r : rows) {
                if (r == null || r.length < 2 || r[0] == null || r[1] == null) continue;
                int m = ((Number) r[0]).intValue();
                double v = ((Number) r[1]).doubleValue();
                if (m >= 1 && m <= 12) arr[m - 1] = v;
            }
        }
        return arr;
    }

    private void applyActual(PredictionLog row, double actual) {
        row.setActualValue(round2(actual));
        double predicted = row.getPredictedValue() == null ? 0.0 : row.getPredictedValue();
        double absErr = Math.abs(predicted - actual);
        row.setAbsoluteError(round2(absErr));
        double pe = absErr / Math.max(Math.abs(actual), EPS);
        row.setPercentError(round4(pe));
        Boolean within = null;
        if (row.getLowerBound() != null && row.getUpperBound() != null) {
            within = actual >= row.getLowerBound() && actual <= row.getUpperBound();
        }
        row.setWithinInterval(within);
        row.setSettledAt(LocalDateTime.now());
    }

    /**
     * Returns the prediction history + accuracy summary for an enterprise.
     * Auto-runs reconciliation first so the summary always reflects whatever
     * months have closed.
     */
    @Transactional
    public PredictionAccuracyDto getHistory(Long entrepriseId) {
        reconcile(entrepriseId);

        List<PredictionLog> rows = predictionLogRepository
                .findByEntrepriseIdOrderByTargetMonthDescHorizonMonthsAsc(entrepriseId);

        List<PredictionLogDto> history = new ArrayList<>(rows.size());
        for (PredictionLog row : rows) {
            history.add(toDto(row));
        }

        PredictionAccuracyDto out = new PredictionAccuracyDto();
        out.setHistory(history);
        out.setTotalPredictions(rows.size());

        int settled = 0;
        double sumAbs = 0.0;
        double sumPct = 0.0;
        double sumSmape = 0.0;
        double sumSigned = 0.0;
        int withinCount = 0;
        int withinDenom = 0;
        for (PredictionLog row : rows) {
            if (row.getActualValue() == null) continue;
            settled++;
            double actual = row.getActualValue();
            double predicted = row.getPredictedValue() == null ? 0.0 : row.getPredictedValue();
            double absErr = Math.abs(predicted - actual);
            sumAbs += absErr;
            sumPct += absErr / Math.max(Math.abs(actual), EPS);
            double denomS = (Math.abs(actual) + Math.abs(predicted)) / 2.0;
            sumSmape += denomS == 0.0 ? 0.0 : absErr / denomS;
            sumSigned += predicted - actual;
            if (row.getWithinInterval() != null) {
                withinDenom++;
                if (Boolean.TRUE.equals(row.getWithinInterval())) withinCount++;
            }
        }
        out.setSettledPredictions(settled);
        out.setPendingPredictions(rows.size() - settled);
        if (settled > 0) {
            out.setMae(round2(sumAbs / settled));
            out.setMape(round4(sumPct / settled));
            out.setSmape(round4(sumSmape / settled));
            out.setBias(round2(sumSigned / settled));
        }
        if (withinDenom > 0) {
            out.setIntervalCoverage(round4((double) withinCount / withinDenom));
        }
        return out;
    }

    /**
     * Reconciles every enterprise's pending predictions on the 2nd day of each
     * month at 02:00. We wait one day past the month boundary so that any
     * commandes posted late on the last day still land before reconciliation.
     */
    @Scheduled(cron = "0 0 2 2 * *")
    @Transactional
    public void scheduledReconcileAll() {
        try {
            int settled = reconcileAll();
            if (settled > 0) {
                log.info("Scheduled prediction reconciliation: {} predictions settled.", settled);
            }
        } catch (RuntimeException ex) {
            log.warn("Scheduled prediction reconciliation failed: {}", ex.toString());
        }
    }

    /* ----------------------------- helpers ----------------------------- */

    private PredictionLogDto toDto(PredictionLog row) {
        PredictionLogDto dto = new PredictionLogDto();
        dto.setId(row.getId());
        dto.setPredictionDate(row.getPredictionDate());
        dto.setPredictionMonth(row.getPredictionMonth());
        dto.setTargetMonth(row.getTargetMonth());
        if (row.getTargetMonth() != null) {
            YearMonth ym = YearMonth.from(row.getTargetMonth());
            String monthLabel = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            dto.setTargetMonthLabel(monthLabel + " " + ym.getYear());
        }
        dto.setHorizonMonths(row.getHorizonMonths());
        dto.setPredictedValue(row.getPredictedValue());
        dto.setLowerBound(row.getLowerBound());
        dto.setUpperBound(row.getUpperBound());
        dto.setConfidenceScore(row.getConfidenceScore());
        dto.setMethodology(row.getMethodology());
        dto.setModelName(row.getModelName());
        dto.setActualValue(row.getActualValue());
        dto.setAbsoluteError(row.getAbsoluteError());
        dto.setPercentError(row.getPercentError());
        dto.setWithinInterval(row.getWithinInterval());
        dto.setStatus(row.getActualValue() == null ? "PENDING" : "SETTLED");
        dto.setSettledAt(row.getSettledAt());
        dto.setCreatedAt(row.getCreatedAt());
        return dto;
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static Double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static Double round4(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
