package BuissnesManager.BuissnesManager.dto;

import java.time.LocalDateTime;

public class StockDto {
    private Long id;
    private Integer quantiteEntree;
    private Integer quantiteDisponible;
    private Integer quantiteReserve;
    private Integer quantiteVendue;
    private String localisation;
    private LocalDateTime dateDernierInventaire;
    private Long articleAchatId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getQuantiteEntree() { return quantiteEntree; }
    public void setQuantiteEntree(Integer quantiteEntree) { this.quantiteEntree = quantiteEntree; }

    public Integer getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(Integer quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }

    public Integer getQuantiteReserve() { return quantiteReserve; }
    public void setQuantiteReserve(Integer quantiteReserve) { this.quantiteReserve = quantiteReserve; }

    public Integer getQuantiteVendue() { return quantiteVendue; }
    public void setQuantiteVendue(Integer quantiteVendue) { this.quantiteVendue = quantiteVendue; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public LocalDateTime getDateDernierInventaire() { return dateDernierInventaire; }
    public void setDateDernierInventaire(LocalDateTime dateDernierInventaire) { this.dateDernierInventaire = dateDernierInventaire; }

    public Long getArticleAchatId() { return articleAchatId; }
    public void setArticleAchatId(Long articleAchatId) { this.articleAchatId = articleAchatId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
