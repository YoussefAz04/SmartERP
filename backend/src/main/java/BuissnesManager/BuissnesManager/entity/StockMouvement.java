package BuissnesManager.BuissnesManager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_mouvement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMouvement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false)
    private TypeMouvement typeMouvement;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantite;

    @Column(name = "reference_commande")
    private String referenceCommande;

    private String motif;

    @Column(name = "date_mouvement")
    private LocalDateTime dateMouvement;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;
}
