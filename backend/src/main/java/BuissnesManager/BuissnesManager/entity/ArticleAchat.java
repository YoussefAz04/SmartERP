package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "article_achat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAchat {

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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "fournisseur_id")
    @JsonIgnoreProperties("articleAchats")
    private Fournisseur fournisseur;

    @ManyToOne
    @JoinColumn(name = "categorie_fournisseur_id")
    @JsonIgnoreProperties("articleAchats")
    private CategorieFournisseur categorieFournisseur;

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
