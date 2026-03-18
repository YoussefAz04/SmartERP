package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id ;

    private String title;

    private boolean status = false;

    @ManyToOne
    @JsonIgnore
    private ModuleTittle moduleTittle ;

    @OneToMany(mappedBy = "module",cascade = CascadeType.ALL)
    private List<SousModule> sousModules ;


    @OneToMany(mappedBy = "module")
    private List<Permission> permissions ;

}
