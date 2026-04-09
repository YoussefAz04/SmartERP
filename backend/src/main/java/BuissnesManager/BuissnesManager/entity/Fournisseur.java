package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private String pays;

    @Column(name = "code_postal")
    private String codePostal;

    @Column(name = "numero_identification_fiscale")
    private String numeroIdentificationFiscale;

    @Column(name = "registre_commerce")
    private String registreCommerce;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_fournisseur_id")
    @JsonIgnoreProperties({"fournisseurs", "hibernateLazyInitializer", "handler"})
    private CategorieFournisseur categorieFournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    @JsonIgnoreProperties({"fournisseurs", "hibernateLazyInitializer", "handler"})
    private Entreprise entreprise;
}