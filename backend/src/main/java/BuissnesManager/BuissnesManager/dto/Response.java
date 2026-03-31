package BuissnesManager.BuissnesManager.dto;

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
    private List<PermissionDto> permission;  // ✅ Use PermissionDto, not Permission entity
}