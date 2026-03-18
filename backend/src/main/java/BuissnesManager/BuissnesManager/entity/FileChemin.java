package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileChemin {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    private String  urLPdf ;


    @OneToOne(mappedBy = "fileChemin")
    @JsonIgnoreProperties("fileChemin")
    private Document document ;
}
