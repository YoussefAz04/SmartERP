package BuissnesManager.BuissnesManager.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ArticleDto {
    private Long id;
    private String reference;
    private String nom;
    private String description;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private BigDecimal tva;
    private boolean actif;
    private Long categorieArticleId;
    private Long mesureId;
    private Long entrepriseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }

    public BigDecimal getPrixVente() { return prixVente; }
    public void setPrixVente(BigDecimal prixVente) { this.prixVente = prixVente; }

    public BigDecimal getTva() { return tva; }
    public void setTva(BigDecimal tva) { this.tva = tva; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Long getCategorieArticleId() { return categorieArticleId; }
    public void setCategorieArticleId(Long categorieArticleId) { this.categorieArticleId = categorieArticleId; }

    public Long getMesureId() { return mesureId; }
    public void setMesureId(Long mesureId) { this.mesureId = mesureId; }

    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
