package BuissnesManager.BuissnesManager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartementDTO {

    private Long id;
    private String nom;
    private String description;
    private String responsable;
    private String email;
    private String telephone;
    private Integer nombreEmployes;

    // Pour éviter de retourner toute l'entité Entreprise, on ne garde que les infos utiles
    private Long entrepriseId;
    private String entrepriseNom;
}