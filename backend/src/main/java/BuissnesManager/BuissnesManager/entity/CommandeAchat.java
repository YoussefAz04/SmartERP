package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commande_achat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandeAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", unique = true)
    private String reference;

    @Column(name = "date_commande")
    private LocalDate dateCommande;

    @Column(name = "date_livraison_prevue")
    private LocalDate dateLivraisonPrevue;

    @Column(name = "statut")
    private String statut;

    @Column(name = "montant_ht", precision = 15, scale = 2)
    private BigDecimal montantHT;

    @Column(name = "montant_tva", precision = 15, scale = 2)
    private BigDecimal montantTVA;

    @Column(name = "montant_ttc", precision = 15, scale = 2)
    private BigDecimal montantTTC;

    @Column(name = "note")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    @JsonIgnoreProperties("commandesAchat")
    private Fournisseur fournisseur;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    @JsonIgnore
    private Entreprise entreprise;

    @OneToMany(mappedBy = "commandeAchat", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("commandeAchat")
    private List<LigneCommandeAchat> lignes;
}
