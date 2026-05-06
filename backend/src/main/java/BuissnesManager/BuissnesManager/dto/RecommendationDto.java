package BuissnesManager.BuissnesManager.dto;

public class RecommendationDto {

    private String type;
    private String title;
    private String description;
    private String priority;
    private String icon;
    private String actionLabel;

    public RecommendationDto() {}

    public RecommendationDto(String type, String title, String description, String priority, String icon, String actionLabel) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.icon = icon;
        this.actionLabel = actionLabel;
    }

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
}
