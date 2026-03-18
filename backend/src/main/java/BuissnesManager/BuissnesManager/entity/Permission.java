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
@NoArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;
    @Column(name = "can_create")
    private boolean create;

    @Column(name = "can_delete")
    private boolean delete;

    @Column(name = "can_update")
    private boolean update;

    @Column(name = "can_view")
    private boolean view;



     @ManyToOne
     @JsonIgnore
     private Module module ;
     @JsonIgnore
     @ManyToOne()
     private Role role ;





}
