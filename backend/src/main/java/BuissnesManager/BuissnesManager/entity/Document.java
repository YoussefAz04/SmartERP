package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String type;


    private LocalDateTime dateAjout;

    @ManyToOne()
    @JsonIgnoreProperties("documents")
    private CategorieDocument categorieDocument ;


    @OneToOne()
    @JsonIgnoreProperties("document")
    private FileChemin fileChemin ;
}
