package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
// Table name preserved as-is for backward compatibility with existing DB; rename requires a DB migration
@Table(name = "DetaisFactureAchat")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DetailsFactureAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantite;
    private Double prixUnitaireHT;
    private Double tva;
    private Double totalTTC;
    private String article ;

    @OneToOne()
    @JsonIgnoreProperties(value = "detaisFactureAchat")
    private FactureAchat factureAchat ;
}
