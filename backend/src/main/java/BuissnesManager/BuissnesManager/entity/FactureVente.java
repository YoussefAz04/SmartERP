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
@Table(name = "facture_de_vente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FactureVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String referance;

    @Column(name = "date_achat")
    private String dateVente;

    @Column(name = "montantht", precision = 15, scale = 2)
    private BigDecimal montantHT;

    @Column(name = "montanttva", precision = 15, scale = 2)
    private BigDecimal montantTVA;

    @Column(name = "montantttc", precision = 15, scale = 2)
    private BigDecimal montantTTC;

    @Column(precision = 15, scale = 2)
    private BigDecimal remise;

    @Column(name = "mode_paiement")
    private String modePaiement;

    private String statut;

    private String description;

    @Column(name = "chemin_fichier")
    private String cheminFichier;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties("factureVentes")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "commande_vente_id")
    @JsonIgnoreProperties("factureVentes")
    private CommandeVente commandeVente;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    @JsonIgnore
    private Entreprise entreprise;

    @OneToMany(mappedBy = "factureVente")
    @JsonIgnoreProperties("factureVente")
    private List<LigneFactureVente> lignes;
}
