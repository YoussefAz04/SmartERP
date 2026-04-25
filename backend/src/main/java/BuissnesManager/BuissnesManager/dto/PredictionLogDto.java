package BuissnesManager.BuissnesManager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only projection of a {@link BuissnesManager.BuissnesManager.entity.PredictionLog}
 * used by the AI dashboard to display predictions vs. real CA.
 */
public class PredictionLogDto {

    private Long id;
    private LocalDate predictionDate;
    private LocalDate predictionMonth;
    private LocalDate targetMonth;
    private String targetMonthLabel;
    private Integer horizonMonths;
    private Double predictedValue;
    private Double lowerBound;
    private Double upperBound;
    private Double confidenceScore;
    private String methodology;
    private String modelName;
    private Double actualValue;
    private Double absoluteError;
    private Double percentError;
    private Boolean withinInterval;
    private String status; // "PENDING" or "SETTLED"
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;

    public PredictionLogDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getPredictionDate() { return predictionDate; }
    public void setPredictionDate(LocalDate predictionDate) { this.predictionDate = predictionDate; }
    public LocalDate getPredictionMonth() { return predictionMonth; }
    public void setPredictionMonth(LocalDate predictionMonth) { this.predictionMonth = predictionMonth; }
    public LocalDate getTargetMonth() { return targetMonth; }
    public void setTargetMonth(LocalDate targetMonth) { this.targetMonth = targetMonth; }
    public String getTargetMonthLabel() { return targetMonthLabel; }
    public void setTargetMonthLabel(String targetMonthLabel) { this.targetMonthLabel = targetMonthLabel; }
    public Integer getHorizonMonths() { return horizonMonths; }
    public void setHorizonMonths(Integer horizonMonths) { this.horizonMonths = horizonMonths; }
    public Double getPredictedValue() { return predictedValue; }
    public void setPredictedValue(Double predictedValue) { this.predictedValue = predictedValue; }
    public Double getLowerBound() { return lowerBound; }
    public void setLowerBound(Double lowerBound) { this.lowerBound = lowerBound; }
    public Double getUpperBound() { return upperBound; }
    public void setUpperBound(Double upperBound) { this.upperBound = upperBound; }
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getMethodology() { return methodology; }
    public void setMethodology(String methodology) { this.methodology = methodology; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Double getActualValue() { return actualValue; }
    public void setActualValue(Double actualValue) { this.actualValue = actualValue; }
    public Double getAbsoluteError() { return absoluteError; }
    public void setAbsoluteError(Double absoluteError) { this.absoluteError = absoluteError; }
    public Double getPercentError() { return percentError; }
    public void setPercentError(Double percentError) { this.percentError = percentError; }
    public Boolean getWithinInterval() { return withinInterval; }
    public void setWithinInterval(Boolean withinInterval) { this.withinInterval = withinInterval; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
