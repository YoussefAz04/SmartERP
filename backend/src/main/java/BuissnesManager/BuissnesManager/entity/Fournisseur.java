package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "categorie_fournisseur_id")
    @JsonIgnoreProperties("fournisseurs")
    private CategorieFournisseur categorieFournisseur;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    @JsonIgnoreProperties("fournisseurs")
    private Entreprise entreprise;
}