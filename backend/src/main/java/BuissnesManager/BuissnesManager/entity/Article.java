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

    @Column(unique = true)
    private String reference;

    private String nom;

    private String description;

    @Column(name = "prix_achat", precision = 15, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "prix_vente", precision = 15, scale = 2)
    private BigDecimal prixVente;

    @Column(precision = 5, scale = 2)
    private BigDecimal tva;

    private boolean actif = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_article_id")
    @JsonIgnoreProperties("articles")
    private CategorieArticle categorieArticle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mesure_id")
    @JsonIgnoreProperties("articles")
    private Mesure mesure;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entreprise_id")
    @JsonIgnore
    private Entreprise entreprise;
}