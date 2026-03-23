package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(precision = 15, scale = 2)
    private BigDecimal prixAchat;

    @Column(precision = 15, scale = 2)
    private BigDecimal prixVente;

    @Column(precision = 5, scale = 2)
    private BigDecimal tva;

    private boolean actif = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "categorie_article_id")
    @JsonIgnoreProperties("articles")
    private CategorieArticle categorieArticle;

    @ManyToOne
    @JoinColumn(name = "mesure_id")
    @JsonIgnoreProperties("articles")
    private Mesure mesure;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    @JsonIgnore
    private Entreprise entreprise;

    @OneToMany(mappedBy = "article")
    @JsonIgnore
    private List<Stock> stocks;
}
