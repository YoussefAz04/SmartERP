package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "article_vente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Column(name = "prix_unitaire", precision = 15, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @PrePersist
    protected void prePersist() {
        if (this.quantite == null) {
            this.quantite = 0;
        }
    }

    private String unite;

    private String sku;

    @Column(precision = 5, scale = 2)
    private BigDecimal markup;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "article_achat_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lignesCommandeAchat", "mesure", "entreprise"})
    private Article articleAchat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "articleVentes", "factureDeVentes"})
    private Client client;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Stock stock;
}
