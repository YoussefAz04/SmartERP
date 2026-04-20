package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales forecasting service.
 *
 * <p>The original implementation used Single Exponential Smoothing (SES) with
 * ad-hoc seasonal scaling of the last smoothed level. That under-reacted to
 * genuine growth, over-smoothed short noisy series, and produced flat forecasts
 * whenever a business had only partial current-year data.
 *
 * <p>This revision keeps the same DTO contract but upgrades the algorithm:
 *
 * <ul>
 *   <li><b>Holt's linear method</b> (double exponential smoothing) when there is
 *       no reliable prior-year history: tracks both a level and a trend so that
 *       steady growth is projected forward instead of repeated as a flat line.
 *   <li><b>Holt–Winters-lite</b> with multiplicative seasonal indices derived
 *       from the previous year when available: the level+trend model is scaled
 *       month-by-month by the seasonal factor instead of naively multiplying
 *       the last level by <code>(1 + seasonality)</code>.
 *   <li><b>Residual-based prediction intervals</b>: historical bounds are the
 *       smoothed value ± one in-sample residual standard deviation (clamped to
 *       non-negative), and future bounds widen with the square-root of the
 *       forecast horizon — a standard, statistically-sound approach.
 *   <li><b>SMAPE-based confidence</b>: symmetric MAPE avoids the asymmetric
 *       penalty of classic MAPE against near-zero actuals, and is blended with
 *       a data-density term normalised to a full expected year(s).
 * </ul>
 *
 * <p>Every downstream consumer (AiDashboardService, the Angular dashboard)
 * continues to receive the same {@link ForecastDto} shape.
 */
@Service
public class ForecastService {

    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    private static final String[] MONTH_NAMES_FR = {
        "Jan", "Fév", "Mars", "Avr", "Mai", "Juin",
        "Juil", "Août", "Sept", "Oct", "Nov", "Déc"
    };

    // Holt's linear smoothing parameters. Level smoothing reacts moderately,
    // trend smoothing is lower so the trend doesn't chase single-month spikes.
    static final double ALPHA = 0.35; // level smoothing
    static final double BETA  = 0.15; // trend smoothing

    // Hard clamps on multiplicative seasonal indices so a single outlier month
    // in the previous year can't create wildly implausible forecasts.
    private static final double MIN_SEASONAL_INDEX = 0.60; // -40%
    private static final double MAX_SEASONAL_INDEX = 1.50; // +50%

    // Hard clamp on per-horizon growth when projecting trend without prior year
    // history, to avoid absurd extrapolations on very short series.
    private static final double MAX_TREND_GROWTH_PER_MONTH = 0.50; // 50%/month

    public ForecastDto generateForecast(Long entrepriseId) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        List<Object[]> currentYearData = commandeVenteRepository.getMonthlySales(entrepriseId, currentYear);
        List<Object[]> prevYearData = commandeVenteRepository.getMonthlySales(entrepriseId, currentYear - 1);

        double[] currentValues = buildMonthArray(currentYearData);
        double[] prevValues = buildMonthArray(prevYearData);
        int observedCurrentMonths = Math.min(currentMonth, 12);

        boolean hasPrevYear = false;
        for (double v : prevValues) {
            if (v > 0) { hasPrevYear = true; break; }
        }

        // Build the historical series: previous full year (if any) + current year
        // up to the current month. Skipping all-zero prior-year months avoids
        // biasing Holt's level toward zero.
        List<Double> historicalSeries = new ArrayList<>();
        if (hasPrevYear) {
            for (double v : prevValues) historicalSeries.add(v);
        }
        for (int i = 0; i < observedCurrentMonths; i++) {
            historicalSeries.add(currentValues[i]);
        }

        // Multiplicative seasonal indices (12 values) computed from prior-year
        // months. When there is no prior year, every index is 1.0 (neutral).
        double[] seasonalIndices = computeSeasonalIndices(prevValues, hasPrevYear);

        // Fit Holt's linear model on the series with the seasonal component
        // stripped out. This is a pragmatic Holt–Winters variant that doesn't
        // require two full seasonal cycles before producing usable forecasts.
        HoltResult holt = fitHolt(historicalSeries, seasonalIndices, hasPrevYear);

        List<ForecastDto.ForecastPointDto> forecastPoints = new ArrayList<>();

        // Historical points (current year only, up to current month). The fitted
        // value for month m aligns with historicalSeries index (offset + m - 1).
        int historyOffset = hasPrevYear ? 12 : 0;
        double residualStd = computeResidualStd(historicalSeries, holt.fitted);
        for (int m = 1; m <= observedCurrentMonths; m++) {
            double actual = currentValues[m - 1];
            int fittedIdx = historyOffset + m - 1;
            double fitted = fittedIdx >= 0 && fittedIdx < holt.fitted.size()
                ? holt.fitted.get(fittedIdx) : actual;
            double upper = Math.max(0.0, fitted + residualStd);
            double lower = Math.max(0.0, fitted - residualStd);
            forecastPoints.add(new ForecastDto.ForecastPointDto(
                MONTH_NAMES_FR[m - 1], round2(actual), round2(fitted),
                round2(upper), round2(lower)
            ));
        }

        // Future 6 months: level + h*trend, scaled by the seasonal index of
        // the future month. Intervals widen with sqrt(h) in classic fashion.
        double level = holt.finalLevel;
        double trend = holt.finalTrend;
        // When projecting without prior year, clamp the implied monthly growth
        // so very short series don't produce absurd extrapolations.
        if (!hasPrevYear && level > 0) {
            double maxStep = level * MAX_TREND_GROWTH_PER_MONTH;
            if (trend >  maxStep) trend =  maxStep;
            if (trend < -maxStep) trend = -maxStep;
        }

        for (int h = 1; h <= 6; h++) {
            // currentMonth is 1..12; horizon h=1 is the month AFTER currentMonth.
            int futureMonthIdx = ((currentMonth - 1) + h) % 12; // 0-based
            double seasonal = seasonalIndices[futureMonthIdx];
            double baseline = level + h * trend;
            double forecastValue = Math.max(0.0, baseline * seasonal);

            double horizonStd = residualStd * Math.sqrt((double) h);
            double upper = Math.max(0.0, forecastValue + horizonStd);
            double lower = Math.max(0.0, forecastValue - horizonStd);

            forecastPoints.add(new ForecastDto.ForecastPointDto(
                MONTH_NAMES_FR[futureMonthIdx] + "+", null,
                round2(forecastValue), round2(upper), round2(lower)
            ));
        }

        // Summary predictions derived from the freshly-computed future points,
        // so they always stay in sync with what the chart displays.
        List<Double> futureForecasts = new ArrayList<>();
        for (ForecastDto.ForecastPointDto p : forecastPoints) {
            if (p.getActual() == null && p.getForecast() != null) {
                futureForecasts.add(p.getForecast());
            }
        }
        double predictedNextMonth = futureForecasts.isEmpty() ? 0.0 : futureForecasts.get(0);
        double predictedNext3Months = futureForecasts.stream().limit(3).mapToDouble(Double::doubleValue).sum();
        double predictedNext6Months = futureForecasts.stream().limit(6).mapToDouble(Double::doubleValue).sum();

        // Confidence: density of non-zero observations + SMAPE fit, capped at 95.
        long nonZeroMonths = historicalSeries.stream().filter(v -> v > 0).count();
        double expectedMonths = hasPrevYear ? 24.0 : 12.0;
        double density = Math.min(1.0, nonZeroMonths / expectedMonths);
        double fit = computeSmapeFit(historicalSeries, holt.fitted);
        double confidence = Math.min(95.0, Math.max(0.0, (0.5 * density + 0.5 * fit) * 100.0));

        // Seasonality indices normalised around 1.0 for display (using prev year
        // when available, otherwise the current year's own profile).
        List<ForecastDto.SeasonalityDto> seasonality =
            computeSeasonalityView(hasPrevYear ? prevValues : currentValues);

        ForecastDto dto = new ForecastDto();
        dto.setForecastPoints(forecastPoints);
        dto.setPredictedNextMonth(round2(predictedNextMonth));
        dto.setPredictedNext3Months(round2(predictedNext3Months));
        dto.setPredictedNext6Months(round2(predictedNext6Months));
        dto.setConfidenceScore(Math.round(confidence * 10.0) / 10.0);
        dto.setMethodology(buildMethodologyLabel(hasPrevYear, historicalSeries.size()));
        dto.setSeasonality(seasonality);

        if (log.isDebugEnabled()) {
            log.debug("Forecast for entreprise={}: series={} points, prevYear={}, " +
                    "finalLevel={}, finalTrend={}, residualStd={}, confidence={}%",
                    entrepriseId, historicalSeries.size(), hasPrevYear,
                    holt.finalLevel, holt.finalTrend, residualStd, confidence);
        }

        return dto;
    }

    /* --------------------------- core algorithms --------------------------- */

    /** Holds the Holt-model state after fitting. */
    private static final class HoltResult {
        final List<Double> fitted;   // one-step-ahead fits aligned with input
        final double finalLevel;     // level after last observation
        final double finalTrend;     // trend after last observation
        HoltResult(List<Double> fitted, double finalLevel, double finalTrend) {
            this.fitted = fitted;
            this.finalLevel = finalLevel;
            this.finalTrend = finalTrend;
        }
    }

    /**
     * Fits Holt's linear method on the seasonally-adjusted series. When prior-year
     * seasonal indices are available, the input is divided by the seasonal factor
     * before smoothing and re-multiplied when producing fitted values, so the
     * fitted series matches the original scale.
     */
    HoltResult fitHolt(List<Double> data, double[] seasonalIndices, boolean hasSeasonal) {
        List<Double> fitted = new ArrayList<>(data.size());
        if (data.isEmpty()) {
            return new HoltResult(fitted, 0.0, 0.0);
        }

        // Initialise level and trend. Level = first deseasonalised value;
        // trend = (second - first) if we have two observations, else 0.
        double firstSeasonal = hasSeasonal ? seasonalForIndex(0, seasonalIndices) : 1.0;
        double level = data.get(0) / safe(firstSeasonal);
        double trend = 0.0;
        if (data.size() >= 2) {
            double secondSeasonal = hasSeasonal ? seasonalForIndex(1, seasonalIndices) : 1.0;
            double second = data.get(1) / safe(secondSeasonal);
            trend = second - level;
        }
        // First fitted value = first actual (we have no forecast before seeing anything).
        fitted.add(data.get(0));

        for (int t = 1; t < data.size(); t++) {
            double seasonal = hasSeasonal ? seasonalForIndex(t, seasonalIndices) : 1.0;
            // One-step-ahead forecast before observing data[t]:
            double oneStep = Math.max(0.0, (level + trend) * seasonal);
            fitted.add(oneStep);
            double deseasonalised = data.get(t) / safe(seasonal);
            double prevLevel = level;
            level = ALPHA * deseasonalised + (1.0 - ALPHA) * (level + trend);
            trend = BETA  * (level - prevLevel) + (1.0 - BETA) * trend;
        }
        return new HoltResult(fitted, level, trend);
    }

    /**
     * Returns the seasonal index to apply to historical position {@code t} in the
     * fitted series. The series is built as
     * <code>[prevYear Jan..Dec][currentYear Jan..current]</code>, so the calendar
     * month is simply {@code t % 12} — both blocks start in January.
     */
    private double seasonalForIndex(int t, double[] seasonalIndices) {
        int monthIdx = t % 12;
        return safe(seasonalIndices[monthIdx]);
    }

    /** Multiplicative seasonal indices (12 values) from previous-year actuals. */
    double[] computeSeasonalIndices(double[] prevValues, boolean hasPrevYear) {
        double[] indices = new double[12];
        if (!hasPrevYear) {
            for (int i = 0; i < 12; i++) indices[i] = 1.0;
            return indices;
        }
        double avgPrev = computeAverageNonZero(prevValues);
        if (avgPrev <= 0) {
            for (int i = 0; i < 12; i++) indices[i] = 1.0;
            return indices;
        }
        for (int i = 0; i < 12; i++) {
            double raw = prevValues[i] > 0 ? prevValues[i] / avgPrev : 1.0;
            indices[i] = clamp(raw, MIN_SEASONAL_INDEX, MAX_SEASONAL_INDEX);
        }
        return indices;
    }

    /** Standard deviation of one-step-ahead residuals on observed (non-zero) months. */
    double computeResidualStd(List<Double> actuals, List<Double> fitted) {
        if (actuals.isEmpty() || fitted.isEmpty()) return 0.0;
        int size = Math.min(actuals.size(), fitted.size());
        double sumSq = 0.0;
        int n = 0;
        // Skip index 0 since fitted[0] = actuals[0] by construction.
        for (int i = 1; i < size; i++) {
            double a = actuals.get(i);
            if (a <= 0) continue;
            double diff = a - fitted.get(i);
            sumSq += diff * diff;
            n++;
        }
        if (n == 0) return 0.0;
        return Math.sqrt(sumSq / n);
    }

    /**
     * Symmetric MAPE-based fit score in [0,1]: 1 − mean(|a-f| / ((|a|+|f|)/2)).
     * Symmetric variant is preferred so over- and under-forecasts are treated
     * evenly, and because classic MAPE explodes near a=0.
     */
    double computeSmapeFit(List<Double> actuals, List<Double> fitted) {
        if (actuals.isEmpty() || fitted.isEmpty()) return 0.0;
        int size = Math.min(actuals.size(), fitted.size());
        double totalErr = 0.0;
        int n = 0;
        for (int i = 1; i < size; i++) {
            double a = actuals.get(i);
            double f = fitted.get(i);
            if (a <= 0 && f <= 0) continue;
            double denom = (Math.abs(a) + Math.abs(f)) / 2.0;
            if (denom == 0) continue;
            totalErr += Math.min(1.0, Math.abs(a - f) / denom);
            n++;
        }
        if (n == 0) return 0.0;
        return Math.max(0.0, 1.0 - (totalErr / n));
    }

    /** 12-month display seasonality (index + HAUTE/NORMALE/BASSE label). */
    private List<ForecastDto.SeasonalityDto> computeSeasonalityView(double[] referenceValues) {
        List<ForecastDto.SeasonalityDto> result = new ArrayList<>();
        double avg = computeAverageNonZero(referenceValues);
        for (int m = 0; m < 12; m++) {
            double idx = avg > 0 && referenceValues[m] > 0 ? referenceValues[m] / avg : 1.0;
            String trend;
            if (idx > 1.1) trend = "HAUTE";
            else if (idx < 0.9) trend = "BASSE";
            else trend = "NORMALE";
            result.add(new ForecastDto.SeasonalityDto(
                MONTH_NAMES_FR[m],
                Math.round(idx * 100.0) / 100.0,
                trend));
        }
        return result;
    }

    private String buildMethodologyLabel(boolean hasPrevYear, int seriesSize) {
        if (hasPrevYear) {
            return String.format(
                "Holt-Winters (α=%.2f, β=%.2f) + Saisonnalité multiplicative N-1 (%d obs.)",
                ALPHA, BETA, seriesSize);
        }
        return String.format(
            "Holt linéaire (α=%.2f, β=%.2f) — pas d'historique N-1 (%d obs.)",
            ALPHA, BETA, seriesSize);
    }

    /* ------------------------------ utilities ------------------------------ */

    double[] buildMonthArray(List<Object[]> rawData) {
        double[] values = new double[12];
        if (rawData == null) return values;
        for (Object[] row : rawData) {
            if (row == null || row.length < 2 || row[0] == null) continue;
            int month = ((Number) row[0]).intValue();
            double amount = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            if (month >= 1 && month <= 12) {
                values[month - 1] = amount;
            }
        }
        return values;
    }

    private double computeAverageNonZero(double[] values) {
        double sum = 0.0;
        int count = 0;
        for (double v : values) {
            if (v > 0) { sum += v; count++; }
        }
        return count > 0 ? sum / count : 0.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Avoid divide-by-zero when a seasonal index is (pathologically) zero. */
    private static double safe(double factor) {
        return factor <= 0 ? 1.0 : factor;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
