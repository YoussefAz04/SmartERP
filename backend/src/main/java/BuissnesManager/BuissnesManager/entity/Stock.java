package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(name = "quantite_entree")
    private Integer quantiteEntree;

    @Column(name = "quantite_disponible")
    private Integer quantiteDisponible;

    @Column(name = "quantite_reserve")
    private Integer quantiteReserve;

    @Column(name = "quantite_vendue")
    private Integer quantiteVendue;

    private String localisation;

    @Column(name = "date_dernier_inventaire")
    private LocalDateTime dateDernierInventaire;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "article_achat_id", nullable = false)
    @JsonIgnoreProperties("stocks")
    private ArticleAchat articleAchat;

    @ManyToOne
    @JoinColumn(name = "article_vente_id")
    @JsonIgnoreProperties("stocks")
    private ArticleVente articleVente;
}
