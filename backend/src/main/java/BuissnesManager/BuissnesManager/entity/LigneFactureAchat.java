package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ligne_facture_achat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_nom")
    private String designation;

    @Column(name = "quantite", precision = 15, scale = 2, nullable = false)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire_ht", precision = 15, scale = 2, nullable = false)
    private BigDecimal prixUnitaireHT;

    @Column(name = "taux_tva", precision = 5, scale = 2)
    private BigDecimal tva;

    @Column(name = "montant_ht", precision = 15, scale = 2)
    private BigDecimal montantHT;

    @Column(name = "montant_ttc", precision = 15, scale = 2)
    private BigDecimal montantTTC;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "facture_achat_id")
    @JsonIgnore
    private FactureAchat factureAchat;

    // ✅ ADD THIS: Validate and set defaults before persisting
    @PrePersist
    protected void prePersist() {
        if (this.prixUnitaireHT == null) {
            this.prixUnitaireHT = new BigDecimal("0.00");
        }
        if (this.quantite == null) {
            this.quantite = new BigDecimal("1.00");
        }
        if (this.tva == null) {
            this.tva = new BigDecimal("0.00");
        }
        if (this.montantHT == null) {
            this.montantHT = new BigDecimal("0.00");
        }
        if (this.montantTTC == null) {
            this.montantTTC = new BigDecimal("0.00");
        }
    }
}