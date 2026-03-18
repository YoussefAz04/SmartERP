package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FactureAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    private String  dateAchat;
    private String referance ;

    private Double montantTotal;


    private Double montantHT;
    private Double montantTVA;
    private Double montantTTC;

    private Double remise;

    private String modePaiement;

    private String statut;

    private String description;

    private String cheminFichier;



    @OneToOne(mappedBy = "factureAchat")
    @JsonIgnoreProperties(value = "factureAchat")
    private DetaisFactureAchat detaisFactureAchat ;


}
