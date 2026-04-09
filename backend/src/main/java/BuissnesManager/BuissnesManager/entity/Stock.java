package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
    private Article articleAchat;
}