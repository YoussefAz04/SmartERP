package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class ZoneSelectionneeDeDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String typeDeDocument;
    private float x;
    private float y;
    private float width;
    private float height;
    private int pageNumber;
    private float canvasWidth;
    private float canvasHeight;
    private float pdfWidth;
    private float pdfHeight;



}
