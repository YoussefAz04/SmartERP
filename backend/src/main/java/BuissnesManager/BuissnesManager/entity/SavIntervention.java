package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entrée dans le journal d'un ticket SAV : commentaire, changement de statut,
 * intervention technique, etc. Permet d'afficher une timeline sur la fiche SAV.
 */
@Entity
@Table(name = "sav_intervention")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** COMMENTAIRE, CHANGEMENT_STATUT, INTERVENTION, PIECE, CLOTURE */
    @Column(length = 30)
    private String type;

    /** Snapshot of the status that was set when this event was logged (optional). */
    @Column(length = 30)
    private String statut;

    @Column(length = 2000)
    private String commentaire;

    /** Free-text author name (username). Kept simple to avoid extra FK. */
    @Column(length = 150)
    private String auteur;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sav_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "interventions"})
    private Sav sav;
}
