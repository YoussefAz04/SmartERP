package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CategorieClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ CHANGE FROM AUTO TO IDENTITY
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "categorieClient")
    @JsonIgnore
    private List<Client> clients;
}