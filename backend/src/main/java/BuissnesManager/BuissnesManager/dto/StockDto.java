package BuissnesManager.BuissnesManager.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockDto {
    private Long id;
    private Long articleId;
    private String articleNom;
    private Long entrepriseId;
    private BigDecimal quantiteDisponible;
    private BigDecimal quantiteMinimale;
    private BigDecimal quantiteMaximale;
    private String emplacement;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleNom() { return articleNom; }
    public void setArticleNom(String articleNom) { this.articleNom = articleNom; }

    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }

    public BigDecimal getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(BigDecimal quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }

    public BigDecimal getQuantiteMinimale() { return quantiteMinimale; }
    public void setQuantiteMinimale(BigDecimal quantiteMinimale) { this.quantiteMinimale = quantiteMinimale; }

    public BigDecimal getQuantiteMaximale() { return quantiteMaximale; }
    public void setQuantiteMaximale(BigDecimal quantiteMaximale) { this.quantiteMaximale = quantiteMaximale; }

    public String getEmplacement() { return emplacement; }
    public void setEmplacement(String emplacement) { this.emplacement = emplacement; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
