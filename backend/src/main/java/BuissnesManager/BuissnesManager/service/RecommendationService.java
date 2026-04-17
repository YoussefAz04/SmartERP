package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.RecommendationDto;
import BuissnesManager.BuissnesManager.repository.CommandeAchatRepository;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private CommandeAchatRepository commandeAchatRepository;

    public List<RecommendationDto> generateRecommendations(Long entrepriseId) {
        List<RecommendationDto> recommendations = new ArrayList<>();

        // 1. Stock alerts
        Long lowStockCount = stockRepository.getLowStockItemsCount(entrepriseId);
        Long outOfStockCount = stockRepository.getOutOfStockItemsCount(entrepriseId);

        if (outOfStockCount != null && outOfStockCount > 0) {
            recommendations.add(new RecommendationDto(
                "STOCK_CRITIQUE",
                "Rupture de Stock Détectée",
                outOfStockCount + " article(s) en rupture de stock. Passez des commandes d'achat immédiatement pour éviter des pertes de ventes.",
                "CRITIQUE",
                "fa-exclamation-triangle",
                "Voir Stock"
            ));
        }

        if (lowStockCount != null && lowStockCount > 0) {
            recommendations.add(new RecommendationDto(
                "STOCK_FAIBLE",
                "Stock Faible - Action Requise",
                lowStockCount + " article(s) ont atteint le seuil d'alerte. Anticipez vos approvisionnements pour maintenir la continuité.",
                "HAUTE",
                "fa-boxes",
                "Gérer Stock"
            ));
        }

        // 2. Sales performance recommendations
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        Double salesThisMonth = commandeVenteRepository.getSalesInPeriod(entrepriseId, startOfMonth, endOfMonth);
        Double totalSales = commandeVenteRepository.getTotalSalesRevenue(entrepriseId);

        if (salesThisMonth != null && totalSales != null && totalSales > 0) {
            double monthShare = (salesThisMonth / totalSales) * 100;
            if (monthShare < 5.0) {
                recommendations.add(new RecommendationDto(
                    "VENTES_FAIBLES",
                    "Performance des Ventes ce Mois",
                    "Les ventes de ce mois représentent seulement " + Math.round(monthShare * 10) / 10.0 + "% du total. Envisagez des actions promotionnelles.",
                    "MOYENNE",
                    "fa-chart-line",
                    "Analyser Ventes"
                ));
            }
        }

        // 3. Top products recommendation
        List<Object[]> topProducts = commandeVenteRepository.getTopProducts(entrepriseId);
        if (topProducts != null && !topProducts.isEmpty()) {
            String topProduct = topProducts.get(0)[0] != null ? topProducts.get(0)[0].toString() : "N/A";
            recommendations.add(new RecommendationDto(
                "PRODUIT_STAR",
                "Optimisation Produit Vedette",
                "\"" + topProduct + "\" est votre produit le plus vendu. Assurez un stock suffisant et envisagez des offres groupées pour maximiser les revenus.",
                "BASSE",
                "fa-star",
                "Voir Produits"
            ));
        }

        // 4. Purchase optimization – use true gross margin (sales - COGS) so that
        //    inventory build-up isn't mistaken for a loss, and factor in the
        //    realized margin this year when sales history is substantial.
        Double totalPurchases = commandeAchatRepository.getTotalPurchases(entrepriseId);
        Double cogsAllTime = commandeVenteRepository.getCOGSInPeriod(
                entrepriseId, LocalDate.of(1970, 1, 1), now);
        double cogs = cogsAllTime != null ? cogsAllTime : 0.0;
        if (totalSales != null && totalSales > 0 && cogs > 0) {
            double margin = ((totalSales - cogs) / totalSales) * 100.0;
            double marginRounded = Math.round(margin * 10) / 10.0;
            if (margin < 20.0) {
                recommendations.add(new RecommendationDto(
                    "MARGE_FAIBLE",
                    "Amélioration de la Marge Commerciale",
                    "Votre marge commerciale brute est de " + marginRounded + "%. Négociez de meilleures conditions avec vos fournisseurs ou réévaluez vos prix de vente.",
                    "HAUTE",
                    "fa-percentage",
                    "Analyser Achats"
                ));
            } else if (margin < 35.0) {
                recommendations.add(new RecommendationDto(
                    "MARGE_CORRECTE",
                    "Marge Commerciale Acceptable",
                    "Votre marge commerciale brute de " + marginRounded + "% est correcte. Un objectif >35% sécuriserait davantage vos résultats.",
                    "INFO",
                    "fa-percentage",
                    "Voir Détails"
                ));
            } else {
                recommendations.add(new RecommendationDto(
                    "BONNE_PERFORMANCE",
                    "Excellente Marge Commerciale",
                    "Votre marge commerciale brute de " + marginRounded + "% est saine. Continuez à surveiller les coûts d'approvisionnement.",
                    "INFO",
                    "fa-thumbs-up",
                    "Voir Détails"
                ));
            }
        } else if (totalSales != null && totalPurchases != null && totalSales > 0 && totalPurchases > 0) {
            // Fallback when we can't compute COGS (e.g. articles without prix_achat):
            // at least surface a purchases-vs-sales ratio so the user has feedback.
            double ratio = (totalPurchases / totalSales) * 100.0;
            recommendations.add(new RecommendationDto(
                "RATIO_ACHATS",
                "Ratio Achats/Ventes",
                "Vos achats représentent " + Math.round(ratio * 10) / 10.0 + "% de vos ventes. Renseignez le prix d'achat de vos articles pour obtenir une marge commerciale précise.",
                "INFO",
                "fa-balance-scale",
                "Voir Détails"
            ));
        }

        // 5. Data-driven delivery quality recommendation
        Long overdueOrders = commandeAchatRepository.getOverdueOrdersCount(entrepriseId, now);
        Long activeOrders = commandeAchatRepository.getActiveOrdersCount(entrepriseId);
        if (overdueOrders != null && overdueOrders > 0) {
            String urgency = overdueOrders > 5 ? "HAUTE" : "MOYENNE";
            String icon = overdueOrders > 5 ? "fa-exclamation-circle" : "fa-clock";
            recommendations.add(new RecommendationDto(
                "LIVRAISON_RETARD",
                "Commandes en Retard de Livraison",
                overdueOrders + " commande(s) d'achat dépassent leur date de livraison prévue. Contactez vos fournisseurs pour suivre ces livraisons.",
                urgency,
                icon,
                "Voir Commandes"
            ));
        } else if (activeOrders != null && activeOrders > 0) {
            recommendations.add(new RecommendationDto(
                "LIVRAISON_OK",
                "Livraisons à Jour",
                "Toutes vos commandes d'achat actives (" + activeOrders + ") respectent leurs délais de livraison prévus.",
                "INFO",
                "fa-truck",
                "Voir Commandes"
            ));
        }

        // 6. Always add a data quality recommendation if little data
        Long totalOrders = commandeVenteRepository.getTotalSalesOrders(entrepriseId);
        if (totalOrders != null && totalOrders < 10) {
            recommendations.add(new RecommendationDto(
                "DONNEES_INSUFFISANTES",
                "Enrichissez Vos Données",
                "Vous avez seulement " + totalOrders + " commande(s) enregistrée(s). Plus vous enregistrez de transactions, plus les analyses IA seront précises.",
                "INFO",
                "fa-database",
                "Ajouter Commandes"
            ));
        }

        return recommendations;
    }
}
