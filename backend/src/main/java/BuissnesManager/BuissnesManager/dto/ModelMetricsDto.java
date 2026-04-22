package BuissnesManager.BuissnesManager.dto;

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
    /** Number of training rows actually used to fit the final model. */
    private Integer samples;
    /** Number of features (excluding intercept). */
    private Integer features;
    /** In-sample coefficient of determination, [0, 1]. */
    private Double rSquared;
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
    /** Number of CV folds actually evaluated. */
    private Integer cvFolds;
    /** Classifier accuracy, [0, 1]. */
    private Double accuracy;

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
}
