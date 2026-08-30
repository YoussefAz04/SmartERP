package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "facture_de_vente")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FactureDeVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "date_achat")
    private String dateAchat;
    
    @Column(name = "referance")
    private String referance;

    @Column(name = "montant_total")
    private Double montantTotal;

    @Column(name = "montant_ht")
    private Double montantHT;
    
    @Column(name = "montant_tva")
    private Double montantTVA;
    
    @Column(name = "montant_ttc")
    private Double montantTTC;

    @Column(name = "remise")
    private Double remise;

    @Column(name = "mode_paiement")
    private String modePaiement;

    @Column(name = "statut")
    private String statut;

    @Column(name = "description")
    private String description;

    @Column(name = "chemin_fichier")
    private String cheminFichier;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "montant_paye", precision = 15, scale = 2)
    private BigDecimal montantPaye;

    @Column(name = "solde_restant", precision = 15, scale = 2)
    private BigDecimal soldeRestant;

    // ✅ ADD: Enterprise relationship
    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @ManyToOne
    @JoinColumn(name = "commande_vente_id")
    @JsonIgnoreProperties({"client", "entreprise", "factureDeVentes"})
    private CommandeVente commandeVente;

    @OneToOne(mappedBy = "factureDeVente", cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = "factureDeVente")
    private DetailsFactureDeVente detaisFactureDeVente;

    @ManyToOne
    @JsonIgnoreProperties(value = "factureDeVentes")
    private Client client;
}
