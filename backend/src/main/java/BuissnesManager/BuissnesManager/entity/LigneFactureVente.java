package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ligne_facture_vente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String designation;

    @Column(precision = 15, scale = 2)
    private BigDecimal quantite;

    @Column(precision = 15, scale = 2)
    private BigDecimal prixUnitaireHT;

    @Column(precision = 5, scale = 2)
    private BigDecimal tva;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantHT;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantTTC;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "facture_vente_id")
    @JsonIgnore
    private FactureVente factureVente;
}
