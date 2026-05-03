package BuissnesManager.BuissnesManager.dto;

import BuissnesManager.BuissnesManager.entity.FactureDeVente;

import java.util.ArrayList;
import java.util.List;

/**
 * Response payload returned by {@code PUT /api/commandes-vente/{id}/valider}.
 *
 * <p>Wraps the generated or updated {@link FactureDeVente} together with any
 * stock alerts (rupture / faible stock) raised at reservation time — i.e. when
 * an article's available stock drops below 10 (or below its configured
 * {@code seuilMinimum}) after the order quantities are reserved.</p>
 */
public class ValidationResponseDto {

    private FactureDeVente facture;
    private List<StockAlertDto> stockAlerts = new ArrayList<>();

    public ValidationResponseDto() {
    }

    public ValidationResponseDto(FactureDeVente facture, List<StockAlertDto> stockAlerts) {
        this.facture = facture;
        this.stockAlerts = stockAlerts != null ? stockAlerts : new ArrayList<>();
    }

    public FactureDeVente getFacture() { return facture; }
    public void setFacture(FactureDeVente facture) { this.facture = facture; }

    public List<StockAlertDto> getStockAlerts() { return stockAlerts; }
    public void setStockAlerts(List<StockAlertDto> stockAlerts) {
        this.stockAlerts = stockAlerts != null ? stockAlerts : new ArrayList<>();
    }
}
