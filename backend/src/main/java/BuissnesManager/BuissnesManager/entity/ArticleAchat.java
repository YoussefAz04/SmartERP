package BuissnesManager.BuissnesManager.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Plain DTO/POJO representing an article achat.
 * Not a JPA entity – backed by the Article entity mapped to the "article" table.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAchat {

    private Long id;
    private String reference;
    private String nom;
    private String description;
    private BigDecimal prixUnitaire;
    private BigDecimal prixVente;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}