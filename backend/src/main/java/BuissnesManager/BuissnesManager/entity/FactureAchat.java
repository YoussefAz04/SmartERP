package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "facture_achat")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FactureAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_achat")
    private String dateAchat;

    @Column(name = "referance")
    private String referance;

    @Column(name = "montant_total")
    private Double montantTotal;

    @Column(name = "montantht")
    private Double montantHT;

    @Column(name = "montanttva")
    private Double montantTVA;

    @Column(name = "montantttc")
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

    @OneToOne(mappedBy = "factureAchat")
    @JsonIgnoreProperties(value = "factureAchat")
    private DetailsFactureAchat detaisFactureAchat;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    @JsonIgnoreProperties({"articleAchats", "factureAchats"})
    private Fournisseur fournisseur;

    @ManyToOne
    @JoinColumn(name = "commande_achat_id")
    @JsonIgnoreProperties({"factureAchats", "lignes", "fournisseur", "entreprise"})
    private CommandeAchat commandeAchat;

    // ✅ FIXED: Single entreprise field with proper relationship
    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    @JsonIgnoreProperties({"modules", "factureAchats", "factureVentes"})
    private Entreprise entreprise;

    @OneToMany(mappedBy = "factureAchat", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("factureAchat")
    private List<LigneFactureAchat> lignes;

}
