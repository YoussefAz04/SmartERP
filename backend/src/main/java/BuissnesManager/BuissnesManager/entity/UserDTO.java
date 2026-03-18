package BuissnesManager.BuissnesManager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String phone;
    private String username;
    private boolean enabled;
    private List<String> roles; // noms des rôles
    private Long entrepriseId;
    private String entrepriseNom;
}