package BuissnesManager.BuissnesManager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembreDto {
    private Long id;
    private String membreTitre;
    private String description;
    private String specialite;

    private List<User> users;
    private Long equipeId;
}
