package BuissnesManager.BuissnesManager.entity;

import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// Table name preserved as-is for backward compatibility with existing DB; rename requires a DB migration
@Table(name = "DetaisFactureDeVente")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DetailsFactureDeVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantite;
    private Double prixUnitaireHT;
    private Double totalHT;
    private Double tva;
    private Double totalTTC;
    private String article ;

    @OneToOne()
    @JsonIgnoreProperties(value = "detaisFactureDeVente")
    private FactureDeVente factureDeVente ;

}
