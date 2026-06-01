package BuissnesManager.BuissnesManager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandeVenteResponseDto {

    private Long id;
    private String reference;
    private LocalDate dateCommande;
    private LocalDate dateLivraisonPrevue;
    private String statut;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ClientInfo client;
    private List<LigneInfo> lignes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private Long id;
        private String nom;
        private String prenom;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneInfo {
        private Long id;
        private BigDecimal quantite;
        private BigDecimal prixUnitaireHT;
        private BigDecimal tva;
        private BigDecimal montantHT;
        private BigDecimal montantTTC;
        private ArticleVenteInfo articleVente;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleVenteInfo {
        private Long id;
        private String nom;
    }
}
