package BuissnesManager.BuissnesManager.dto;

/**
 * Details of an outlier value detected by the Hampel filter and replaced
 * (or flagged) before training. Surfaced in {@link ForecastDto} so the UI
 * can warn the user: "April 2024 was 970 000 TND vs. typical 9 700 TND —
 * please verify".
 */
public class ForecastAnomalyDto {
    /** ISO month label, e.g. "2024-04". */
    private String period;
    /** Original value as it appeared in the data. */
    private Double original;
    /** Replacement value used by the model (may equal original if policy=raw). */
    private Double replacement;
    /** How many MAD units the original deviated from the local median. */
    private Double zScore;

    public ForecastAnomalyDto() {}

    public ForecastAnomalyDto(String period, Double original, Double replacement, Double zScore) {
        this.period = period;
        this.original = original;
        this.replacement = replacement;
        this.zScore = zScore;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Double getOriginal() { return original; }
    public void setOriginal(Double original) { this.original = original; }
    public Double getReplacement() { return replacement; }
    public void setReplacement(Double replacement) { this.replacement = replacement; }
    public Double getZScore() { return zScore; }
    public void setZScore(Double zScore) { this.zScore = zScore; }
}
