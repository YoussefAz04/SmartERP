package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.OffsetDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Evenement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(name = "start_date")
    private OffsetDateTime start;

    @Column(name = "end_date")
    private OffsetDateTime  end;
    private boolean allDay;

    @ManyToOne()
    @JsonIgnoreProperties("evenements")
    private CategorieEvenement categorieEvenement ;

}
