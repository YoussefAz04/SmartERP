package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ligne_commande_achat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 15, scale = 2)
    private BigDecimal quantite;

    @Column(precision = 15, scale = 2)
    private BigDecimal prixUnitaireHT;

    @Column(precision = 5, scale = 2)
    private BigDecimal tva;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantHT;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantTTC;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ✅ KEY FIX: Add nullable = false to ensure article_achat_id is required
    @ManyToOne(fetch = FetchType.EAGER)  // ✅ Change to EAGER to load immediately
    @JoinColumn(name = "article_achat_id", nullable = false)  // ✅ nullable = false
    @JsonIgnoreProperties({"lignesCommandeAchat", "fournisseur", "categorieFournisseur"})
    private ArticleAchat articleAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_achat_id", nullable = false)
    @JsonIgnore
    private CommandeAchat commandeAchat;
}