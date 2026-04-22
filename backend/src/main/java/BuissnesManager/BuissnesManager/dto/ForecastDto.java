package BuissnesManager.BuissnesManager.dto;

import java.util.List;

public class ForecastDto {

    private List<ForecastPointDto> forecastPoints;
    private Double predictedNextMonth;
    private Double predictedNext3Months;
    private Double predictedNext6Months;
    private Double confidenceScore;
    private String methodology;
    private List<SeasonalityDto> seasonality;
    /** Name of the learner picked by walk-forward CV (e.g. "Ridge", "RandomForest"). */
    private String selectedModel;
    /** Cross-validated RMSE, in TND (null if CV couldn't run). */
    private Double crossValidatedRmse;
    /** Cross-validated sMAPE, [0, 1]. */
    private Double crossValidatedSmape;
    /** Cross-validated MAPE, [0, 1]. */
    private Double crossValidatedMape;
    /** Full ML diagnostics bundle for the methodology panel. */
    private ModelMetricsDto modelMetrics;

    public ForecastDto() {}

    public List<ForecastPointDto> getForecastPoints() { return forecastPoints; }
    public void setForecastPoints(List<ForecastPointDto> forecastPoints) { this.forecastPoints = forecastPoints; }

    public Double getPredictedNextMonth() { return predictedNextMonth; }
    public void setPredictedNextMonth(Double predictedNextMonth) { this.predictedNextMonth = predictedNextMonth; }

    public Double getPredictedNext3Months() { return predictedNext3Months; }
    public void setPredictedNext3Months(Double predictedNext3Months) { this.predictedNext3Months = predictedNext3Months; }

    public Double getPredictedNext6Months() { return predictedNext6Months; }
    public void setPredictedNext6Months(Double predictedNext6Months) { this.predictedNext6Months = predictedNext6Months; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getMethodology() { return methodology; }
    public void setMethodology(String methodology) { this.methodology = methodology; }

    public List<SeasonalityDto> getSeasonality() { return seasonality; }
    public void setSeasonality(List<SeasonalityDto> seasonality) { this.seasonality = seasonality; }

    public String getSelectedModel() { return selectedModel; }
    public void setSelectedModel(String selectedModel) { this.selectedModel = selectedModel; }
    public Double getCrossValidatedRmse() { return crossValidatedRmse; }
    public void setCrossValidatedRmse(Double crossValidatedRmse) { this.crossValidatedRmse = crossValidatedRmse; }
    public Double getCrossValidatedSmape() { return crossValidatedSmape; }
    public void setCrossValidatedSmape(Double crossValidatedSmape) { this.crossValidatedSmape = crossValidatedSmape; }
    public Double getCrossValidatedMape() { return crossValidatedMape; }
    public void setCrossValidatedMape(Double crossValidatedMape) { this.crossValidatedMape = crossValidatedMape; }
    public ModelMetricsDto getModelMetrics() { return modelMetrics; }
    public void setModelMetrics(ModelMetricsDto modelMetrics) { this.modelMetrics = modelMetrics; }

    public static class ForecastPointDto {
        private String period;
        private Double actual;
        private Double forecast;
        private Double upperBound;
        private Double lowerBound;

        public ForecastPointDto() {}

        public ForecastPointDto(String period, Double actual, Double forecast, Double upperBound, Double lowerBound) {
            this.period = period;
            this.actual = actual;
            this.forecast = forecast;
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
        }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public Double getActual() { return actual; }
        public void setActual(Double actual) { this.actual = actual; }
        public Double getForecast() { return forecast; }
        public void setForecast(Double forecast) { this.forecast = forecast; }
        public Double getUpperBound() { return upperBound; }
        public void setUpperBound(Double upperBound) { this.upperBound = upperBound; }
        public Double getLowerBound() { return lowerBound; }
        public void setLowerBound(Double lowerBound) { this.lowerBound = lowerBound; }
    }

    public static class SeasonalityDto {
        private String month;
        private Double index;
        private String trend;

        public SeasonalityDto() {}

        public SeasonalityDto(String month, Double index, String trend) {
            this.month = month;
            this.index = index;
            this.trend = trend;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public Double getIndex() { return index; }
        public void setIndex(Double index) { this.index = index; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }
}
