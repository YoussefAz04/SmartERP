package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.FeatureImportanceDto;
import BuissnesManager.BuissnesManager.dto.ForecastAnomalyDto;
import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.dto.MetricGlossaryEntryDto;
import BuissnesManager.BuissnesManager.dto.ModelMetricsDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.service.ml.CalendarFeatureService;
import BuissnesManager.BuissnesManager.service.ml.MlForecastEngine;
import BuissnesManager.BuissnesManager.service.ml.SeriesCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;




@Service
public class ForecastService {

    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;


    @Autowired(required = false)
    private MlForecastEngine mlForecastEngine;

    
    @Autowired(required = false)
    private CalendarFeatureService calendarFeatureService;

    
    @Value("${forecast.outlier.policy:winsorize}")
    private String outlierPolicy;

    
    @Value("${forecast.boxcox.enabled:true}")
    private boolean boxCoxEnabled;

    private static final String[] MONTH_NAMES_FR = {
        "Jan", "Fév", "Mars", "Avr", "Mai", "Juin",
        "Juil", "Août", "Sept", "Oct", "Nov", "Déc"
    };

    
    
    static final double ALPHA = 0.35; 
    static final double BETA  = 0.15; 

    
    
    private static final double MIN_SEASONAL_INDEX = 0.60; 
    private static final double MAX_SEASONAL_INDEX = 1.50; 

    
    
    private static final double MAX_TREND_GROWTH_PER_MONTH = 0.50; 

    @Cacheable(cacheNames = "salesForecast",
               key = "#entrepriseId + '-' + T(java.time.LocalDate).now().getDayOfYear()")
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

        
        
        
        List<Double> historicalSeries = new ArrayList<>();
        if (hasPrevYear) {
            for (double v : prevValues) historicalSeries.add(v);
        }
        for (int i = 0; i < observedCurrentMonths; i++) {
            historicalSeries.add(currentValues[i]);
        }

        
        
        
        
        
        if (mlForecastEngine != null && mlForecastEngine.canFit(historicalSeries.size())) {
            ForecastDto mlDto = generateMlForecast(
                historicalSeries, currentValues, prevValues, hasPrevYear,
                observedCurrentMonths, currentMonth);
            if (mlDto != null) return mlDto;
        
        }

        
        
        double[] seasonalIndices = computeSeasonalIndices(prevValues, hasPrevYear);

        
        
        
        HoltResult holt = fitHolt(historicalSeries, seasonalIndices, hasPrevYear);

        List<ForecastDto.ForecastPointDto> forecastPoints = new ArrayList<>();

        
        
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

        
        
        double level = holt.finalLevel;
        double trend = holt.finalTrend;
        
        
        if (!hasPrevYear && level > 0) {
            double maxStep = level * MAX_TREND_GROWTH_PER_MONTH;
            if (trend >  maxStep) trend =  maxStep;
            if (trend < -maxStep) trend = -maxStep;
        }

        for (int h = 1; h <= 6; h++) {
            
            int futureMonthIdx = ((currentMonth - 1) + h) % 12; 
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

        
        
        List<Double> futureForecasts = new ArrayList<>();
        for (ForecastDto.ForecastPointDto p : forecastPoints) {
            if (p.getActual() == null && p.getForecast() != null) {
                futureForecasts.add(p.getForecast());
            }
        }
        double predictedNextMonth = futureForecasts.isEmpty() ? 0.0 : futureForecasts.get(0);
        double predictedNext3Months = futureForecasts.stream().limit(3).mapToDouble(Double::doubleValue).sum();
        double predictedNext6Months = futureForecasts.stream().limit(6).mapToDouble(Double::doubleValue).sum();

        
        long nonZeroMonths = historicalSeries.stream().filter(v -> v > 0).count();
        double expectedMonths = hasPrevYear ? 24.0 : 12.0;
        double density = Math.min(1.0, nonZeroMonths / expectedMonths);
        double fit = computeSmapeFit(historicalSeries, holt.fitted);
        double confidence = Math.min(95.0, Math.max(0.0, (0.5 * density + 0.5 * fit) * 100.0));

        
        
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

    

    










    private ForecastDto generateMlForecast(
            List<Double> historicalSeries, double[] currentValues, double[] prevValues,
            boolean hasPrevYear, int observedCurrentMonths, int currentMonth) {

        double[] rawSeries = new double[historicalSeries.size()];
        for (int i = 0; i < rawSeries.length; i++) rawSeries[i] = historicalSeries.get(i);

        
        
        
        
        String[] status = computeHistoryStatus(rawSeries);

        
        
        int currentYear = LocalDate.now().getYear();
        YearMonth firstMonth = hasPrevYear
                ? YearMonth.of(currentYear - 1, 1)
                : YearMonth.of(currentYear, 1);

        
        
        
        SeriesCleaner.Policy policy = SeriesCleaner.Policy.parse(outlierPolicy);
        SeriesCleaner.Result cleaned = SeriesCleaner.clean(rawSeries, policy,
                SeriesCleaner.DEFAULT_WINDOW, SeriesCleaner.DEFAULT_K);
        double[] series = cleaned.cleaned;
        if (policy == SeriesCleaner.Policy.REJECT) {
            
            
            
            interpolateNaN(series);
        }

        MlForecastEngine.MlForecastResult result;
        try {
            result = mlForecastEngine.forecast(series, 0, 6, firstMonth, !boxCoxEnabled);
        } catch (RuntimeException ex) {
            log.warn("ML forecast failed ({}); falling back to Holt-Winters.", ex.getMessage());
            return null;
        }

        int historyOffset = hasPrevYear ? 12 : 0;
        List<ForecastDto.ForecastPointDto> forecastPoints = new ArrayList<>();
        
        for (int m = 1; m <= observedCurrentMonths; m++) {
            double actual = currentValues[m - 1];
            int fittedIdx = historyOffset + m - 1;
            double fitted = fittedIdx >= 0 && fittedIdx < result.fittedHistorical.size()
                ? result.fittedHistorical.get(fittedIdx) : actual;
            double upper = Math.max(0.0, fitted + result.residualStd);
            double lower = Math.max(0.0, fitted - result.residualStd);
            forecastPoints.add(new ForecastDto.ForecastPointDto(
                MONTH_NAMES_FR[m - 1], round2(actual), round2(fitted),
                round2(upper), round2(lower)
            ));
        }

        
        for (int h = 1; h <= result.futurePoint.size(); h++) {
            int futureMonthIdx = ((currentMonth - 1) + h) % 12;
            double forecastValue = result.futurePoint.get(h - 1);
            double upper;
            double lower;
            if (result.upperBounds != null && h - 1 < result.upperBounds.size()
                    && result.lowerBounds != null && h - 1 < result.lowerBounds.size()) {
                upper = Math.max(0.0, result.upperBounds.get(h - 1));
                lower = Math.max(0.0, result.lowerBounds.get(h - 1));
                if (upper < forecastValue) upper = forecastValue;
                if (lower > forecastValue) lower = forecastValue;
            } else {
                double horizonStd = result.residualStd * Math.sqrt((double) h);
                upper = Math.max(0.0, forecastValue + horizonStd);
                lower = Math.max(0.0, forecastValue - horizonStd);
            }
            forecastPoints.add(new ForecastDto.ForecastPointDto(
                MONTH_NAMES_FR[futureMonthIdx] + "+", null,
                round2(forecastValue), round2(upper), round2(lower)
            ));
        }

        
        List<Double> futureForecasts = new ArrayList<>();
        for (ForecastDto.ForecastPointDto p : forecastPoints) {
            if (p.getActual() == null && p.getForecast() != null) {
                futureForecasts.add(p.getForecast());
            }
        }
        double predictedNextMonth = futureForecasts.isEmpty() ? 0.0 : futureForecasts.get(0);
        double predictedNext3Months = futureForecasts.stream().limit(3).mapToDouble(Double::doubleValue).sum();
        double predictedNext6Months = futureForecasts.stream().limit(6).mapToDouble(Double::doubleValue).sum();

        long nonZeroMonths = historicalSeries.stream().filter(v -> v > 0).count();
        double expectedMonths = hasPrevYear ? 24.0 : 12.0;
        double density = Math.min(1.0, nonZeroMonths / expectedMonths);
        double rSquaredClamped = Math.max(0.0, Math.min(1.0, result.rSquared));
        double accuracySignal;
        if (!Double.isNaN(result.walkForwardMape)) {
            accuracySignal = Math.max(0.0, Math.min(1.0, 1.0 - result.walkForwardMape));
        } else {
            accuracySignal = rSquaredClamped;
        }
        double seriesMean = historicalSeries.stream().filter(v -> v > 0)
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        double stability = seriesMean > 0
                ? Math.max(0.0, 1.0 - Math.min(1.0, result.residualStd / seriesMean))
                : 0.5;
        double confidence = Math.min(95.0, Math.max(0.0,
                (0.3 * density + 0.5 * accuracySignal + 0.2 * stability) * 100.0));

        List<ForecastDto.SeasonalityDto> seasonality =
            computeSeasonalityView(hasPrevYear ? prevValues : currentValues);

        ForecastDto dto = new ForecastDto();
        dto.setForecastPoints(forecastPoints);
        dto.setPredictedNextMonth(round2(predictedNextMonth));
        dto.setPredictedNext3Months(round2(predictedNext3Months));
        dto.setPredictedNext6Months(round2(predictedNext6Months));
        dto.setConfidenceScore(Math.round(confidence * 10.0) / 10.0);

        String mapeText = Double.isNaN(result.walkForwardMape)
                ? "MAPE-CV n/a"
                : String.format(java.util.Locale.ROOT,
                        "MAPE-CV=%.1f%%, sMAPE-CV=%.1f%%, MASE=%.2f (%d plis)",
                        result.walkForwardMape * 100.0,
                        Double.isNaN(result.walkForwardSmape) ? 0.0 : result.walkForwardSmape * 100.0,
                        Double.isNaN(result.walkForwardMase) ? Double.NaN : result.walkForwardMase,
                        result.walkForwardFolds);
        String boxCoxText = Double.isNaN(result.boxCoxLambda)
                ? "" : String.format(java.util.Locale.ROOT, ", Box-Cox λ=%.2f", result.boxCoxLambda);
        dto.setMethodology(String.format(java.util.Locale.ROOT,
            "ML supervisé Smile %s (%d caractéristiques lag/saisonnières/calendaires, " +
                    "intervalles bootstrap, CA exprimé HT) — R² in-sample=%.2f, %s%s, %d échantillons",
            result.modelName, result.featureCount, rSquaredClamped, mapeText, boxCoxText,
            result.trainingSamples));
        dto.setSeasonality(seasonality);
        dto.setSelectedModel(result.modelName);
        if (!Double.isNaN(result.walkForwardMape)) {
            dto.setCrossValidatedMape(Math.round(result.walkForwardMape * 10000.0) / 10000.0);
        }
        if (!Double.isNaN(result.walkForwardSmape)) {
            dto.setCrossValidatedSmape(Math.round(result.walkForwardSmape * 10000.0) / 10000.0);
        }
        if (!Double.isNaN(result.walkForwardRmse)) {
            dto.setCrossValidatedRmse(Math.round(result.walkForwardRmse * 100.0) / 100.0);
        }
        if (!Double.isNaN(result.walkForwardMase)) {
            dto.setCrossValidatedMase(Math.round(result.walkForwardMase * 1000.0) / 1000.0);
        }

        
        List<String> visibleStatus = new ArrayList<>(observedCurrentMonths);
        for (int i = 0; i < observedCurrentMonths && historyOffset + i < status.length; i++) {
            visibleStatus.add(status[historyOffset + i]);
        }
        dto.setHistoryStatus(visibleStatus);

        
        
        List<ForecastAnomalyDto> anomalies = new ArrayList<>();
        for (SeriesCleaner.Anomaly a : cleaned.anomalies) {
            YearMonth ym = firstMonth.plusMonths(a.index);
            anomalies.add(new ForecastAnomalyDto(
                    ym.toString(),
                    round2(a.original),
                    round2(a.replacement),
                    Math.round(a.zScore * 100.0) / 100.0));
        }
        dto.setAnomalies(anomalies);

        
        ModelMetricsDto metrics = new ModelMetricsDto();
        metrics.setModelName(result.modelName);
        metrics.setChallengerModel(result.challengerName);
        metrics.setSamples(result.trainingSamples);
        metrics.setFeatures(result.featureCount);
        metrics.setRSquared(Math.round(rSquaredClamped * 1000.0) / 1000.0);
        metrics.setRSquaredCaveat(
                "Mesure l'ajustement aux données d'entraînement; n'indique pas la précision sur le futur.");
        if (!Double.isNaN(result.walkForwardMape)) {
            metrics.setCvMape(Math.round(result.walkForwardMape * 10000.0) / 10000.0);
        }
        if (!Double.isNaN(result.walkForwardSmape)) {
            metrics.setCvSmape(Math.round(result.walkForwardSmape * 10000.0) / 10000.0);
        }
        if (!Double.isNaN(result.walkForwardRmse)) {
            metrics.setCvRmse(Math.round(result.walkForwardRmse * 100.0) / 100.0);
        }
        if (!Double.isNaN(result.walkForwardMase)) {
            metrics.setCvMase(Math.round(result.walkForwardMase * 1000.0) / 1000.0);
        }
        if (!Double.isNaN(result.boxCoxLambda)) {
            metrics.setBoxCoxLambda(Math.round(result.boxCoxLambda * 100.0) / 100.0);
        }
        metrics.setCvFolds(result.walkForwardFolds);
        metrics.setFeatureImportances(buildFeatureImportances(result));
        metrics.setGlossary(buildMetricsGlossary());
        dto.setModelMetrics(metrics);

        if (log.isDebugEnabled()) {
            log.debug("ML forecast: model={}, R²={}, OOS-MAPE={}, MASE={}, anomalies={}, λ={}",
                    result.modelName, result.rSquared, result.walkForwardMape,
                    result.walkForwardMase, anomalies.size(), result.boxCoxLambda);
        }

        return dto;
    }

    

    









    static String[] computeHistoryStatus(double[] series) {
        String[] out = new String[series.length];
        int firstNonZero = -1;
        for (int i = 0; i < series.length; i++) if (series[i] > 0) { firstNonZero = i; break; }
        for (int i = 0; i < series.length; i++) {
            if (firstNonZero >= 0 && i < firstNonZero) out[i] = "MISSING";
            else if (series[i] > 0) out[i] = "OBSERVED";
            else out[i] = "ZERO_BUSINESS";
        }
        return out;
    }

    
    private static void interpolateNaN(double[] series) {
        int n = series.length;
        
        int firstFinite = -1;
        for (int i = 0; i < n; i++) if (!Double.isNaN(series[i])) { firstFinite = i; break; }
        if (firstFinite < 0) { for (int i = 0; i < n; i++) series[i] = 0.0; return; }
        for (int i = 0; i < firstFinite; i++) series[i] = series[firstFinite];
        
        int i = firstFinite + 1;
        while (i < n) {
            if (Double.isNaN(series[i])) {
                int j = i;
                while (j < n && Double.isNaN(series[j])) j++;
                if (j == n) {
                    for (int k = i; k < n; k++) series[k] = series[i - 1];
                } else {
                    double slope = (series[j] - series[i - 1]) / (j - (i - 1));
                    for (int k = i; k < j; k++) series[k] = series[i - 1] + slope * (k - (i - 1));
                }
                i = j;
            } else i++;
        }
    }

    private static List<FeatureImportanceDto> buildFeatureImportances(MlForecastEngine.MlForecastResult r) {
        List<FeatureImportanceDto> out = new ArrayList<>();
        if (r == null || r.featureNames == null || r.featureImportances == null) return out;
        int p = Math.min(r.featureImportances.length, r.featureNames.length - 1);
        
        
        Integer[] order = new Integer[p];
        for (int i = 0; i < p; i++) order[i] = i;
        java.util.Arrays.sort(order, (a, b) ->
                Double.compare(Math.abs(r.featureImportances[b]),
                               Math.abs(r.featureImportances[a])));
        Map<String, String> calendarGloss = CalendarFeatureService.glossary();
        for (int idx : order) {
            String name = r.featureNames[idx + 1];
            double v = r.featureImportances[idx];
            int dir = r.importancesSigned ? Integer.signum((int) Math.signum(v)) : 0;
            String desc = calendarGloss.getOrDefault(name, defaultFeatureDescription(name));
            out.add(new FeatureImportanceDto(name, Math.round(Math.abs(v) * 1000.0) / 1000.0,
                    dir, desc));
        }
        return out;
    }

    private static String defaultFeatureDescription(String name) {
        switch (name) {
            case "lag1": return "Valeur du mois précédent (composante autorégressive).";
            case "lag2": return "Valeur d'il y a deux mois.";
            case "t":    return "Tendance temporelle (indice du mois normalisé).";
            case "sin_m": case "cos_m": return "Composante saisonnière annuelle (Fourier).";
            case "sin_2m": case "cos_2m": return "Composante saisonnière semestrielle (Fourier 2x).";
            default: return name;
        }
    }

    private static Map<String, MetricGlossaryEntryDto> buildMetricsGlossary() {
        Map<String, MetricGlossaryEntryDto> g = new LinkedHashMap<>();
        g.put("rSquared", new MetricGlossaryEntryDto(
                "R²",
                "Part de la variance expliquée par le modèle sur les données d'entraînement.",
                "[0, 1]",
                "Mesure d'ajustement in-sample. Une valeur élevée n'implique pas une bonne précision sur le futur."));
        g.put("mape", new MetricGlossaryEntryDto(
                "MAPE",
                "Erreur absolue moyenne en pourcentage.",
                "[0, +∞)",
                "MAPE devient instable lorsque le CA approche zéro (division par 0). Nous privilégions sMAPE / MASE."));
        g.put("smape", new MetricGlossaryEntryDto(
                "sMAPE",
                "MAPE symétrique : erreur absolue / moyenne(|réel|, |prévu|).",
                "[0, 2]",
                "Plus stable que MAPE près de zéro ; valeurs basses = bon."));
        g.put("mase", new MetricGlossaryEntryDto(
                "MASE",
                "Erreur absolue moyenne mise à l'échelle par la naïve saisonnière (12 mois).",
                "[0, +∞)",
                "MASE < 1 = on bat la prévision naïve (référence honnête). MASE ≥ 1 = on perd."));
        return g;
    }

    

    
    private static final class HoltResult {
        final List<Double> fitted;   
        final double finalLevel;     
        final double finalTrend;     
        HoltResult(List<Double> fitted, double finalLevel, double finalTrend) {
            this.fitted = fitted;
            this.finalLevel = finalLevel;
            this.finalTrend = finalTrend;
        }
    }

    





    HoltResult fitHolt(List<Double> data, double[] seasonalIndices, boolean hasSeasonal) {
        List<Double> fitted = new ArrayList<>(data.size());
        if (data.isEmpty()) {
            return new HoltResult(fitted, 0.0, 0.0);
        }

        
        
        double firstSeasonal = hasSeasonal ? seasonalForIndex(0, seasonalIndices) : 1.0;
        double level = data.get(0) / safe(firstSeasonal);
        double trend = 0.0;
        if (data.size() >= 2) {
            double secondSeasonal = hasSeasonal ? seasonalForIndex(1, seasonalIndices) : 1.0;
            double second = data.get(1) / safe(secondSeasonal);
            trend = second - level;
        }
        
        fitted.add(data.get(0));

        for (int t = 1; t < data.size(); t++) {
            double seasonal = hasSeasonal ? seasonalForIndex(t, seasonalIndices) : 1.0;
            
            double oneStep = Math.max(0.0, (level + trend) * seasonal);
            fitted.add(oneStep);
            double deseasonalised = data.get(t) / safe(seasonal);
            double prevLevel = level;
            level = ALPHA * deseasonalised + (1.0 - ALPHA) * (level + trend);
            trend = BETA  * (level - prevLevel) + (1.0 - BETA) * trend;
        }
        return new HoltResult(fitted, level, trend);
    }

    





    private double seasonalForIndex(int t, double[] seasonalIndices) {
        int monthIdx = t % 12;
        return safe(seasonalIndices[monthIdx]);
    }

    
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

    
    double computeResidualStd(List<Double> actuals, List<Double> fitted) {
        if (actuals.isEmpty() || fitted.isEmpty()) return 0.0;
        int size = Math.min(actuals.size(), fitted.size());
        double sumSq = 0.0;
        int n = 0;
        
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

    
    private static double safe(double factor) {
        return factor <= 0 ? 1.0 : factor;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
