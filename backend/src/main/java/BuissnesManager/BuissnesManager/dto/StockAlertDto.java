package BuissnesManager.BuissnesManager.dto;

import java.math.BigDecimal;

/**
 * Lightweight payload describing a stock alert raised at the moment
 * stock is decremented (e.g. when a sale is delivered).
 *
 * <p>Used by the frontend to display an immediate red toast when an
 * article's stock falls to {@code 0} ({@code RUPTURE}) or below its
 * configured {@code seuilMinimum} ({@code FAIBLE_STOCK}).</p>
 */
public class StockAlertDto {

    /** "RUPTURE" when quantity reached 0, otherwise "FAIBLE_STOCK". */
    private String type;
    private Long articleId;
    private String articleName;
    private BigDecimal quantiteActuelle;
    private BigDecimal seuilMinimum;
    private String message;

    public StockAlertDto() {
    }

    public StockAlertDto(String type, Long articleId, String articleName,
                         BigDecimal quantiteActuelle, BigDecimal seuilMinimum, String message) {
        this.type = type;
        this.articleId = articleId;
        this.articleName = articleName;
        this.quantiteActuelle = quantiteActuelle;
        this.seuilMinimum = seuilMinimum;
        this.message = message;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleName() { return articleName; }
    public void setArticleName(String articleName) { this.articleName = articleName; }

    public BigDecimal getQuantiteActuelle() { return quantiteActuelle; }
    public void setQuantiteActuelle(BigDecimal quantiteActuelle) { this.quantiteActuelle = quantiteActuelle; }

    public BigDecimal getSeuilMinimum() { return seuilMinimum; }
    public void setSeuilMinimum(BigDecimal seuilMinimum) { this.seuilMinimum = seuilMinimum; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
