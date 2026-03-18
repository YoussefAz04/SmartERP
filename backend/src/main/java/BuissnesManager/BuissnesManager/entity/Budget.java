package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private Double montant;

    private LocalDate dateDebut;

    private LocalDate dateFin;


    @Column(unique = true)
    private Long idProjet ;


    @OneToMany(mappedBy = "budget")
    @JsonIgnoreProperties(value = "budget")
    private List<Depense> depenses ;

}
