package BuissnesManager.BuissnesManager.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FactureAchatDto {
    private Long id;
    private String referance;
    private String dateAchat;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal remise;
    private String modePaiement;
    private String statut;
    private String description;
    private Long fournisseurId;
    private Long commandeAchatId;
    private Long entrepriseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferance() { return referance; }
    public void setReferance(String referance) { this.referance = referance; }

    public String getDateAchat() { return dateAchat; }
    public void setDateAchat(String dateAchat) { this.dateAchat = dateAchat; }

    public BigDecimal getMontantHT() { return montantHT; }
    public void setMontantHT(BigDecimal montantHT) { this.montantHT = montantHT; }

    public BigDecimal getMontantTVA() { return montantTVA; }
    public void setMontantTVA(BigDecimal montantTVA) { this.montantTVA = montantTVA; }

    public BigDecimal getMontantTTC() { return montantTTC; }
    public void setMontantTTC(BigDecimal montantTTC) { this.montantTTC = montantTTC; }

    public BigDecimal getRemise() { return remise; }
    public void setRemise(BigDecimal remise) { this.remise = remise; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(Long fournisseurId) { this.fournisseurId = fournisseurId; }

    public Long getCommandeAchatId() { return commandeAchatId; }
    public void setCommandeAchatId(Long commandeAchatId) { this.commandeAchatId = commandeAchatId; }

    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
