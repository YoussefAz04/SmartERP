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
@Table(name = "article")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", unique = true)
    private String reference;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description")
    private String description;

    // ✅ FIXED: Explicit column names with proper precision/scale
    @Column(name = "prix_achat", precision = 15, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "prix_vente", precision = 15, scale = 2)
    private BigDecimal prixVente;

    @Column(name = "actif")
    private boolean actif = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_article_id")
    @JsonIgnoreProperties("articles")
    private CategorieArticle categorieArticle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesure_id")
    @JsonIgnoreProperties("articles")
    private Mesure mesure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    @JsonIgnore
    private Entreprise entreprise;

    // ❌ REMOVED @PrePersist/@PreUpdate hooks
    // Let Hibernate @CreationTimestamp/@UpdateTimestamp handle timestamps automatically
}