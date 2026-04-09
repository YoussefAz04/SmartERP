package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← Change from AUTO to IDENTITY
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String secteurActivite;

    @ManyToOne
    @JsonIgnore
    private CategorieClient categorieClient;

    // Primary enterprise association (entreprise_id column) used for enterprise-scoped queries
    @ManyToOne
    @JoinColumn(name = "entreprise_id")
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