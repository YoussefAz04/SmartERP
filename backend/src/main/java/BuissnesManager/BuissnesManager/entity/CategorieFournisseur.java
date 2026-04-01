package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categorie_fournisseur")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategorieFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nom;

    private String description;

    @OneToMany(mappedBy = "categorieFournisseur")
    @JsonIgnoreProperties("categorieFournisseur")
    private List<Fournisseur> fournisseurs;
}