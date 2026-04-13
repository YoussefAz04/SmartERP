package BuissnesManager.BuissnesManager.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String token;
    private boolean enabled;
    private List<String> roles;
    private List<?> permission;
    private Long entrepriseId;  // ✅ ADD THIS
    private String entrepriseName;  // ✅ ADD THIS
    private Integer roleId;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<?> getPermission() { return permission; }
    public void setPermission(List<?> permission) { this.permission = permission; }

    public Long getEntrepriseId() { return entrepriseId; }  // ✅ ADD THIS
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }  // ✅ ADD THIS

    public String getEntrepriseName() { return entrepriseName; }  // ✅ ADD THIS
    public void setEntrepriseName(String entrepriseName) { this.entrepriseName = entrepriseName; }  // ✅ ADD THIS

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
}