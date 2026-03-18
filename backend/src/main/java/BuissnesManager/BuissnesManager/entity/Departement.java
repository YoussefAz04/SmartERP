package BuissnesManager.BuissnesManager.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Departement {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String description;

    private String responsable;

    private String email;

    private String telephone;

    private Integer nombreEmployes;


    @ManyToOne
    @JsonIgnore
    private Entreprise entreprise ;
}
