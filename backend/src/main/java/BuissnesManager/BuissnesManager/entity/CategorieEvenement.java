package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategorieEvenement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ColorCategorie colorCategorie;

    @OneToMany()
    @JsonIgnoreProperties("categorieEvenement")
    private List<Evenement> evenements ;

}
