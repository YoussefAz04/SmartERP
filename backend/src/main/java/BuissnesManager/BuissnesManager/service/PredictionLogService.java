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
import java.util.TreeMap;

















@Service
public class PredictionLogService {

    private static final Logger log = LoggerFactory.getLogger(PredictionLogService.class);

    

    private static final double EPS = 1.0;

    @Autowired
    private PredictionLogRepository predictionLogRepository;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    




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

                
                
                if (dto.getModelMetrics() != null) {
                    var mm = dto.getModelMetrics();
                    row.setChallengerModel(truncate(mm.getChallengerModel(), 128));
                    row.setCvMase(mm.getCvMase());
                    row.setCvSmape(mm.getCvSmape());
                    row.setBoxCoxLambda(mm.getBoxCoxLambda());
                    row.setDataPointsUsed(mm.getSamples());
                    if (mm.getFeatures() != null) {
                        row.setFeatureSpec(truncate(mm.getFeatures() <= 4 ? "SMALL" : "WIDE+CAL", 64));
                    }
                }
                if (dto.getAnomalies() != null) {
                    row.setOutliersDetected(dto.getAnomalies().size());
                }

                
                
                
                predictionLogRepository.save(row);
            } catch (RuntimeException ex) {
                
                log.warn("Failed to log prediction (entreprise={}, target={}, horizon={}): {}",
                        entrepriseId, targetMonth, horizon, ex.toString());
            }
        }
    }

    






    @Transactional
    public int reconcile(Long entrepriseId) {
        LocalDate today = LocalDate.now();
        
        
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

    









    @Transactional
    public PredictionAccuracyDto getHistory(Long entrepriseId) {
        reconcile(entrepriseId);

        List<PredictionLog> rows = predictionLogRepository
                .findByEntrepriseIdOrderByTargetMonthDescHorizonMonthsAsc(entrepriseId);

        if (rows.isEmpty()) {
            try {
                int created = backfillFromCommandeVente(entrepriseId);
                if (created > 0) {
                    rows = predictionLogRepository
                            .findByEntrepriseIdOrderByTargetMonthDescHorizonMonthsAsc(entrepriseId);
                }
            } catch (RuntimeException ex) {
                
                
                log.warn("Prediction backfill failed for entreprise {}: {}",
                        entrepriseId, ex.toString());
            }
        }

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

    

    
    private static final int BACKFILL_MIN_PRIOR_MONTHS = 3;
    
    private static final int BACKFILL_LOOKBACK_MONTHS = 24;
    
    private static final double[] BACKFILL_CONFIDENCE = { 0.85, 0.78, 0.70 };
    
    private static final double BACKFILL_INTERVAL_Z = 1.28;
    private static final String BACKFILL_METHODOLOGY =
            "Backtest rétrospectif (moyenne mobile 3 mois + tendance, ajustement saisonnier annuel " +
            "lorsque disponible). Généré automatiquement à partir du CA réel quand l'historique " +
            "des prédictions est vide.";
    private static final String BACKFILL_MODEL_NAME = "Auto-backfill v1";

    










    @Transactional
    public int backfillFromCommandeVente(Long entrepriseId) {
        if (entrepriseId == null) return 0;
        Optional<Entreprise> entrepriseOpt = entrepriseRepo.findById(entrepriseId);
        if (entrepriseOpt.isEmpty()) return 0;
        Entreprise entreprise = entrepriseOpt.get();

        
        
        LocalDate today = LocalDate.now();
        YearMonth firstMonth = YearMonth.from(today).minusMonths(BACKFILL_LOOKBACK_MONTHS);
        YearMonth lastMonth = YearMonth.from(today).minusMonths(1);

        TreeMap<YearMonth, Double> series = new TreeMap<>();
        Map<Integer, double[]> yearCache = new HashMap<>();
        for (YearMonth ym = firstMonth; !ym.isAfter(lastMonth); ym = ym.plusMonths(1)) {
            double[] year = yearCache.computeIfAbsent(ym.getYear(),
                    y -> loadMonthly(entrepriseId, y));
            series.put(ym, year[ym.getMonthValue() - 1]);
        }

        
        
        long nonZero = series.values().stream().filter(v -> v > 0).count();
        if (nonZero < BACKFILL_MIN_PRIOR_MONTHS + 1) {
            return 0;
        }

        List<YearMonth> orderedMonths = new ArrayList<>(series.keySet());
        int created = 0;

        
        
        
        for (int i = BACKFILL_MIN_PRIOR_MONTHS; i < orderedMonths.size(); i++) {
            YearMonth predictionYm = orderedMonths.get(i);
            
            double a3 = series.get(orderedMonths.get(i - 3));
            double a2 = series.get(orderedMonths.get(i - 2));
            double a1 = series.get(orderedMonths.get(i - 1));
            
            
            
            if (a1 + a2 + a3 <= 0.0) {
                continue;
            }
            double baseline = (a1 + a2 + a3) / 3.0;
            double trend = (a1 - a3) / 2.0;
            double residualStd = stdDev(new double[] { a3, a2, a1 }, baseline);

            for (int h = 1; h <= 3; h++) {
                int targetIdx = i + h;
                if (targetIdx >= orderedMonths.size()) break;
                YearMonth targetYm = orderedMonths.get(targetIdx);

                
                double seasonal = 1.0;
                YearMonth prevYearYm = targetYm.minusYears(1);
                Double prevYearValue = series.get(prevYearYm);
                if (prevYearValue != null && prevYearValue > 0 && baseline > 0) {
                    double yoyBaseline = avgPriorYearWindow(series, prevYearYm);
                    if (yoyBaseline > 0) {
                        seasonal = clamp(prevYearValue / yoyBaseline, 0.6, 1.5);
                    }
                }

                double rawPredicted = (baseline + h * trend) * seasonal;
                double predicted = Math.max(0.0, rawPredicted);
                
                double width = BACKFILL_INTERVAL_Z * residualStd * Math.sqrt(h);
                double lower = Math.max(0.0, predicted - width);
                double upper = Math.max(predicted, predicted + width);

                LocalDate predictionMonth = predictionYm.atDay(1);
                LocalDate targetMonth = targetYm.atDay(1);

                Optional<PredictionLog> existing = predictionLogRepository
                        .findByEntrepriseIdAndPredictionMonthAndTargetMonthAndHorizonMonths(
                                entrepriseId, predictionMonth, targetMonth, h);
                if (existing.isPresent()) {
                    continue;
                }

                PredictionLog row = new PredictionLog();
                row.setEntreprise(entreprise);
                
                
                row.setPredictionDate(predictionMonth.plusDays(1));
                row.setPredictionMonth(predictionMonth);
                row.setTargetMonth(targetMonth);
                row.setHorizonMonths(h);
                row.setPredictedValue(round2(predicted));
                row.setLowerBound(round2(lower));
                row.setUpperBound(round2(upper));
                row.setConfidenceScore(BACKFILL_CONFIDENCE[h - 1]);
                row.setMethodology(truncate(BACKFILL_METHODOLOGY, 512));
                row.setModelName(truncate(BACKFILL_MODEL_NAME, 128));

                Double actual = series.get(targetYm);
                if (actual != null) {
                    applyActual(row, actual);
                }

                try {
                    predictionLogRepository.save(row);
                    created++;
                } catch (RuntimeException ex) {
                    log.warn("Failed to backfill prediction (entreprise={}, target={}, horizon={}): {}",
                            entrepriseId, targetMonth, h, ex.toString());
                }
            }
        }
        if (created > 0) {
            log.info("Backfilled {} synthetic prediction rows for entreprise {}", created, entrepriseId);
        }
        return created;
    }

    
    private static double avgPriorYearWindow(TreeMap<YearMonth, Double> series, YearMonth ym) {
        double sum = 0.0;
        int n = 0;
        for (int delta = -1; delta <= 1; delta++) {
            Double v = series.get(ym.plusMonths(delta));
            if (v != null) { sum += v; n++; }
        }
        return n == 0 ? 0.0 : sum / n;
    }

    private static double stdDev(double[] values, double mean) {
        if (values.length < 2) return 0.0;
        double s = 0.0;
        for (double v : values) {
            double d = v - mean;
            s += d * d;
        }
        return Math.sqrt(s / values.length);
    }

    private static double clamp(double v, double lo, double hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    

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
