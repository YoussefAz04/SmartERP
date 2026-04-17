package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.dto.MonthlySalesDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastService {

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    private static final String[] MONTH_NAMES_FR = {
        "Jan", "Fév", "Mars", "Avr", "Mai", "Juin",
        "Juil", "Août", "Sept", "Oct", "Nov", "Déc"
    };

    private static final double ALPHA = 0.3; // Exponential smoothing factor

    public ForecastDto generateForecast(Long entrepriseId) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // Get historical data for current and previous year
        List<Object[]> currentYearData = commandeVenteRepository.getMonthlySales(entrepriseId, currentYear);
        List<Object[]> prevYearData = commandeVenteRepository.getMonthlySales(entrepriseId, currentYear - 1);

        double[] currentValues = buildMonthArray(currentYearData);
        double[] prevValues = buildMonthArray(prevYearData);

        boolean hasPrevYear = false;
        for (double v : prevValues) if (v > 0) { hasPrevYear = true; break; }

        // Combine both years for historical series (skip the empty prev-year block when
        // there's no history yet — otherwise the 12 leading zeros bias exponential
        // smoothing toward zero and the forecast collapses to a flat line).
        List<Double> historicalSeries = new ArrayList<>();
        if (hasPrevYear) {
            for (double v : prevValues) historicalSeries.add(v);
        }
        for (double v : currentValues) historicalSeries.add(v);

        // Apply exponential smoothing to generate smoothed series
        List<Double> smoothed = exponentialSmoothing(historicalSeries, ALPHA);

        // Precompute a linear trend on the observed (non-zero tail of the) current year
        // so that when no prev-year data is available we can still project forward
        // instead of repeating the last smoothed value for six months.
        double[] trend = computeLinearTrend(currentValues, currentMonth);
        double trendSlope = trend[0];
        double trendIntercept = trend[1];
        int observedMonths = (int) trend[2];

        // Build forecast points (historical + future 6 months)
        List<ForecastDto.ForecastPointDto> forecastPoints = new ArrayList<>();

        // Historical points (current year only, up to current month)
        int historyOffset = hasPrevYear ? 12 : 0;
        for (int m = 1; m <= Math.min(currentMonth, 12); m++) {
            double actual = currentValues[m - 1];
            int smoothedIdx = historyOffset + m - 1;
            double smoothedVal = smoothedIdx >= 0 && smoothedIdx < smoothed.size()
                ? smoothed.get(smoothedIdx) : actual;
            forecastPoints.add(new ForecastDto.ForecastPointDto(
                MONTH_NAMES_FR[m - 1], actual, smoothedVal,
                smoothedVal * 1.15, smoothedVal * 0.85
            ));
        }

        // Future forecast (next 6 months)
        double lastSmoothed = smoothed.isEmpty() ? 0.0 : smoothed.get(smoothed.size() - 1);
        double stdDev = computeStdDev(historicalSeries);
        for (int i = 1; i <= 6; i++) {
            int futureMonth = (currentMonth + i - 1) % 12;
            double forecastValue;
            if (hasPrevYear) {
                // Seasonal scaling from previous year
                forecastValue = lastSmoothed * (1 + getSeasonalFactor(futureMonth, prevValues));
            } else if (observedMonths >= 2 && trendSlope != 0.0) {
                // No prior year – project the fitted trend line forward
                double x = currentMonth + i; // 1-indexed month position
                forecastValue = trendIntercept + trendSlope * x;
            } else {
                // Fallback: repeat the last smoothed value
                forecastValue = lastSmoothed;
            }
            forecastValue = Math.max(0, forecastValue);
            forecastPoints.add(new ForecastDto.ForecastPointDto(
                MONTH_NAMES_FR[futureMonth] + "+", null, forecastValue,
                forecastValue + stdDev, Math.max(0, forecastValue - stdDev)
            ));
        }

        // Compute seasonality indices (prefer prev year, fall back to current year)
        List<ForecastDto.SeasonalityDto> seasonality = computeSeasonality(
                hasPrevYear ? prevValues : currentValues, currentValues);

        // Summary forecasts
        double predictedNextMonth = forecastPoints.size() > currentMonth
            ? forecastPoints.get(currentMonth).getForecast() : lastSmoothed;
        double predictedNext3Months = forecastPoints.stream()
            .filter(p -> p.getActual() == null)
            .limit(3)
            .mapToDouble(p -> p.getForecast() != null ? p.getForecast() : 0.0)
            .sum();
        double predictedNext6Months = forecastPoints.stream()
            .filter(p -> p.getActual() == null)
            .mapToDouble(p -> p.getForecast() != null ? p.getForecast() : 0.0)
            .sum();

        // Confidence score: blend data-density with in-sample fit quality so a
        // steady series scores higher than an erratic one, capped at 95%.
        long nonZeroMonths = historicalSeries.stream().filter(v -> v > 0).count();
        double density = Math.min(1.0, nonZeroMonths / 12.0);
        double fit = computeInSampleFitScore(historicalSeries, smoothed);
        double confidence = Math.min(95.0, (0.6 * density + 0.4 * fit) * 100.0);

        ForecastDto dto = new ForecastDto();
        dto.setForecastPoints(forecastPoints);
        dto.setPredictedNextMonth(Math.round(predictedNextMonth * 100.0) / 100.0);
        dto.setPredictedNext3Months(Math.round(predictedNext3Months * 100.0) / 100.0);
        dto.setPredictedNext6Months(Math.round(predictedNext6Months * 100.0) / 100.0);
        dto.setConfidenceScore(Math.round(confidence * 10.0) / 10.0);
        String method = hasPrevYear
            ? "Lissage Exponentiel Simple (α=" + ALPHA + ") + Saisonnalité N-1"
            : "Lissage Exponentiel + Régression Linéaire (données N uniquement)";
        dto.setMethodology(method);
        dto.setSeasonality(seasonality);

        return dto;
    }

    /**
     * Ordinary-least-squares linear regression over months that have real observations.
     * Returns [slope, intercept, observedCount] with x = month index (1..currentMonth).
     */
    private double[] computeLinearTrend(double[] values, int currentMonth) {
        int n = 0;
        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0;
        int limit = Math.min(currentMonth, values.length);
        for (int m = 1; m <= limit; m++) {
            double y = values[m - 1];
            if (y <= 0) continue; // ignore empty months so the slope reflects active business
            n++;
            sumX += m;
            sumY += y;
            sumXX += (double) m * m;
            sumXY += m * y;
        }
        if (n < 2) return new double[]{0.0, 0.0, n};
        double denom = n * sumXX - sumX * sumX;
        if (denom == 0.0) return new double[]{0.0, sumY / n, n};
        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;
        return new double[]{slope, intercept, n};
    }

    /**
     * Normalised in-sample fit score in [0, 1] based on MAPE of the smoothed series
     * against the historical actuals (only non-zero observations count).
     */
    private double computeInSampleFitScore(List<Double> actuals, List<Double> fitted) {
        if (actuals.isEmpty() || fitted.isEmpty()) return 0.0;
        double totalErr = 0.0;
        int n = 0;
        int size = Math.min(actuals.size(), fitted.size());
        for (int i = 0; i < size; i++) {
            double a = actuals.get(i);
            if (a <= 0) continue;
            double f = fitted.get(i);
            totalErr += Math.min(1.0, Math.abs(a - f) / a);
            n++;
        }
        if (n == 0) return 0.0;
        double mape = totalErr / n;
        return Math.max(0.0, 1.0 - mape);
    }

    private double[] buildMonthArray(List<Object[]> rawData) {
        double[] values = new double[12];
        for (Object[] row : rawData) {
            int month = ((Number) row[0]).intValue();
            double amount = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            if (month >= 1 && month <= 12) {
                values[month - 1] = amount;
            }
        }
        return values;
    }

    private List<Double> exponentialSmoothing(List<Double> data, double alpha) {
        List<Double> result = new ArrayList<>();
        if (data.isEmpty()) return result;

        double s = data.get(0);
        result.add(s);
        for (int i = 1; i < data.size(); i++) {
            s = alpha * data.get(i) + (1 - alpha) * s;
            result.add(Math.round(s * 100.0) / 100.0);
        }
        return result;
    }

    private double getSeasonalFactor(int monthIndex, double[] prevValues) {
        double avgPrev = 0.0;
        for (double v : prevValues) avgPrev += v;
        avgPrev /= 12.0;
        if (avgPrev == 0) return 0.0;
        return (prevValues[monthIndex] - avgPrev) / avgPrev;
    }

    private List<ForecastDto.SeasonalityDto> computeSeasonality(double[] prevValues, double[] currentValues) {
        List<ForecastDto.SeasonalityDto> result = new ArrayList<>();
        double avgPrev = 0.0;
        for (double v : prevValues) avgPrev += v;
        avgPrev /= 12.0;

        for (int m = 0; m < 12; m++) {
            double idx = avgPrev > 0 ? prevValues[m] / avgPrev : 1.0;
            String trend;
            if (idx > 1.1) {
                trend = "HAUTE";
            } else if (idx < 0.9) {
                trend = "BASSE";
            } else {
                trend = "NORMALE";
            }
            result.add(new ForecastDto.SeasonalityDto(MONTH_NAMES_FR[m], Math.round(idx * 100.0) / 100.0, trend));
        }
        return result;
    }

    private double computeStdDev(List<Double> data) {
        if (data.isEmpty()) return 0.0;
        double mean = data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = data.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }
}
