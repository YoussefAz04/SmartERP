package BuissnesManager.BuissnesManager.dto;

import java.math.BigDecimal;

public class LigneCommandeVenteDto {
    private Long articleVenteId;
    private BigDecimal quantite;
    private BigDecimal prixUnitaireHT;
    private BigDecimal tva;
    private BigDecimal montantHT;
    private BigDecimal montantTTC;

    public Long getArticleVenteId() { return articleVenteId; }
    public void setArticleVenteId(Long articleVenteId) { this.articleVenteId = articleVenteId; }

    public BigDecimal getQuantite() { return quantite; }
    public void setQuantite(BigDecimal quantite) { this.quantite = quantite; }

    public BigDecimal getPrixUnitaireHT() { return prixUnitaireHT; }
    public void setPrixUnitaireHT(BigDecimal prixUnitaireHT) { this.prixUnitaireHT = prixUnitaireHT; }

    public BigDecimal getTva() { return tva; }
    public void setTva(BigDecimal tva) { this.tva = tva; }

    public BigDecimal getMontantHT() { return montantHT; }
    public void setMontantHT(BigDecimal montantHT) { this.montantHT = montantHT; }

    public BigDecimal getMontantTTC() { return montantTTC; }
    public void setMontantTTC(BigDecimal montantTTC) { this.montantTTC = montantTTC; }
}
