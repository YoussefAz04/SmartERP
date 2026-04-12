package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nom is required")
    @Column(nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "Prenom is required")
    @Column(nullable = false, length = 100)
    private String prenom;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank(message = "Telephone is required")
    @Column(nullable = false, length = 20)
    private String telephone;

    @NotBlank(message = "Adresse is required")
    @Column(nullable = false, length = 255)
    private String adresse;

    @NotBlank(message = "Secteur activite is required")
    @Column(nullable = false, length = 100)
    private String secteurActivite;

    // Properly mapped category relationship
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_client_id", nullable = false)
    @NotNull(message = "Category is required")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "clients"})
    private CategorieClient categorieClient;

    // Primary enterprise association (entreprise_id column) used for enterprise-scoped queries
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entreprise_id", nullable = false)
    @NotNull(message = "Enterprise is required")
    @JsonIgnore
    private Entreprise entreprise;

    // Legacy many-to-many kept for backward compatibility with the client_entreprises junction table
    @ManyToMany
    @JsonIgnore
    private List<Entreprise> entreprises;

    @OneToMany(mappedBy = "client")
    @JsonIgnoreProperties(value = "client")
    private List<FactureDeVente> factureDeVentes;
}