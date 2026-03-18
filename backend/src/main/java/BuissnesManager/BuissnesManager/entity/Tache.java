package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutTache statut;

    @Enumerated(EnumType.STRING)
    private PrioriteDeTache priorite;

    @ManyToOne
    @JsonIgnoreProperties("taches")
    private Projet projet;

    @ManyToOne
    @JsonIgnoreProperties("taches")
    private CategorieTache categorieTache;

    @OneToMany(mappedBy = "tache")
    @JsonIgnoreProperties("tache")
    private List<SousTache> sousTaches ;

    private String predecesseur ;
    private Long duree ;
    private  Long progres ;




    @ManyToOne
    @JsonIgnoreProperties("taches")
    private Membre membre ;



}
