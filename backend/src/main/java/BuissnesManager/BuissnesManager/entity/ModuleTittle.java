package BuissnesManager.BuissnesManager.entity;

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
public class ModuleTittle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String  moduleTittle ;

    @OneToMany(mappedBy = "moduleTittle")
    private List<Module> modules ;
}
