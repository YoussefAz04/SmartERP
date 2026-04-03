package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

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

    private Integer quantite;

    private String unite;

    private String sku;

    @Column(precision = 5, scale = 2)
    private BigDecimal markup;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    @JsonIgnoreProperties({"lignesCommandeAchat", "categorieArticle", "mesure", "entreprise"})
    private Article articleAchat;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties("articleVentes")
    private Client client;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
