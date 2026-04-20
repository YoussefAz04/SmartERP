package BuissnesManager.BuissnesManager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SavDto {
    private Long id;
    private String reference;
    private LocalDate dateOuverture;
    private LocalDate dateCloture;
    private String type;
    private String priorite;
    private String statut;
    private String sujet;
    private String description;
    private String resolution;
    private BigDecimal coutEstime;
    private BigDecimal coutReel;

    private Long clientId;
    private Long entrepriseId;
    private Long factureDeVenteId;
    private Long articleVenteId;
    private Long technicienId;
}
