package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SousModule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String titreSousModule ;
    private String link ;


    @ManyToOne
    @JsonIgnore
    private Module module ;

}
