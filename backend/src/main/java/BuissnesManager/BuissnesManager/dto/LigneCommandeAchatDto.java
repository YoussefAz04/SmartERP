package BuissnesManager.BuissnesManager.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LigneCommandeAchatDto {
    
    @JsonProperty("articleAchatId")  // ✅ MAP THE FIELD CORRECTLY
    private Long articleAchatId;
    
    private BigDecimal quantite;
    private BigDecimal prixUnitaireHT;
    private BigDecimal tva;
    private BigDecimal montantHT;
    private BigDecimal montantTTC;
    
    // Getters and Setters
    public Long getArticleAchatId() {
        return articleAchatId;
    }
    
    public void setArticleAchatId(Long articleAchatId) {
        this.articleAchatId = articleAchatId;
    }
    
    public BigDecimal getQuantite() {
        return quantite;
    }
    
    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
    }
    
    public BigDecimal getPrixUnitaireHT() {
        return prixUnitaireHT;
    }
    
    public void setPrixUnitaireHT(BigDecimal prixUnitaireHT) {
        this.prixUnitaireHT = prixUnitaireHT;
    }
    
    public BigDecimal getTva() {
        return tva;
    }
    
    public void setTva(BigDecimal tva) {
        this.tva = tva;
    }
    
    public BigDecimal getMontantHT() {
        return montantHT;
    }
    
    public void setMontantHT(BigDecimal montantHT) {
        this.montantHT = montantHT;
    }
    
    public BigDecimal getMontantTTC() {
        return montantTTC;
    }
    
    public void setMontantTTC(BigDecimal montantTTC) {
        this.montantTTC = montantTTC;
    }
    
    @Override
    public String toString() {
        return "LigneCommandeAchatDto{" +
                "articleAchatId=" + articleAchatId +
                ", quantite=" + quantite +
                ", prixUnitaireHT=" + prixUnitaireHT +
                ", tva=" + tva +
                ", montantHT=" + montantHT +
                ", montantTTC=" + montantTTC +
                '}';
    }
}