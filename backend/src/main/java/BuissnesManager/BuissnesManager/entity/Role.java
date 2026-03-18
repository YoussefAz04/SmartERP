package BuissnesManager.BuissnesManager.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    @Column(unique = true)
    private String name;

    private LocalDate createdOn ;

    @OneToMany(mappedBy = "role" )
    private List<Permission> permission;

}
