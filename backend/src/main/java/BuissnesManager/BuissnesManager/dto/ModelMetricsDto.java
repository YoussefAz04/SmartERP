package BuissnesManager.BuissnesManager.dto;

import java.util.List;
import java.util.Map;

/**
 * Generic ML diagnostics bundle emitted alongside the AI endpoints so the
 * frontend can display real accuracy numbers ("RMSE = 42.3, MAPE = 8.1%")
 * instead of a single opaque "confidence" percentage.
 *
 * <p>All fields are nullable so callers can set only the ones that make
 * sense for their task (e.g. a classifier fills {@code accuracy} and leaves
 * RMSE empty). Clients should treat any missing field as "not computed".
 */
public class ModelMetricsDto {

    /** Short name of the selected learner (e.g. "OLS", "Ridge", "RandomForest"). */
    private String modelName;
    /**
     * Short name of the runner-up learner kept for A/B comparison. {@code null}
     * when only one candidate was eligible. Recorded in {@code prediction_log}
     * so future endpoints can compare champion vs. challenger MAPE.
     */
    private String challengerModel;
    /** Number of training rows actually used to fit the final model. */
    private Integer samples;
    /** Number of features (excluding intercept). */
    private Integer features;
    /** In-sample coefficient of determination, [0, 1]. */
    private Double rSquared;
    /**
     * Plain-language caveat clarifying that R² is in-sample (training fit),
     * not a measure of future accuracy. Always populated alongside
     * {@link #rSquared} so the frontend can render the disclaimer.
     */
    private String rSquaredCaveat;
    /** In-sample root-mean-square error (original units). */
    private Double rmse;
    /** In-sample mean-absolute error (original units). */
    private Double mae;
    /** In-sample mean-absolute-percentage error, [0, 1]. */
    private Double mape;
    /** In-sample symmetric MAPE, [0, 1]. */
    private Double smape;
    /** Walk-forward / cross-validated RMSE (original units). */
    private Double cvRmse;
    /** Walk-forward / cross-validated MAPE. */
    private Double cvMape;
    /** Walk-forward / cross-validated sMAPE. */
    private Double cvSmape;
    /** Walk-forward / cross-validated MASE. &lt;1 = beats seasonal-naive. */
    private Double cvMase;
    /** Number of CV folds actually evaluated. */
    private Integer cvFolds;
    /** Classifier accuracy, [0, 1]. */
    private Double accuracy;
    /** Box-Cox λ chosen by profile-likelihood (null when transform disabled). */
    private Double boxCoxLambda;
    /** Feature importances of the selected model, ordered by magnitude. */
    private List<FeatureImportanceDto> featureImportances;
    /**
     * Glossary explaining each metric in plain language. Keys are metric
     * codes ({@code "rSquared"}, {@code "mape"}, {@code "smape"},
     * {@code "mase"}, ...).
     */
    private Map<String, MetricGlossaryEntryDto> glossary;

    public ModelMetricsDto() {}

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Integer getSamples() { return samples; }
    public void setSamples(Integer samples) { this.samples = samples; }
    public Integer getFeatures() { return features; }
    public void setFeatures(Integer features) { this.features = features; }
    public Double getRSquared() { return rSquared; }
    public void setRSquared(Double rSquared) { this.rSquared = rSquared; }
    public Double getRmse() { return rmse; }
    public void setRmse(Double rmse) { this.rmse = rmse; }
    public Double getMae() { return mae; }
    public void setMae(Double mae) { this.mae = mae; }
    public Double getMape() { return mape; }
    public void setMape(Double mape) { this.mape = mape; }
    public Double getSmape() { return smape; }
    public void setSmape(Double smape) { this.smape = smape; }
    public Double getCvRmse() { return cvRmse; }
    public void setCvRmse(Double cvRmse) { this.cvRmse = cvRmse; }
    public Double getCvMape() { return cvMape; }
    public void setCvMape(Double cvMape) { this.cvMape = cvMape; }
    public Double getCvSmape() { return cvSmape; }
    public void setCvSmape(Double cvSmape) { this.cvSmape = cvSmape; }
    public Integer getCvFolds() { return cvFolds; }
    public void setCvFolds(Integer cvFolds) { this.cvFolds = cvFolds; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    public String getRSquaredCaveat() { return rSquaredCaveat; }
    public void setRSquaredCaveat(String rSquaredCaveat) { this.rSquaredCaveat = rSquaredCaveat; }
    public Double getCvMase() { return cvMase; }
    public void setCvMase(Double cvMase) { this.cvMase = cvMase; }
    public Double getBoxCoxLambda() { return boxCoxLambda; }
    public void setBoxCoxLambda(Double boxCoxLambda) { this.boxCoxLambda = boxCoxLambda; }
    public List<FeatureImportanceDto> getFeatureImportances() { return featureImportances; }
    public void setFeatureImportances(List<FeatureImportanceDto> featureImportances) {
        this.featureImportances = featureImportances;
    }
    public Map<String, MetricGlossaryEntryDto> getGlossary() { return glossary; }
    public void setGlossary(Map<String, MetricGlossaryEntryDto> glossary) { this.glossary = glossary; }
    public String getChallengerModel() { return challengerModel; }
    public void setChallengerModel(String challengerModel) { this.challengerModel = challengerModel; }
}
