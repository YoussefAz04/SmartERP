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
@Table(name = "ligne_commande_vente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @JoinColumn(name = "article_vente_id")
    @JsonIgnoreProperties({"lignesCommandeVente", "articleAchat", "client"})
    private ArticleVente articleVente;

    @ManyToOne
    @JoinColumn(name = "commande_vente_id")
    @JsonIgnore
    private CommandeVente commandeVente;
}
