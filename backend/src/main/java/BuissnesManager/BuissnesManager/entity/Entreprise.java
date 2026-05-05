package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Entreprise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ CHANGE FROM AUTO TO IDENTITY
    private Long id;
    
    private String nom;
    private String raisonSociale;
    private String description;
    private String secteurActivite;
    private String statutJuridique;
    private String numeroIdentificationFiscale;
    private String registreCommerce;
    private String siteWeb;
    private LocalDate dateCreation;
    private LocalDateTime dateDerniereModification;
    private String email;
    private String adresse;
    private String Telephone;

    @Column(nullable = false)
    @ColumnDefault("1")
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "entreprise", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "entreprise", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Client> clients = new ArrayList<>();

    @OneToMany(mappedBy = "entreprise", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Fournisseur> fournisseurs = new ArrayList<>();

    @OneToMany(mappedBy = "entreprise", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Article> articles = new ArrayList<>();

    @OneToMany(mappedBy = "entreprise", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CommandeAchat> commandesAchat = new ArrayList<>();

    @OneToMany(mappedBy = "entreprise", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CommandeVente> commandesVente = new ArrayList<>();

    @OneToMany(mappedBy = "entreprise", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FactureAchat> facturesAchat = new ArrayList<>();

    @OneToOne(mappedBy = "entreprise")
    @JsonIgnoreProperties(value = "entreprise")
    private CaracteristiqueFactureEntreprise caracteristiqueFactureEntreprise;
}