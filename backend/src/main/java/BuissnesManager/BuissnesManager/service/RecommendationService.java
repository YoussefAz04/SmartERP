package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.RecommendationDto;
import BuissnesManager.BuissnesManager.dto.RecommendationDto.MetricItem;
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

    /**
     * Generates a deterministic "phrasing variant" per call so that two
     * subsequent dashboard refreshes within the same month show the same
     * wording (no jitter), but messages rotate naturally month over month.
     */
    private static int phrasingVariant(Long entrepriseId) {
        long e = entrepriseId != null ? entrepriseId : 0L;
        int monthSeed = LocalDate.now().getYear() * 12 + LocalDate.now().getMonthValue();
        return (int) Math.floorMod(e * 31 + monthSeed, Integer.MAX_VALUE);
    }

    /** Picks one phrasing among {@code variants} based on the rotating seed. */
    private static String pick(int variant, String... variants) {
        if (variants == null || variants.length == 0) return "";
        return variants[Math.floorMod(variant, variants.length)];
    }

    public List<RecommendationDto> generateRecommendations(Long entrepriseId) {
        List<RecommendationDto> recommendations = new ArrayList<>();
        final int v = phrasingVariant(entrepriseId);

        // === 0. ML stock-risk classifier (per-article). Appended first so
        // critical risks surface at the top of the list. When the classifier
        // can't fit (too few articles, single-class data …) it returns an
        // empty list and the rule-based counts below take over. ===
        List<RecommendationDto> mlStockRisk = mlStockRiskRecommendations(entrepriseId);
        recommendations.addAll(mlStockRisk);

        // === 1. Stock alerts ===
        Long lowStockCount = stockRepository.getLowStockItemsCount(entrepriseId);
        Long outOfStockCount = stockRepository.getOutOfStockItemsCount(entrepriseId);
        String worstStockArticle = findWorstStockArticleName(entrepriseId);

        if (outOfStockCount != null && outOfStockCount > 0) {
            String detail = worstStockArticle != null
                    ? String.format(Locale.FRANCE,
                            " Référence la plus impactée : « %s ».",
                            worstStockArticle)
                    : "";
            String headline = pick(v,
                    outOfStockCount + " référence(s) en rupture de stock totale.",
                    outOfStockCount + " article(s) accusent un stock nul.",
                    "Rupture confirmée sur " + outOfStockCount + " référence(s).");
            String insight = pick(v,
                    "Déclenchez un bon de commande en urgence pour rétablir la disponibilité.",
                    "Émettez les bons de commande nécessaires pour prévenir toute rupture de service.",
                    "Traitez ces ruptures en priorité absolue — chaque jour de rupture se traduit en perte de vente.");
            MetricItem[] metrics = worstStockArticle != null
                    ? new MetricItem[]{
                        new MetricItem("Articles en rupture", String.valueOf(outOfStockCount)),
                        new MetricItem("Référence la plus impactée", worstStockArticle)
                      }
                    : new MetricItem[]{
                        new MetricItem("Articles en rupture", String.valueOf(outOfStockCount))
                      };
            recommendations.add(new RecommendationDto(
                "STOCK_CRITIQUE",
                "Rupture de Stock Détectée",
                headline + detail,
                "CRITIQUE",
                "fa-exclamation-triangle",
                "Voir Stock"
            ).withMetrics(metrics).withInsight(insight));
        }

        if (lowStockCount != null && lowStockCount > 0) {
            String insightLow = worstStockArticle != null && (outOfStockCount == null || outOfStockCount == 0)
                    ? String.format(Locale.FRANCE,
                            "Référence la plus exposée : « %s ». Planifiez le réapprovisionnement avant rupture.",
                            worstStockArticle)
                    : "Planifiez vos commandes fournisseurs pour garantir la continuité opérationnelle.";
            String headline = pick(v,
                    lowStockCount + " référence(s) ont atteint le seuil de réapprovisionnement.",
                    "Seuil d'alerte franchi par " + lowStockCount + " référence(s).",
                    lowStockCount + " article(s) approchent du point de rupture.");
            MetricItem[] metricsLow = worstStockArticle != null && (outOfStockCount == null || outOfStockCount == 0)
                    ? new MetricItem[]{
                        new MetricItem("Articles en alerte stock", String.valueOf(lowStockCount)),
                        new MetricItem("Référence la plus exposée", worstStockArticle)
                      }
                    : new MetricItem[]{
                        new MetricItem("Articles en alerte stock", String.valueOf(lowStockCount))
                      };
            recommendations.add(new RecommendationDto(
                "STOCK_FAIBLE",
                "Stock Faible - Action Requise",
                headline,
                "HAUTE",
                "fa-boxes",
                "Gérer Stock"
            ).withMetrics(metricsLow).withInsight(insightLow));
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
                String levers = pick(v,
                        "Activez les actions commerciales prioritaires et relancez votre portefeuille clients.",
                        "Déclenchez les relances commerciales et envisagez une remise promotionnelle ciblée.",
                        "Reprenez contact avec les clients dormants et réactivez les campagnes de vente.");
                recommendations.add(new RecommendationDto(
                    "VENTES_FAIBLES",
                    "Performance des Ventes ce Mois",
                    String.format(Locale.FRANCE,
                        "Le chiffre d'affaires mensuel accuse un écart de %.0f %% par rapport à la référence historique.",
                        shortfallPct),
                    "MOYENNE",
                    "fa-chart-line",
                    "Analyser Ventes"
                ).withMetrics(
                    new MetricItem("Réalisé (mois en cours)", String.format(Locale.FRANCE, "%.0f TND", salesThisMonth)),
                    new MetricItem("Objectif proratisé", String.format(Locale.FRANCE, "%.0f TND", expectedMtd)),
                    new MetricItem("Écart", String.format(Locale.FRANCE, "−%.0f %%", shortfallPct))
                ).withInsight(levers));
            } else if (expectedMtd > 0 && now.getDayOfMonth() >= 10
                    && salesThisMonth > 1.3 * expectedMtd) {
                double surgePct = ((salesThisMonth - expectedMtd) / expectedMtd) * 100.0;
                String advice = pick(v,
                        "Sécurisez le stock des références leaders et capitalisez sur cette dynamique.",
                        "Augmentez les volumes commandés auprès des fournisseurs avant la fin du mois.",
                        "Engagez une action de fidélisation pour consolider les nouveaux acheteurs.");
                recommendations.add(new RecommendationDto(
                    "TENDANCE_CROISSANCE",
                    "Tendance de Ventes Positive",
                    String.format(Locale.FRANCE,
                        "Performance commerciale en hausse : le chiffre d'affaires mensuel dépasse la référence historique de %.0f %%.",
                        surgePct),
                    "INFO",
                    "fa-rocket",
                    "Analyser Ventes"
                ).withMetrics(
                    new MetricItem("Réalisé (mois en cours)", String.format(Locale.FRANCE, "%.0f TND", salesThisMonth)),
                    new MetricItem("Référence proratisée", String.format(Locale.FRANCE, "%.0f TND", expectedMtd)),
                    new MetricItem("Hausse", String.format(Locale.FRANCE, "+%.0f %%", surgePct))
                ).withInsight(advice));
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
                    "« %s » est la référence la plus performante du catalogue.",
                    topProduct),
                "BASSE",
                "fa-star",
                "Voir Produits"
            ).withMetrics(
                new MetricItem("Produit vedette", topProduct)
            ).withInsight("Maintenez un stock optimal et valorisez les ventes croisées pour maximiser la contribution au chiffre d'affaires."));
        }

        // === 4. Gross margin ===
        Double totalPurchases = commandeAchatRepository.getTotalPurchases(entrepriseId);
        Double cogsAllTime = commandeVenteRepository.getCOGSInPeriod(
                entrepriseId, LocalDate.of(1970, 1, 1), now);
        double cogs = cogsAllTime != null ? cogsAllTime : 0.0;
        if (totalSales != null && totalSales > 0 && cogs > 0) {
            double margin = ((totalSales - cogs) / totalSales) * 100.0;
            double grossProfit = totalSales - cogs;
            String marginStr = String.format(Locale.FRANCE, "%.1f", margin);
            if (margin < 20.0) {
                String levers = pick(v,
                        "Renégociez vos conditions fournisseurs ou réévaluez les prix de vente des références à fort volume.",
                        "Identifiez les articles à marge négative et ajustez leur tarification ou leur référencement.",
                        "Comparez vos prix d'achat auprès de fournisseurs alternatifs et envisagez une remise en concurrence.");
                recommendations.add(new RecommendationDto(
                    "MARGE_FAIBLE",
                    "Amélioration de la Marge Commerciale",
                    String.format(Locale.FRANCE, "Taux de marge brute insuffisant : %s %%.", marginStr),
                    "HAUTE",
                    "fa-percentage",
                    "Analyser Achats"
                ).withMetrics(
                    new MetricItem("Taux de marge brute", marginStr + " %"),
                    new MetricItem("Chiffre d'affaires", String.format(Locale.FRANCE, "%.0f TND", totalSales)),
                    new MetricItem("Coût des ventes", String.format(Locale.FRANCE, "%.0f TND", cogs)),
                    new MetricItem("Marge dégagée", String.format(Locale.FRANCE, "%.0f TND", grossProfit))
                ).withInsight(levers));
            } else if (margin < 35.0) {
                recommendations.add(new RecommendationDto(
                    "MARGE_CORRECTE",
                    "Marge Commerciale Acceptable",
                    String.format(Locale.FRANCE, "Taux de marge brute : %s %%.", marginStr),
                    "INFO",
                    "fa-percentage",
                    "Voir Détails"
                ).withMetrics(
                    new MetricItem("Taux de marge brute", marginStr + " %"),
                    new MetricItem("Chiffre d'affaires", String.format(Locale.FRANCE, "%.0f TND", totalSales)),
                    new MetricItem("Marge dégagée", String.format(Locale.FRANCE, "%.0f TND", grossProfit))
                ).withInsight(pick(v,
                    "Un taux cible de 35 % renforcerait la résilience financière.",
                    "Travaillez les conditions fournisseurs pour franchir le palier des 35 %.",
                    "Identifiez les références à marge faible pour améliorer le taux global.")));
            } else {
                recommendations.add(new RecommendationDto(
                    "BONNE_PERFORMANCE",
                    "Excellente Marge Commerciale",
                    String.format(Locale.FRANCE, "Taux de marge brute : %s %%.", marginStr),
                    "INFO",
                    "fa-thumbs-up",
                    "Voir Détails"
                ).withMetrics(
                    new MetricItem("Taux de marge brute", marginStr + " %"),
                    new MetricItem("Chiffre d'affaires", String.format(Locale.FRANCE, "%.0f TND", totalSales)),
                    new MetricItem("Marge dégagée", String.format(Locale.FRANCE, "%.0f TND", grossProfit))
                ).withInsight(pick(v,
                    "Maintenez la maîtrise des coûts d'approvisionnement pour préserver cette performance.",
                    "Investissez la marge dégagée dans des actions de fidélisation et de développement client.",
                    "Sécurisez ces conditions tarifaires par des contrats-cadres avec vos fournisseurs stratégiques.")));
            }
        } else if (totalSales != null && totalPurchases != null
                && totalSales > 0 && totalPurchases > 0) {
            double ratio = (totalPurchases / totalSales) * 100.0;
            recommendations.add(new RecommendationDto(
                "RATIO_ACHATS",
                "Ratio Achats/Ventes",
                String.format(Locale.FRANCE,
                    "Le ratio achats/ventes s'établit à %.1f %%.", ratio),
                "INFO",
                "fa-balance-scale",
                "Voir Détails"
            ).withMetrics(
                new MetricItem("Ratio achats / ventes", String.format(Locale.FRANCE, "%.1f %%", ratio)),
                new MetricItem("Achats", String.format(Locale.FRANCE, "%.0f TND", totalPurchases)),
                new MetricItem("Chiffre d'affaires", String.format(Locale.FRANCE, "%.0f TND", totalSales))
            ).withInsight("Renseignez les prix d'achat des articles pour activer le calcul automatique de la marge commerciale."));
        }

        // === 5. Delivery quality ===
        Long overdueOrders = commandeAchatRepository.getOverdueOrdersCount(entrepriseId, now);
        Long activeOrders = commandeAchatRepository.getActiveOrdersCount(entrepriseId);
        if (overdueOrders != null && overdueOrders > 0) {
            String urgency = overdueOrders > 5 ? "HAUTE" : "MOYENNE";
            String icon = overdueOrders > 5 ? "fa-exclamation-circle" : "fa-clock";
            String advice = pick(v,
                    "Sollicitez vos fournisseurs pour une mise à jour du statut de livraison.",
                    "Exigez une nouvelle date de livraison ferme et arbitrez si nécessaire.",
                    "Identifiez les fournisseurs récurrents en retard et révisez leurs conditions contractuelles.");
            recommendations.add(new RecommendationDto(
                "LIVRAISON_RETARD",
                "Commandes en Retard de Livraison",
                String.format(Locale.FRANCE,
                    "%d bon(s) de commande dépassent l'échéance de livraison contractuelle.",
                    overdueOrders),
                urgency,
                icon,
                "Voir Commandes"
            ).withMetrics(
                new MetricItem("Commandes en retard", String.valueOf(overdueOrders))
            ).withInsight(advice));
        } else if (activeOrders != null && activeOrders > 0) {
            recommendations.add(new RecommendationDto(
                "LIVRAISON_OK",
                "Livraisons à Jour",
                String.format(Locale.FRANCE,
                    "L'ensemble des bons de commande actifs (%d) s'exécute dans les délais contractuels.",
                    activeOrders),
                "INFO",
                "fa-truck",
                "Voir Commandes"
            ).withMetrics(
                new MetricItem("Commandes actives", String.valueOf(activeOrders)),
                new MetricItem("Commandes en retard", "0")
            ).withInsight(pick(v,
                "Maintenez le suivi des confirmations fournisseurs pour garantir cette performance.",
                "Anticipez les commandes pour les périodes à forte demande prévisionnelle.",
                "Bonne maîtrise de la chaîne d'approvisionnement — capitalisez sur ce niveau de service.")));
        }

        // === 6. Customer concentration risk ===
        // If the top 3 customers account for more than 60% of revenue the
        // business is exposed to the churn of any single one of them.
        List<Object[]> topCustomers = commandeVenteRepository.getTopCustomers(entrepriseId);
        if (topCustomers != null && !topCustomers.isEmpty()
                && totalSales != null && totalSales > 0) {
            double top3Revenue = 0.0;
            int counted = 0;
            String topCustomerName = null;
            double topCustomerRev = 0.0;
            for (Object[] row : topCustomers) {
                if (counted >= 3) break;
                if (row != null && row.length > 1 && row[1] != null) {
                    double rev = ((Number) row[1]).doubleValue();
                    top3Revenue += rev;
                    if (counted == 0 && row[0] != null) {
                        topCustomerName = row[0].toString();
                        topCustomerRev = rev;
                    }
                    counted++;
                }
            }
            double concentration = (top3Revenue / totalSales) * 100.0;
            if (concentration > 60.0 && counted >= 3 && topCustomerName != null) {
                double topShare = (topCustomerRev / totalSales) * 100.0;
                recommendations.add(new RecommendationDto(
                    "CONCENTRATION_CLIENT",
                    "Risque de Concentration Client",
                    String.format(Locale.FRANCE,
                        "Les 3 principaux clients concentrent %.1f %% du chiffre d'affaires.",
                        concentration),
                    "HAUTE",
                    "fa-users",
                    "Voir Clients"
                ).withMetrics(
                    new MetricItem("Part top 3 clients", String.format(Locale.FRANCE, "%.1f %%", concentration)),
                    new MetricItem("1er client — " + topCustomerName, String.format(Locale.FRANCE, "%.1f %% du CA", topShare))
                ).withInsight(pick(v,
                    "Diversifiez le portefeuille client pour réduire l'exposition au risque de churn.",
                    "Lancez un programme d'acquisition pour atténuer cette dépendance.",
                    "Sécurisez ces comptes stratégiques par des engagements contractuels ou des conditions préférentielles.")));
            }
        }

        // === 7. Slow-moving inventory ===
        // Products in the bottom 5 that still hold stock are capital tied up.
        // We flag them only once per analysis, keyed off the total count so the
        // user gets a single summary rather than five noisy alerts.
        if (topProducts != null && topProducts.size() > 10) {
            int slowCount = 0;
            String firstSlowName = null;
            int start = Math.max(0, topProducts.size() - 5);
            for (int i = topProducts.size() - 1; i >= start; i--) {
                Object[] row = topProducts.get(i);
                if (row != null && row.length > 1 && row[1] != null
                        && ((Number) row[1]).longValue() > 0) {
                    slowCount++;
                    if (firstSlowName == null && row[0] != null) {
                        firstSlowName = row[0].toString();
                    }
                }
            }
            if (slowCount > 0) {
                String detail = firstSlowName != null
                        ? String.format(Locale.FRANCE, " Exemple : « %s ».", firstSlowName)
                        : "";
                MetricItem[] slowMetrics = firstSlowName != null
                        ? new MetricItem[]{
                            new MetricItem("Références concernées", String.valueOf(slowCount)),
                            new MetricItem("Exemple de référence lente", firstSlowName)
                          }
                        : new MetricItem[]{
                            new MetricItem("Références concernées", String.valueOf(slowCount))
                          };
                recommendations.add(new RecommendationDto(
                    "PRODUITS_LENTS",
                    "Produits à Rotation Faible",
                    String.format(Locale.FRANCE,
                        "%d référence(s) accusent les cinq plus faibles rotations du catalogue.%s",
                        slowCount, detail),
                    "MOYENNE",
                    "fa-boxes-stacked",
                    "Voir Produits"
                ).withMetrics(slowMetrics).withInsight(pick(v,
                    "Envisagez une promotion ciblée ou un déréférencement pour libérer la trésorerie immobilisée.",
                    "Regroupez ces références en offres combinées pour accélérer leur écoulement.",
                    "Réduisez les stocks cibles pour dégager du fonds de roulement.")));
            }
        }

        // === 9. Cancellation rate (sales orders) ===
        // High cancellation rate may signal stock-out at the time of order,
        // pricing issues, or fulfilment problems. We surface it only when we
        // have enough orders to be statistically meaningful.
        if (totalOrders != null && totalOrders >= 20) {
            List<Object[]> orderStatus = commandeVenteRepository.getOrderStatusBreakdown(entrepriseId);
            long cancelled = 0;
            long totalStatus = 0;
            if (orderStatus != null) {
                for (Object[] row : orderStatus) {
                    if (row == null || row.length < 2 || row[0] == null || row[1] == null) continue;
                    long cnt = ((Number) row[1]).longValue();
                    totalStatus += cnt;
                    if ("ANNULEE".equalsIgnoreCase(row[0].toString())) cancelled = cnt;
                }
            }
            if (totalStatus > 0 && cancelled > 0) {
                double rate = (cancelled * 100.0) / totalStatus;
                if (rate > 15.0) {
                    recommendations.add(new RecommendationDto(
                        "TAUX_ANNULATION",
                        "Taux d'Annulation Élevé",
                        String.format(Locale.FRANCE,
                            "Le taux d'annulation des commandes de vente s'établit à %.1f %%.",
                            rate),
                        rate > 25.0 ? "HAUTE" : "MOYENNE",
                        "fa-ban",
                        "Voir Commandes"
                    ).withMetrics(
                        new MetricItem("Taux d'annulation", String.format(Locale.FRANCE, "%.1f %%", rate)),
                        new MetricItem("Commandes annulées", String.valueOf(cancelled)),
                        new MetricItem("Total commandes", String.valueOf(totalStatus))
                    ).withInsight(pick(v,
                        "Vérifiez la disponibilité du stock au moment de la prise de commande.",
                        "Analysez les motifs d'annulation pour identifier la cause racine.",
                        "Contrôlez les délais de confirmation et la qualité des fiches articles.")));
                }
            }
        }

        // === 10. Category concentration / opportunity ===
        try {
            List<Object[]> categories = commandeVenteRepository.getCategoryPerformance(entrepriseId);
            if (categories != null && categories.size() >= 2 && totalSales != null && totalSales > 0) {
                double catTotal = 0.0;
                for (Object[] row : categories) {
                    if (row != null && row.length > 1 && row[1] != null) {
                        catTotal += ((Number) row[1]).doubleValue();
                    }
                }
                Object[] first = categories.get(0);
                if (catTotal > 0 && first != null && first.length > 1
                        && first[0] != null && first[1] != null) {
                    String topCat = first[0].toString();
                    double topCatRev = ((Number) first[1]).doubleValue();
                    double topShare = (topCatRev / catTotal) * 100.0;
                    if (topShare > 70.0) {
                        recommendations.add(new RecommendationDto(
                            "CATEGORIE_DOMINANTE",
                            "Concentration par Catégorie",
                            String.format(Locale.FRANCE,
                                "La famille « %s » représente %.1f %% du chiffre d'affaires total.",
                                topCat, topShare),
                            "MOYENNE",
                            "fa-layer-group",
                            "Voir Produits"
                        ).withMetrics(
                            new MetricItem("Catégorie principale", topCat),
                            new MetricItem("Part de marché", String.format(Locale.FRANCE, "%.1f %%", topShare))
                        ).withInsight(pick(v,
                            "Diversifiez l'offre pour amortir un éventuel retournement de marché.",
                            "Identifiez une ou deux catégories adjacentes à développer.",
                            "Renforcez le plan marketing des catégories secondaires pour rééquilibrer le mix produit.")));
                    }
                }
            }
        } catch (RuntimeException ignored) {
            // Defensive: if the legacy schema returns unexpected types, skip the rule silently.
        }

        // === 11. Supplier diversification ===
        try {
            List<Object[]> suppliers = commandeAchatRepository.getSupplierMetrics(entrepriseId);
            if (suppliers != null && !suppliers.isEmpty()) {
                double suppTotal = 0.0;
                for (Object[] row : suppliers) {
                    if (row != null && row.length > 4 && row[4] != null) {
                        suppTotal += ((Number) row[4]).doubleValue();
                    }
                }
                if (suppTotal > 0) {
                    Object[] first = suppliers.get(0);
                    if (first != null && first.length > 4 && first[1] != null && first[4] != null) {
                        String name = first[1].toString();
                        double rev = ((Number) first[4]).doubleValue();
                        double share = (rev / suppTotal) * 100.0;
                        if (share > 65.0 && suppliers.size() >= 2) {
                            recommendations.add(new RecommendationDto(
                                "DIVERSIFICATION_FOURNISSEUR",
                                "Dépendance Fournisseur",
                                String.format(Locale.FRANCE,
                                    "Le fournisseur « %s » représente %.1f %% du volume d'achat total.",
                                    name, share),
                                "MOYENNE",
                                "fa-truck-arrow-right",
                                "Voir Commandes"
                            ).withMetrics(
                                new MetricItem("Fournisseur principal", name),
                                new MetricItem("Part des achats", String.format(Locale.FRANCE, "%.1f %%", share))
                            ).withInsight(pick(v,
                                "Qualifiez un fournisseur de secours pour les références critiques.",
                                "Mettez en concurrence vos fournisseurs pour négocier de meilleures conditions.",
                                "Sécurisez votre approvisionnement en homologuant un second fournisseur stratégique.")));
                        }
                    }
                    // Highlight best-rated supplier when ratings are filled in.
                    Object[] best = null;
                    double bestScore = -1.0;
                    for (Object[] row : suppliers) {
                        if (row == null || row.length < 7) continue;
                        double notation = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
                        double tauxLiv = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
                        // Combine rating (0..5) and delivery rate (0..100) into a single score.
                        double score = notation * 20.0 + tauxLiv * 0.5;
                        if (notation > 0 && score > bestScore) {
                            bestScore = score;
                            best = row;
                        }
                    }
                    if (best != null && best[1] != null && best[6] != null) {
                        double notation = ((Number) best[6]).doubleValue();
                        double tauxLiv = best[5] != null ? ((Number) best[5]).doubleValue() : 0.0;
                        if (notation >= 4.0) {
                            recommendations.add(new RecommendationDto(
                                "FOURNISSEUR_PERFORMANT",
                                "Fournisseur de Référence",
                                String.format(Locale.FRANCE,
                                    "Le fournisseur « %s » obtient une évaluation de %.1f/5 avec un taux de service de %.0f %%.",
                                    best[1].toString(), notation, tauxLiv),
                                "INFO",
                                "fa-medal",
                                "Voir Commandes"
                            ).withMetrics(
                                new MetricItem("Fournisseur", best[1].toString()),
                                new MetricItem("Note", String.format(Locale.FRANCE, "%.1f / 5", notation)),
                                new MetricItem("Taux de service", String.format(Locale.FRANCE, "%.0f %%", tauxLiv))
                            ).withInsight(pick(v,
                                "Privilégiez-le pour vos approvisionnements stratégiques.",
                                "Sécurisez la relation par un contrat-cadre pluriannuel.",
                                "Étendez la collaboration sur d'autres familles de produits.")));
                        }
                    }
                }
            }
        } catch (RuntimeException ignored) {
            // Defensive: tolerate missing supplier metadata.
        }

        // === 12. Average order value (panier moyen) ===
        if (totalSales != null && totalOrders != null && totalOrders > 0 && totalSales > 0) {
            double aov = totalSales / totalOrders;
            // Compare against the AOV of the last 90 days to detect a shift.
            LocalDate ninetyDaysAgo = now.minusDays(90);
            Double recentSales = commandeVenteRepository.getSalesInPeriod(
                    entrepriseId, ninetyDaysAgo, now);
            Long recentOrders = commandeVenteRepository.getSalesOrderCountInPeriod(
                    entrepriseId, ninetyDaysAgo, now);
            if (recentSales != null && recentOrders != null && recentOrders >= 10) {
                double recentAov = recentSales / recentOrders;
                double delta = ((recentAov - aov) / aov) * 100.0;
                if (delta < -15.0) {
                    recommendations.add(new RecommendationDto(
                        "PANIER_MOYEN_BAISSE",
                        "Panier Moyen en Baisse",
                        String.format(Locale.FRANCE,
                            "Le ticket moyen sur les 90 derniers jours est inférieur de %.0f %% à la référence historique.",
                            Math.abs(delta)),
                        "MOYENNE",
                        "fa-cart-shopping",
                        "Analyser Ventes"
                    ).withMetrics(
                        new MetricItem("Ticket moyen récent (90j)", String.format(Locale.FRANCE, "%.0f TND", recentAov)),
                        new MetricItem("Référence historique", String.format(Locale.FRANCE, "%.0f TND", aov)),
                        new MetricItem("Variation", String.format(Locale.FRANCE, "−%.0f %%", Math.abs(delta)))
                    ).withInsight(pick(v,
                        "Activez des offres groupées ou des ventes additionnelles pour rehausser le ticket.",
                        "Révisez la politique de remise pour préserver la valeur unitaire.",
                        "Identifiez les segments clients qui réduisent leur engagement et adaptez l'offre.")));
                } else if (delta > 15.0) {
                    recommendations.add(new RecommendationDto(
                        "PANIER_MOYEN_HAUSSE",
                        "Panier Moyen en Hausse",
                        String.format(Locale.FRANCE,
                            "Le ticket moyen sur les 90 derniers jours progresse de %.0f %% par rapport à la référence historique.",
                            delta),
                        "INFO",
                        "fa-cart-arrow-down",
                        "Analyser Ventes"
                    ).withMetrics(
                        new MetricItem("Ticket moyen récent (90j)", String.format(Locale.FRANCE, "%.0f TND", recentAov)),
                        new MetricItem("Référence historique", String.format(Locale.FRANCE, "%.0f TND", aov)),
                        new MetricItem("Variation", String.format(Locale.FRANCE, "+%.0f %%", delta))
                    ).withInsight(pick(v,
                        "Capitalisez avec des offres premium et des ventes complémentaires.",
                        "Prolongez le mix produit qui tire le ticket vers le haut.",
                        "Étendez les bonnes pratiques commerciales à l'ensemble des segments.")));
                }
            }
        }

        // === 13. Cash-flow proxy: recent purchases vs recent sales ===
        // When 30-day purchases dwarf 30-day sales, working capital is being
        // committed faster than it's recovered.
        LocalDate thirtyDaysAgo = now.minusDays(30);
        Double recentPurchases = commandeAchatRepository.getPurchasesInPeriod(
                entrepriseId, thirtyDaysAgo, now);
        Double recentSales30 = commandeVenteRepository.getSalesInPeriod(
                entrepriseId, thirtyDaysAgo, now);
        if (recentPurchases != null && recentSales30 != null
                && recentSales30 > 0 && recentPurchases > 0) {
            double ratio = recentPurchases / recentSales30;
            if (ratio > 1.5 && recentPurchases > 1000.0) {
                recommendations.add(new RecommendationDto(
                    "CASH_FLOW",
                    "Pression sur la Trésorerie",
                    String.format(Locale.FRANCE,
                        "Sur les 30 derniers jours, les engagements d'achat excèdent le chiffre d'affaires d'un facteur ×%.1f.",
                        ratio),
                    "HAUTE",
                    "fa-wallet",
                    "Voir Commandes"
                ).withMetrics(
                    new MetricItem("Achats (30 derniers jours)", String.format(Locale.FRANCE, "%.0f TND", recentPurchases)),
                    new MetricItem("CA (30 derniers jours)", String.format(Locale.FRANCE, "%.0f TND", recentSales30)),
                    new MetricItem("Ratio achats / CA", String.format(Locale.FRANCE, "×%.1f", ratio))
                ).withInsight(pick(v,
                    "Échelonnez les commandes fournisseurs ou négociez des délais de paiement élargis.",
                    "Vérifiez que la constitution de stock est justifiée par la demande prévisionnelle.",
                    "Priorisez l'écoulement du stock existant avant tout nouvel engagement d'achat.")));
            }
        }

        // === 14. Stock value tied up ===
        try {
            Double stockValue = stockRepository.getTotalStockValue(entrepriseId);
            if (stockValue != null && stockValue > 0
                    && totalSales != null && totalSales > 0) {
                // Months of sales held as inventory, valued at purchase price.
                double monthlyAvgSales = last12MonthSales != null && last12MonthSales > 0
                        ? last12MonthSales / 12.0
                        : totalSales / 12.0;
                if (monthlyAvgSales > 0) {
                    double monthsOfStock = stockValue / monthlyAvgSales;
                    if (monthsOfStock > 6.0) {
                        recommendations.add(new RecommendationDto(
                            "VALEUR_STOCK",
                            "Stock Immobilisé Important",
                            String.format(Locale.FRANCE,
                                "La valeur du stock immobilisé représente l'équivalent de %.1f mois de chiffre d'affaires.",
                                monthsOfStock),
                            "MOYENNE",
                            "fa-warehouse",
                            "Voir Stock"
                        ).withMetrics(
                            new MetricItem("Valeur du stock immobilisé", String.format(Locale.FRANCE, "%.0f TND", stockValue)),
                            new MetricItem("Couverture en mois de CA", String.format(Locale.FRANCE, "%.1f mois", monthsOfStock))
                        ).withInsight(pick(v,
                            "Identifiez les références en surstock pour libérer la trésorerie immobilisée.",
                            "Lancez une opération de déstockage ciblée sur les articles à rotation lente.",
                            "Révisez les quantités cibles des articles sur-approvisionnés.")));
                    }
                }
            }
        } catch (RuntimeException ignored) {
            // Defensive: tolerate missing prixAchat causing the SUM to fail.
        }

        // === 8. Data quality ===
        if (totalOrders != null && totalOrders < 10) {
            recommendations.add(new RecommendationDto(
                "DONNEES_INSUFFISANTES",
                "Enrichissez Vos Données",
                String.format(Locale.FRANCE,
                    "Base de données insuffisante : %d transaction(s) enregistrée(s).",
                    totalOrders),
                "INFO",
                "fa-database",
                "Ajouter Commandes"
            ).withMetrics(
                new MetricItem("Transactions enregistrées", String.valueOf(totalOrders)),
                new MetricItem("Seuil d'activation IA", "30 transactions")
            ).withInsight(pick(v,
                "L'enrichissement des données améliore la précision des modèles prédictifs et la pertinence des analyses.",
                "Les moteurs d'analyse avancée se déclenchent à partir de 30 transactions — complétez votre historique pour activer les prévisions.",
                "Importez votre historique de commandes pour activer l'ensemble des prévisions et recommandations IA.")));
        }

        return recommendations;
    }

    /**
     * Returns the name of the stock row with the lowest quantity (or {@code null}
     * if no stock exists). Used to enrich stock alerts with the most exposed
     * article. Defensive: lazy-loading or schema issues are swallowed.
     */
    private String findWorstStockArticleName(Long entrepriseId) {
        try {
            List<Stock> stocks = stockRepository.findByEntrepriseId(entrepriseId);
            if (stocks == null || stocks.isEmpty()) return null;
            Stock worst = null;
            double worstQty = Double.POSITIVE_INFINITY;
            for (Stock s : stocks) {
                double q = toDouble(s.getQuantiteDisponible());
                if (q < worstQty) {
                    worstQty = q;
                    worst = s;
                }
            }
            return worst != null ? articleName(worst) : null;
        } catch (RuntimeException ignored) {
            return null;
        }
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
