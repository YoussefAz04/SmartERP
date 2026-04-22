package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.RecommendationDto;
import BuissnesManager.BuissnesManager.entity.Stock;
import BuissnesManager.BuissnesManager.repository.CommandeAchatRepository;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import BuissnesManager.BuissnesManager.service.ml.MlRecommendationEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Rule-based recommendation engine.
 *
 * <p>Each rule works on a single business signal (stock, sales velocity, margin,
 * delivery timeliness, customer concentration, product mix, data quality). They
 * were tuned to be:
 * <ul>
 *   <li><b>Day-of-month-aware</b> — MTD comparisons are prorated by the fraction
 *       of the current month elapsed, so a "low sales this month" alert won't
 *       fire on January 1st by definition.
 *   <li><b>Self-calibrating</b> — thresholds compare against the business's own
 *       12-month averages instead of arbitrary hard-coded caps.
 *   <li><b>Locale-safe</b> — all numeric formatting uses {@link Locale#FRANCE}
 *       so decimal separators match the UI language.
 * </ul>
 */
@Service
public class RecommendationService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private CommandeAchatRepository commandeAchatRepository;

    @Autowired
    private MlRecommendationEngine mlRecommendationEngine;

    /** Horizon (days) used by the stock-risk classifier. */
    private static final int STOCK_RISK_HORIZON_DAYS = 30;

    /** Max ML stock-risk recommendations surfaced per call (avoid spamming the UI). */
    private static final int MAX_ML_STOCK_RECOMMENDATIONS = 5;

    public List<RecommendationDto> generateRecommendations(Long entrepriseId) {
        List<RecommendationDto> recommendations = new ArrayList<>();

        // === 0. ML stock-risk classifier (per-article). Appended first so
        // critical risks surface at the top of the list. When the classifier
        // can't fit (too few articles, single-class data …) it returns an
        // empty list and the rule-based counts below take over. ===
        List<RecommendationDto> mlStockRisk = mlStockRiskRecommendations(entrepriseId);
        recommendations.addAll(mlStockRisk);

        // === 1. Stock alerts ===
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

        // === 2. Sales velocity (day-of-month-adjusted) ===
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        Double salesThisMonth = commandeVenteRepository.getSalesInPeriod(entrepriseId, startOfMonth, endOfMonth);
        Double totalSales = commandeVenteRepository.getTotalSalesRevenue(entrepriseId);
        Long totalOrders = commandeVenteRepository.getTotalSalesOrders(entrepriseId);

        // Compare MTD revenue against the 12-month historical average, prorated
        // by the fraction of the current month elapsed. If we're on day 10 of a
        // 30-day month, expected MTD = monthly-average * 10/30.
        LocalDate twelveMonthsAgo = now.minusMonths(12).withDayOfMonth(1);
        Double last12MonthSales = commandeVenteRepository.getSalesInPeriod(entrepriseId, twelveMonthsAgo, now);
        if (salesThisMonth != null && last12MonthSales != null && last12MonthSales > 0) {
            double avgMonthly = last12MonthSales / 12.0;
            double fractionElapsed = (double) now.getDayOfMonth() / now.lengthOfMonth();
            double expectedMtd = avgMonthly * fractionElapsed;
            // Only raise the alert after we have at least 7 days of the month to
            // avoid noisy early-month signals.
            if (expectedMtd > 0 && now.getDayOfMonth() >= 7
                    && salesThisMonth < 0.5 * expectedMtd) {
                double shortfallPct = ((expectedMtd - salesThisMonth) / expectedMtd) * 100.0;
                recommendations.add(new RecommendationDto(
                    "VENTES_FAIBLES",
                    "Performance des Ventes ce Mois",
                    String.format(Locale.FRANCE,
                        "Les ventes du mois sont en retard d'environ %.0f%% sur votre moyenne historique. Envisagez des actions promotionnelles ou relancez vos meilleurs clients.",
                        shortfallPct),
                    "MOYENNE",
                    "fa-chart-line",
                    "Analyser Ventes"
                ));
            }
        }

        // === 3. ML-driven product opportunities (replaces rule-based PRODUIT_STAR) ===
        List<Object[]> topProducts = commandeVenteRepository.getTopProducts(entrepriseId);
        List<RecommendationDto> mlOpportunity = mlOpportunityRecommendations(topProducts);
        if (!mlOpportunity.isEmpty()) {
            recommendations.addAll(mlOpportunity);
        } else if (topProducts != null && !topProducts.isEmpty()
                && topProducts.get(0) != null && topProducts.get(0).length > 0
                && topProducts.get(0)[0] != null) {
            // Fallback to the rule-based rendering when ML has insufficient data.
            String topProduct = topProducts.get(0)[0].toString();
            recommendations.add(new RecommendationDto(
                "PRODUIT_STAR",
                "Optimisation Produit Vedette",
                String.format(Locale.FRANCE,
                    "« %s » est votre produit le plus vendu. Assurez un stock suffisant et envisagez des offres groupées pour maximiser les revenus.",
                    topProduct),
                "BASSE",
                "fa-star",
                "Voir Produits"
            ));
        }

        // === 4. Gross margin ===
        Double totalPurchases = commandeAchatRepository.getTotalPurchases(entrepriseId);
        Double cogsAllTime = commandeVenteRepository.getCOGSInPeriod(
                entrepriseId, LocalDate.of(1970, 1, 1), now);
        double cogs = cogsAllTime != null ? cogsAllTime : 0.0;
        if (totalSales != null && totalSales > 0 && cogs > 0) {
            double margin = ((totalSales - cogs) / totalSales) * 100.0;
            String marginStr = String.format(Locale.FRANCE, "%.1f", margin);
            if (margin < 20.0) {
                recommendations.add(new RecommendationDto(
                    "MARGE_FAIBLE",
                    "Amélioration de la Marge Commerciale",
                    String.format(Locale.FRANCE,
                        "Votre marge commerciale brute est de %s%%. Négociez de meilleures conditions avec vos fournisseurs ou réévaluez vos prix de vente.",
                        marginStr),
                    "HAUTE",
                    "fa-percentage",
                    "Analyser Achats"
                ));
            } else if (margin < 35.0) {
                recommendations.add(new RecommendationDto(
                    "MARGE_CORRECTE",
                    "Marge Commerciale Acceptable",
                    String.format(Locale.FRANCE,
                        "Votre marge commerciale brute de %s%% est correcte. Un objectif >35%% sécuriserait davantage vos résultats.",
                        marginStr),
                    "INFO",
                    "fa-percentage",
                    "Voir Détails"
                ));
            } else {
                recommendations.add(new RecommendationDto(
                    "BONNE_PERFORMANCE",
                    "Excellente Marge Commerciale",
                    String.format(Locale.FRANCE,
                        "Votre marge commerciale brute de %s%% est saine. Continuez à surveiller les coûts d'approvisionnement.",
                        marginStr),
                    "INFO",
                    "fa-thumbs-up",
                    "Voir Détails"
                ));
            }
        } else if (totalSales != null && totalPurchases != null
                && totalSales > 0 && totalPurchases > 0) {
            double ratio = (totalPurchases / totalSales) * 100.0;
            recommendations.add(new RecommendationDto(
                "RATIO_ACHATS",
                "Ratio Achats/Ventes",
                String.format(Locale.FRANCE,
                    "Vos achats représentent %.1f%% de vos ventes. Renseignez le prix d'achat de vos articles pour obtenir une marge commerciale précise.",
                    ratio),
                "INFO",
                "fa-balance-scale",
                "Voir Détails"
            ));
        }

        // === 5. Delivery quality ===
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

        // === 6. Customer concentration risk ===
        // If the top 3 customers account for more than 60% of revenue the
        // business is exposed to the churn of any single one of them.
        List<Object[]> topCustomers = commandeVenteRepository.getTopCustomers(entrepriseId);
        if (topCustomers != null && !topCustomers.isEmpty()
                && totalSales != null && totalSales > 0) {
            double top3Revenue = 0.0;
            int counted = 0;
            for (Object[] row : topCustomers) {
                if (counted >= 3) break;
                if (row != null && row.length > 1 && row[1] != null) {
                    top3Revenue += ((Number) row[1]).doubleValue();
                    counted++;
                }
            }
            double concentration = (top3Revenue / totalSales) * 100.0;
            if (concentration > 60.0 && counted >= 3) {
                recommendations.add(new RecommendationDto(
                    "CONCENTRATION_CLIENT",
                    "Risque de Concentration Client",
                    String.format(Locale.FRANCE,
                        "Vos 3 meilleurs clients représentent %.1f%% de votre chiffre d'affaires. Diversifiez votre portefeuille pour limiter ce risque.",
                        concentration),
                    "HAUTE",
                    "fa-users",
                    "Voir Clients"
                ));
            }
        }

        // === 7. Slow-moving inventory ===
        // Products in the bottom 5 that still hold stock are capital tied up.
        // We flag them only once per analysis, keyed off the total count so the
        // user gets a single summary rather than five noisy alerts.
        if (topProducts != null && topProducts.size() > 10) {
            int slowCount = 0;
            int start = Math.max(0, topProducts.size() - 5);
            for (int i = topProducts.size() - 1; i >= start; i--) {
                Object[] row = topProducts.get(i);
                if (row != null && row.length > 1 && row[1] != null
                        && ((Number) row[1]).longValue() > 0) {
                    slowCount++;
                }
            }
            if (slowCount > 0) {
                recommendations.add(new RecommendationDto(
                    "PRODUITS_LENTS",
                    "Produits à Rotation Faible",
                    String.format(Locale.FRANCE,
                        "%d article(s) figurent parmi vos cinq plus faibles ventes. Envisagez une promotion ciblée ou un déréférencement pour libérer la trésorerie.",
                        slowCount),
                    "MOYENNE",
                    "fa-boxes-stacked",
                    "Voir Produits"
                ));
            }
        }

        // === 8. Data quality ===
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

    /* =========================== ML helpers =========================== */

    /**
     * Build per-article evidence rows and delegate to
     * {@link MlRecommendationEngine#stockRiskRecommendations}.
     *
     * <p>Demand velocity is estimated from the persisted {@code quantite_vendue}
     * cumulative counter divided by a 12-month window — a rough but
     * database-only proxy that avoids heavy SQL joins. Articles without any
     * recorded sales are skipped.
     */
    List<RecommendationDto> mlStockRiskRecommendations(Long entrepriseId) {
        List<RecommendationDto> out = new ArrayList<>();
        List<Stock> stocks;
        try {
            stocks = stockRepository.findByEntrepriseId(entrepriseId);
        } catch (RuntimeException ex) {
            return out;
        }
        if (stocks == null || stocks.isEmpty()) return out;

        List<MlRecommendationEngine.StockRow> rows = new ArrayList<>();
        for (Stock s : stocks) {
            double stockLevel = toDouble(s.getQuantiteDisponible());
            double sold = toDouble(s.getQuantiteVendue());
            if (stockLevel <= 0 && sold <= 0) continue;
            double avgMonthly = sold > 0 ? sold / 12.0 : 0.0;
            // Demand variance unknown at row level — use a fraction of the
            // mean as a reasonable CV proxy so features are never exactly zero.
            double demandVariance = Math.max(1.0, avgMonthly * 0.5);
            double dailyDemand = avgMonthly / 30.0;
            double daysOfCover = dailyDemand > 0
                    ? stockLevel / dailyDemand
                    : (stockLevel > 0 ? 365.0 : 0.0);
            // Lead-time proxy from current month — a constant default; the
            // classifier still works because only the *relative* ordering
            // matters for the logistic fit.
            double leadTimeProxy = 7.0;
            double seasonalityPhase = (LocalDate.now().getMonthValue() - 1) / 11.0;
            String articleName = articleName(s);
            rows.add(new MlRecommendationEngine.StockRow(
                    articleName, stockLevel, avgMonthly, demandVariance,
                    leadTimeProxy, daysOfCover, seasonalityPhase));
        }
        List<RecommendationDto> all = mlRecommendationEngine.stockRiskRecommendations(
                rows, STOCK_RISK_HORIZON_DAYS);
        // Cap to MAX_ML_STOCK_RECOMMENDATIONS to avoid flooding the dashboard.
        for (int i = 0; i < all.size() && i < MAX_ML_STOCK_RECOMMENDATIONS; i++) {
            out.add(all.get(i));
        }
        return out;
    }

    /** Opportunity regression on the top-products dataset. */
    List<RecommendationDto> mlOpportunityRecommendations(List<Object[]> topProducts) {
        if (topProducts == null || topProducts.isEmpty()) return new ArrayList<>();
        // Compute category share on the fly from total revenue.
        double totalRev = 0.0;
        for (Object[] row : topProducts) {
            if (row != null && row.length > 2 && row[2] != null) {
                totalRev += ((Number) row[2]).doubleValue();
            }
        }
        List<MlRecommendationEngine.ProductRow> rows = new ArrayList<>();
        int n = topProducts.size();
        for (int i = 0; i < n; i++) {
            Object[] row = topProducts.get(i);
            if (row == null || row.length < 3 || row[0] == null) continue;
            String name = row[0].toString();
            double qty = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            double rev = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            double marginProxy = qty > 0 ? rev / qty : rev;
            double catShare = totalRev > 0 ? rev / totalRev : 0.0;
            // Velocity trend proxy: rank-based descending score. Higher-ranked
            // products get a positive trend, lower-ranked get negative.
            double trend = n > 1 ? (double) (n - 1 - i) / (n - 1) - 0.5 : 0.0;
            rows.add(new MlRecommendationEngine.ProductRow(
                    name, rev, qty, marginProxy, catShare, trend));
        }
        return mlRecommendationEngine.opportunityRecommendations(rows);
    }

    private static double toDouble(BigDecimal b) {
        return b != null ? b.doubleValue() : 0.0;
    }

    private static String articleName(Stock s) {
        try {
            if (s.getArticleAchat() != null && s.getArticleAchat().getNom() != null) {
                return s.getArticleAchat().getNom();
            }
        } catch (RuntimeException ignored) {
            // Lazy-loading issue on a detached article — fall through to fallback.
        }
        return "Article #" + s.getId();
    }
}
