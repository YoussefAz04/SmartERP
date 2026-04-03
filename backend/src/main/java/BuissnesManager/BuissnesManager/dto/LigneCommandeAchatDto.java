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
    
    private BigDecimal quantite;
    private BigDecimal prixUnitaireHT;
    private BigDecimal tva;
    private BigDecimal montantHT;
    private BigDecimal montantTTC;
}