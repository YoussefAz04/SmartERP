package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket SAV (Service Après-Vente) — represents a single after-sales request
 * (claim, repair, warranty, return, exchange) raised by a client.
 */
@Entity
@Table(name = "sav")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sav {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String reference;

    @Column(name = "date_ouverture")
    private LocalDate dateOuverture;

    @Column(name = "date_cloture")
    private LocalDate dateCloture;

    /** GARANTIE, REPARATION, RETOUR, ECHANGE, RECLAMATION */
    @Column(length = 30)
    private String type;

    /** BASSE, NORMALE, HAUTE, URGENTE */
    @Column(length = 20)
    private String priorite;

    /** NOUVEAU, EN_COURS, EN_ATTENTE_PIECE, RESOLU, CLOTURE, ANNULE */
    @Column(length = 30)
    private String statut;

    @Column(length = 200)
    private String sujet;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String resolution;

    @Column(name = "cout_estime", precision = 15, scale = 2)
    private BigDecimal coutEstime;

    @Column(name = "cout_reel", precision = 15, scale = 2)
    private BigDecimal coutReel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "factureDeVentes", "entreprise", "entreprises"})
    private Client client;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    @JsonIgnore
    private Entreprise entreprise;

    @ManyToOne
    @JoinColumn(name = "facture_vente_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "client", "entreprise", "detailsFactureDeVentes"})
    private FactureDeVente factureDeVente;

    @ManyToOne
    @JoinColumn(name = "article_vente_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "articleAchat", "entreprise"})
    private ArticleVente articleVente;

    @ManyToOne
    @JoinColumn(name = "technicien_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles", "permissions", "entreprise", "entreprises"})
    private User technicien;

    @OneToMany(mappedBy = "sav", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @JsonIgnoreProperties({"sav"})
    private List<SavIntervention> interventions = new ArrayList<>();
}
