package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Membre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    private String membreTitre ;
    private String description  ;
    private String specialite  ;

    @ManyToMany()
    @JsonIgnore
    private List<User> users;

    @ManyToOne
    @JsonIgnore
    private Equipe equipe ;

    @OneToMany
    @JsonIgnoreProperties("membre")
    private List<Tache> taches ;

}
