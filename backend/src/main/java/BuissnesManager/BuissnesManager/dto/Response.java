package BuissnesManager.BuissnesManager.dto;

import BuissnesManager.BuissnesManager.entity.Permission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String token;
    private boolean enabled;
    private List<String> roles;
    private List<Permission> permission;
    
    // ← ADD THIS: Ignore the Entreprise to avoid serialization issues
    @JsonIgnore
    private Object entreprise;
    
    // Optional: If you need entreprise info, create a simple DTO instead
    // private EntrepriseSimpleDTO entreprise;
}