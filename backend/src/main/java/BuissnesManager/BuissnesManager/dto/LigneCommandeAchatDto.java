package BuissnesManager.BuissnesManager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeAchatDto {
    
    @JsonProperty("articleAchatId")
    private Long articleAchatId;
    
    @JsonProperty("quantite")
    private BigDecimal quantite;

    @JsonProperty("prixUnitaireHT")
    private BigDecimal prixUnitaireHT;

    @JsonProperty("tva")
    private BigDecimal tva;

    @JsonProperty("montantHT")
    private BigDecimal montantHT;

    @JsonProperty("montantTTC")
    private BigDecimal montantTTC;
}