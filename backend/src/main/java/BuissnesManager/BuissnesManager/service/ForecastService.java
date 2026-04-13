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

        // Combine both years for historical series
        List<Double> historicalSeries = new ArrayList<>();
        for (double v : prevValues) historicalSeries.add(v);
        for (double v : currentValues) historicalSeries.add(v);

        // Apply exponential smoothing to generate smoothed series
        List<Double> smoothed = exponentialSmoothing(historicalSeries, ALPHA);

        // Build forecast points (historical + future 6 months)
        List<ForecastDto.ForecastPointDto> forecastPoints = new ArrayList<>();

        // Historical points (current year only, up to current month)
        for (int m = 1; m <= Math.min(currentMonth, 12); m++) {
            double actual = currentValues[m - 1];
            double smoothedVal = smoothed.get(11 + m); // offset by prev year (12 months)
            forecastPoints.add(new ForecastDto.ForecastPointDto(
                MONTH_NAMES_FR[m - 1], actual, smoothedVal,
                smoothedVal * 1.15, smoothedVal * 0.85
            ));
        }

        // Future forecast (next 6 months)
        double lastSmoothed = smoothed.get(smoothed.size() - 1);
        double stdDev = computeStdDev(historicalSeries);
        for (int i = 1; i <= 6; i++) {
            int futureMonth = (currentMonth + i - 1) % 12;
            double forecastValue = lastSmoothed * (1 + getSeasonalFactor(futureMonth, prevValues));
            forecastValue = Math.max(0, forecastValue);
            forecastPoints.add(new ForecastDto.ForecastPointDto(
                MONTH_NAMES_FR[futureMonth] + "+", null, forecastValue,
                forecastValue + stdDev, Math.max(0, forecastValue - stdDev)
            ));
        }

        // Compute seasonality indices
        List<ForecastDto.SeasonalityDto> seasonality = computeSeasonality(prevValues, currentValues);

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

        // Confidence score based on data availability
        long nonZeroMonths = historicalSeries.stream().filter(v -> v > 0).count();
        double confidence = Math.min(95, (nonZeroMonths / 24.0) * 100);

        ForecastDto dto = new ForecastDto();
        dto.setForecastPoints(forecastPoints);
        dto.setPredictedNextMonth(Math.round(predictedNextMonth * 100.0) / 100.0);
        dto.setPredictedNext3Months(Math.round(predictedNext3Months * 100.0) / 100.0);
        dto.setPredictedNext6Months(Math.round(predictedNext6Months * 100.0) / 100.0);
        dto.setConfidenceScore(Math.round(confidence * 10.0) / 10.0);
        dto.setMethodology("Lissage Exponentiel Simple (α=" + ALPHA + ")");
        dto.setSeasonality(seasonality);

        return dto;
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
