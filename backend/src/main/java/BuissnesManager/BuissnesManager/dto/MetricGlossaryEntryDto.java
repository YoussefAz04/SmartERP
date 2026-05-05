package BuissnesManager.BuissnesManager.dto;

/**
 * Glossary entry for an error metric, surfaced in the methodology panel so
 * SMEs can interpret the dashboard numbers without a stats background.
 */
public class MetricGlossaryEntryDto {
    /** Short label (e.g. "MASE"). */
    private String label;
    /** One-sentence definition. */
    private String definition;
    /** Range of values the metric can take ("[0, +∞)"). */
    private String range;
    /** Plain-language interpretation ("Below 1 = beats seasonal-naive"). */
    private String interpretation;

    public MetricGlossaryEntryDto() {}

    public MetricGlossaryEntryDto(String label, String definition, String range, String interpretation) {
        this.label = label;
        this.definition = definition;
        this.range = range;
        this.interpretation = interpretation;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }
}
