package BuissnesManager.BuissnesManager.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAchatResponseDto {

    private Long id;
    private String reference;
    private String nom;
    private String description;
    private BigDecimal prixUnitaire;
    private Integer quantite;
    private String sku;
    private String unite;
    private boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private FournisseurInfo fournisseur;
    private CategorieArticleInfo categorieArticle;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FournisseurInfo {
        private Long id;
        private String nom;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorieArticleInfo {
        private Long id;
        private String nom;
    }
}
