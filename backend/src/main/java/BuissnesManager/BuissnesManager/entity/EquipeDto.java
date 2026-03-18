package BuissnesManager.BuissnesManager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipeDto {
    private Long id;
    private String nom;
    private String description;
    private LocalDate dateCreation;
    private List<Membre> membres;
}
