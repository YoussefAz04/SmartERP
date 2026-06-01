package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantite_entree", precision = 15, scale = 2)
    private BigDecimal quantiteEntree;

    @Column(name = "quantite_disponible", precision = 15, scale = 2)
    private BigDecimal quantiteDisponible;

    @Column(name = "quantite_reserve", precision = 15, scale = 2)
    private BigDecimal quantiteReserve;

    @Column(name = "quantite_vendue", precision = 15, scale = 2)
    private BigDecimal quantiteVendue;

    @Column(name = "seuil_minimum", precision = 15, scale = 2)
    private BigDecimal seuilMinimum;

    @Column(name = "seuil_alerte", precision = 15, scale = 2)
    private BigDecimal seuilAlerte;

    @Column(name = "emplacement")
    private String localisation;

    @Column(name = "date_dernier_inventaire")
    private LocalDateTime dateDernierInventaire;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lignesCommandeAchat", "categorieArticle", "mesure", "entreprise"})
    private Article articleAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    @JsonIgnore
    private Entreprise entreprise;

    @OneToOne(mappedBy = "stock", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "stock", "articleAchat", "client"})
    private ArticleVente articleVente;

    /**
     * Returns the effective available quantity: disponible (already excludes reserved).
     * Use this for all business logic checks against stock sufficiency.
     */
    public BigDecimal getAvailableQuantity() {
        return quantiteDisponible != null ? quantiteDisponible : BigDecimal.ZERO;
    }
}
