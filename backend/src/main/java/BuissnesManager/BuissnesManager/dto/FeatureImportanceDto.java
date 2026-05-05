package BuissnesManager.BuissnesManager.dto;

/**
 * Single feature contribution to a forecast, used to populate the "what
 * drives the prediction?" panel in the AI dashboard.
 *
 * <p>For linear models (OLS / Ridge / ElasticNet) the value is the
 * <i>standardised</i> coefficient β·σ_x — directly comparable across
 * features regardless of unit. For tree ensembles (RandomForest) it is the
 * built-in mean decrease in impurity from Smile.
 */
public class FeatureImportanceDto {

    /** Engineered feature name (e.g. {@code "lag1"}, {@code "is_ramadan"}). */
    private String name;
    /** Magnitude of the contribution. Always non-negative. */
    private Double value;
    /**
     * Sign of the linear coefficient — {@code +1}, {@code -1} or {@code 0}.
     * For tree models {@code 0} is reported (the impurity importance is
     * unsigned).
     */
    private Integer direction;
    /** Optional human-readable label for the methodology panel. */
    private String description;

    public FeatureImportanceDto() {}

    public FeatureImportanceDto(String name, Double value, Integer direction, String description) {
        this.name = name;
        this.value = value;
        this.direction = direction;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public Integer getDirection() { return direction; }
    public void setDirection(Integer direction) { this.direction = direction; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
