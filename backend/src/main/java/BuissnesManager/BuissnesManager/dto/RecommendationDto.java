package BuissnesManager.BuissnesManager.dto;

import java.util.Arrays;
import java.util.List;

public class RecommendationDto {

    private String type;
    private String title;
    private String description;
    private String priority;
    private String icon;
    private String actionLabel;

    /**
     * Structured key-value data points surfaced as visual metric chips in the
     * dashboard. Optional — when null/empty the frontend falls back to the plain
     * {@link #description} text.
     */
    private List<MetricItem> metrics;

    /**
     * The action-oriented advice sentence displayed as a highlighted callout at
     * the bottom of the recommendation card. Optional — separated from
     * {@link #metrics} so the UI can style it distinctly.
     */
    private String insight;

    // ── Inner type ────────────────────────────────────────────────────────────

    /** A single labelled metric displayed in the card metrics grid. */
    public static class MetricItem {
        private String label;
        private String value;

        public MetricItem() {}

        public MetricItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public RecommendationDto() {}

    public RecommendationDto(String type, String title, String description,
                             String priority, String icon, String actionLabel) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.icon = icon;
        this.actionLabel = actionLabel;
    }

    // ── Fluent builder helpers ────────────────────────────────────────────────

    /** Attaches structured metric chips and returns {@code this} for chaining. */
    public RecommendationDto withMetrics(MetricItem... items) {
        this.metrics = Arrays.asList(items);
        return this;
    }

    /** Attaches the action-advice callout text and returns {@code this} for chaining. */
    public RecommendationDto withInsight(String insight) {
        this.insight = insight;
        return this;
    }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getActionLabel() { return actionLabel; }
    public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }

    public List<MetricItem> getMetrics() { return metrics; }
    public void setMetrics(List<MetricItem> metrics) { this.metrics = metrics; }

    public String getInsight() { return insight; }
    public void setInsight(String insight) { this.insight = insight; }
}
