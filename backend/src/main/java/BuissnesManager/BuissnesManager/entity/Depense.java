package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Depense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    private String description;
    private Double montant;
    private LocalDate dateDepense;


    @ManyToOne()
    @JsonIgnoreProperties(value = "depense")
    private Budget budget ;

    @ManyToOne
    @JsonIgnoreProperties(value = "depense")
    private CategorieDepense categorieDepense ;

}
