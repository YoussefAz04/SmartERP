package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CaracteristiqueFactureEntreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id ;

    private String fontName;
    private String fontStyle;



    private Long fillColorR ;
    private Long fillColorG ;
    private Long fillColorB ;

    private Long textColorR ;
    private Long textColorG ;
    private Long textColorB ;

    private Long colorHeaderR ;
    private Long colorHeaderG ;
    private Long colorHeaderB ;

    private Long colorFooterR ;
    private Long colorFooterG ;
    private Long colorFooterB ;

    private String messageFooter ;



    @OneToOne
    @JsonIgnoreProperties(value = "caracteristiqueFactureEntreprise")
    private Entreprise entreprise ;



}
