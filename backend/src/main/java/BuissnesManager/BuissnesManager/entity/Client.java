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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String secteurActivite;

    @ManyToOne
    @JsonIgnore
    private CategorieClient categorieClient ;


    @ManyToMany
    @JsonIgnore
    private List<Entreprise> entreprises ;

    @OneToMany(mappedBy = "client")
    @JsonIgnoreProperties(value = "client")
    private List<FactureDeVente> factureDeVentes ;

}
