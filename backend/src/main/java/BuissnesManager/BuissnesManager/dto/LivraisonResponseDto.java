package BuissnesManager.BuissnesManager.dto;

import BuissnesManager.BuissnesManager.entity.BonLivraison;

import java.util.ArrayList;
import java.util.List;

/**
 * Response payload returned by {@code PUT /api/commandes-vente/{id}/livrer}.
 *
 * <p>Wraps the generated {@link BonLivraison} together with any stock
 * alerts (rupture / faible stock) that were raised while decrementing
 * stock for the delivery, so the frontend can display them immediately.</p>
 */
public class LivraisonResponseDto {

    private BonLivraison bonLivraison;
    private List<StockAlertDto> stockAlerts = new ArrayList<>();

    public LivraisonResponseDto() {
    }

    public LivraisonResponseDto(BonLivraison bonLivraison, List<StockAlertDto> stockAlerts) {
        this.bonLivraison = bonLivraison;
        this.stockAlerts = stockAlerts != null ? stockAlerts : new ArrayList<>();
    }

    public BonLivraison getBonLivraison() { return bonLivraison; }
    public void setBonLivraison(BonLivraison bonLivraison) { this.bonLivraison = bonLivraison; }

    public List<StockAlertDto> getStockAlerts() { return stockAlerts; }
    public void setStockAlerts(List<StockAlertDto> stockAlerts) {
        this.stockAlerts = stockAlerts != null ? stockAlerts : new ArrayList<>();
    }

    /** Convenience accessor used by the frontend to mirror BonLivraison fields. */
    public String getReference() {
        return bonLivraison != null ? bonLivraison.getReference() : null;
    }
}
