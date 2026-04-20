package BuissnesManager.BuissnesManager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SavInterventionDto {
    private Long id;
    private String type;
    private String statut;
    private String commentaire;
    private String auteur;
    private Long savId;
}
