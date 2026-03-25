package BuissnesManager.BuissnesManager.dto;

public class RegisterResponse {
    private String message;
    private boolean success;
    private String username;
    private Long userId;

    public RegisterResponse(String message, boolean success, String username, Long userId) {
        this.message = message;
        this.success = success;
        this.username = username;
        this.userId = userId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
