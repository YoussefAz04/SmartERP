package BuissnesManager.BuissnesManager.dto;

import java.util.List;

/**
 * Backtest summary of past predictions vs. actual CA for an enterprise.
 *
 * <ul>
 *   <li><b>mae</b>: Mean Absolute Error in TND.</li>
 *   <li><b>mape</b>: Mean Absolute Percentage Error, in [0,1].</li>
 *   <li><b>smape</b>: symmetric MAPE, in [0,1].</li>
 *   <li><b>bias</b>: signed mean error (predicted − actual). Negative ⇒ chronic
 *       under-forecasting, positive ⇒ over-forecasting.</li>
 *   <li><b>intervalCoverage</b>: fraction of settled predictions whose actual
 *       fell inside the [lower, upper] band.</li>
 * </ul>
 */
public class PredictionAccuracyDto {

    private int totalPredictions;
    private int settledPredictions;
    private int pendingPredictions;
    private Double mae;
    private Double mape;
    private Double smape;
    private Double bias;
    private Double intervalCoverage;
    private List<PredictionLogDto> history;

    public PredictionAccuracyDto() {}

    public int getTotalPredictions() { return totalPredictions; }
    public void setTotalPredictions(int totalPredictions) { this.totalPredictions = totalPredictions; }
    public int getSettledPredictions() { return settledPredictions; }
    public void setSettledPredictions(int settledPredictions) { this.settledPredictions = settledPredictions; }
    public int getPendingPredictions() { return pendingPredictions; }
    public void setPendingPredictions(int pendingPredictions) { this.pendingPredictions = pendingPredictions; }
    public Double getMae() { return mae; }
    public void setMae(Double mae) { this.mae = mae; }
    public Double getMape() { return mape; }
    public void setMape(Double mape) { this.mape = mape; }
    public Double getSmape() { return smape; }
    public void setSmape(Double smape) { this.smape = smape; }
    public Double getBias() { return bias; }
    public void setBias(Double bias) { this.bias = bias; }
    public Double getIntervalCoverage() { return intervalCoverage; }
    public void setIntervalCoverage(Double intervalCoverage) { this.intervalCoverage = intervalCoverage; }
    public List<PredictionLogDto> getHistory() { return history; }
    public void setHistory(List<PredictionLogDto> history) { this.history = history; }
}
