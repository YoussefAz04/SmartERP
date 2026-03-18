package BuissnesManager.BuissnesManager.entity;

import lombok.Getter;
import lombok.Setter;

public class ZoneResponse {
    private String type;
    private String content;

    public ZoneResponse() {
    }

    public ZoneResponse(String type, String content) {
        this.type = type;
        this.content = content;
    }

    // Getters et Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
