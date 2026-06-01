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

    
    private static final int STOCK_RISK_HORIZON_DAYS = 30;

    
    private static final int MAX_ML_STOCK_RECOMMENDATIONS = 5;

    




    private static int phrasingVariant(Long entrepriseId) {
        long e = entrepriseId != null ? entrepriseId : 0L;
        int monthSeed = LocalDate.now().getYear() * 12 + LocalDate.now().getMonthValue();
        return (int) Math.floorMod(e * 31 + monthSeed, Integer.MAX_VALUE);
    }

    
    private static String pick(int variant, String... variants) {
        if (variants == null || variants.length == 0) return "";
        return variants[Math.floorMod(variant, variants.length)];
    }

    public List<RecommendationDto> generateRecommendations(Long entrepriseId) {
        List<RecommendationDto> recommendations = new ArrayList<>();
        final int v = phrasingVariant(entrepriseId);

        
        
        
        
        List<RecommendationDto> mlStockRisk = mlStockRiskRecommendations(entrepriseId);
        recommendations.addAll(mlStockRisk);

        
        Long lowStockCount = stockRepository.getLowStockItemsCount(entrepriseId);
        Long outOfStockCount = stockRepository.getOutOfStockItemsCount(entrepriseId);
        String worstStockArticle = findWorstStockArticleName(entrepriseId);

        if (outOfStockCount != null && outOfStockCount > 0) {
            String detail = worstStockArticle != null
                    ? String.format(Locale.FRANCE,
                            " Article le plus touché : « %s ». Passez une commande fournisseur dès aujourd'hui pour éviter de perdre des ventes.",
                            worstStockArticle)
                    : " Passez les commandes fournisseurs nécessaires dès aujourd'hui pour éviter de perdre des ventes.";
            String headline = pick(v,
                    outOfStockCount + " article(s) sont totalement en rupture de stock.",
                    outOfStockCount + " article(s) ont un stock à zéro.",
                    "Stock à zéro sur " + outOfStockCount + " article(s).");
            recommendations.add(new RecommendationDto(
                "STOCK_CRITIQUE",
                "Rupture de Stock Détectée",
                headline + detail,
                "CRITIQUE",
                "fa-exclamation-triangle",
                "Voir Stock"
            ));
        }

        if (lowStockCount != null && lowStockCount > 0) {
            String detail = worstStockArticle != null && (outOfStockCount == null || outOfStockCount == 0)
                    ? String.format(Locale.FRANCE,
                            " Article le plus exposé : « %s ». Commandez avant qu'il ne tombe à zéro.",
                            worstStockArticle)
                    : " Passez vos commandes fournisseurs maintenant pour ne pas tomber en rupture.";
            String headline = pick(v,
                    lowStockCount + " article(s) ont un stock faible et doivent être réapprovisionnés.",
                    lowStockCount + " article(s) sont sous le seuil d'alerte.",
                    lowStockCount + " article(s) risquent bientôt la rupture de stock.");
            recommendations.add(new RecommendationDto(
                "STOCK_FAIBLE",
                "Stock Faible - Action Requise",
                headline + detail,
                "HAUTE",
                "fa-boxes",
                "Gérer Stock"
            ));
        }

        
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        Double salesThisMonth = commandeVenteRepository.getSalesInPeriod(entrepriseId, startOfMonth, endOfMonth);
        Double totalSales = commandeVenteRepository.getTotalSalesRevenue(entrepriseId);
        Long totalOrders = commandeVenteRepository.getTotalSalesOrders(entrepriseId);

        
        
        
        LocalDate twelveMonthsAgo = now.minusMonths(12).withDayOfMonth(1);
        Double last12MonthSales = commandeVenteRepository.getSalesInPeriod(entrepriseId, twelveMonthsAgo, now);
        if (salesThisMonth != null && last12MonthSales != null && last12MonthSales > 0) {
            double avgMonthly = last12MonthSales / 12.0;
            double fractionElapsed = (double) now.getDayOfMonth() / now.lengthOfMonth();
            double expectedMtd = avgMonthly * fractionElapsed;
            
            
            if (expectedMtd > 0 && now.getDayOfMonth() >= 7
                    && salesThisMonth < 0.5 * expectedMtd) {
                double shortfallPct = ((expectedMtd - salesThisMonth) / expectedMtd) * 100.0;
                String levers = pick(v,
                        "Lancez vos actions commerciales prioritaires et relancez vos clients existants.",
                        "Contactez vos clients dormants et proposez une remise ciblée pour relancer les ventes.",
                        "Reprenez contact avec vos anciens clients et relancez vos campagnes commerciales.");
                recommendations.add(new RecommendationDto(
                    "VENTES_FAIBLES",
                    "Ventes du mois en retard",
                    String.format(Locale.FRANCE,
                        "Vos ventes du mois sont %.0f %% en dessous de votre moyenne habituelle (réalisé : %,.0f — attendu à ce stade du mois : %,.0f). %s",
                        shortfallPct, salesThisMonth, expectedMtd, levers),
                    "MOYENNE",
                    "fa-chart-line",
                    "Analyser Ventes"
                ));
            } else if (expectedMtd > 0 && now.getDayOfMonth() >= 10
                    && salesThisMonth > 1.3 * expectedMtd) {
                double surgePct = ((salesThisMonth - expectedMtd) / expectedMtd) * 100.0;
                String advice = pick(v,
                        "Reconstituez le stock de vos meilleurs articles pour ne pas tomber en rupture.",
                        "Augmentez vos commandes fournisseurs avant la fin du mois pour suivre la demande.",
                        "Profitez de cette dynamique pour fidéliser vos nouveaux clients.");
                recommendations.add(new RecommendationDto(
                    "TENDANCE_CROISSANCE",
                    "Ventes du mois en forte hausse",
                    String.format(Locale.FRANCE,
                        "Vos ventes du mois dépassent de %.0f %% votre moyenne habituelle (réalisé : %,.0f — attendu à ce stade du mois : %,.0f). %s",
                        surgePct, salesThisMonth, expectedMtd, advice),
                    "INFO",
                    "fa-rocket",
                    "Analyser Ventes"
                ));
            }
        }

        
        List<Object[]> topProducts = commandeVenteRepository.getTopProducts(entrepriseId);
        List<RecommendationDto> mlOpportunity = mlOpportunityRecommendations(topProducts);
        if (!mlOpportunity.isEmpty()) {
            recommendations.addAll(mlOpportunity);
        } else if (topProducts != null && !topProducts.isEmpty()
                && topProducts.get(0) != null && topProducts.get(0).length > 0
                && topProducts.get(0)[0] != null) {
            
            String topProduct = topProducts.get(0)[0].toString();
            recommendations.add(new RecommendationDto(
                "PRODUIT_STAR",
                "Article le plus vendu",
                String.format(Locale.FRANCE,
                    "« %s » est l'article qui se vend le mieux. Gardez toujours un stock suffisant et proposez-le avec d'autres articles complémentaires pour augmenter vos ventes.",
                    topProduct),
                "BASSE",
                "fa-star",
                "Voir Produits"
            ));
        }

        
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
                        "Renégociez vos prix d'achat avec vos fournisseurs ou augmentez vos prix de vente sur les articles les plus vendus.",
                        "Repérez les articles vendus à perte ou à très faible marge et corrigez leur prix de vente.",
                        "Comparez vos prix d'achat avec d'autres fournisseurs pour obtenir de meilleures conditions.");
                recommendations.add(new RecommendationDto(
                    "MARGE_FAIBLE",
                    "Marge trop faible",
                    String.format(Locale.FRANCE,
                        "Votre marge est de seulement %s %%. Sur %,.0f de ventes, vos achats vous coûtent %,.0f, ce qui ne vous laisse que %,.0f de bénéfice. %s",
                        marginStr, totalSales, cogs, grossProfit, levers),
                    "HAUTE",
                    "fa-percentage",
                    "Analyser Achats"
                ));
            } else if (margin < 35.0) {
                recommendations.add(new RecommendationDto(
                    "MARGE_CORRECTE",
                    "Marge correcte",
                    String.format(Locale.FRANCE,
                        "Votre marge est de %s %%. Sur %,.0f de ventes, vous gagnez %,.0f. %s",
                        marginStr, totalSales, grossProfit,
                        pick(v,
                            "Visez 35 %% de marge pour gagner en sécurité financière.",
                            "Négociez de meilleurs prix d'achat pour atteindre 35 %% de marge.",
                            "Repérez les articles à faible marge pour améliorer le résultat global.")),
                    "INFO",
                    "fa-percentage",
                    "Voir Détails"
                ));
            } else {
                recommendations.add(new RecommendationDto(
                    "BONNE_PERFORMANCE",
                    "Très bonne marge",
                    String.format(Locale.FRANCE,
                        "Votre marge est excellente : %s %%. Sur %,.0f de ventes, vous gagnez %,.0f. %s",
                        marginStr, totalSales, grossProfit,
                        pick(v,
                            "Continuez à bien négocier vos prix d'achat pour garder cette marge.",
                            "Utilisez ce bénéfice pour fidéliser vos clients et développer votre activité.",
                            "Conservez ces bons prix d'achat en signant des accords de longue durée avec vos meilleurs fournisseurs.")),
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
                "Comparaison achats / ventes",
                String.format(Locale.FRANCE,
                    "Vos achats représentent %.1f %% de vos ventes (achats : %,.0f — ventes : %,.0f). Renseignez le prix d'achat de chaque article pour que le système calcule automatiquement votre marge réelle.",
                    ratio, totalPurchases, totalSales),
                "INFO",
                "fa-balance-scale",
                "Voir Détails"
            ));
        }

        
        Long overdueOrders = commandeAchatRepository.getOverdueOrdersCount(entrepriseId, now);
        Long activeOrders = commandeAchatRepository.getActiveOrdersCount(entrepriseId);
        if (overdueOrders != null && overdueOrders > 0) {
            String urgency = overdueOrders > 5 ? "HAUTE" : "MOYENNE";
            String icon = overdueOrders > 5 ? "fa-exclamation-circle" : "fa-clock";
            String advice = pick(v,
                    "Contactez vos fournisseurs pour connaître la nouvelle date de livraison.",
                    "Demandez à vos fournisseurs une date de livraison ferme et changez de fournisseur si nécessaire.",
                    "Identifiez les fournisseurs souvent en retard et revoyez vos conditions avec eux.");
            recommendations.add(new RecommendationDto(
                "LIVRAISON_RETARD",
                "Commandes en retard",
                String.format(Locale.FRANCE,
                    "%d commande(s) fournisseur ont dépassé la date de livraison prévue. %s",
                    overdueOrders, advice),
                urgency,
                icon,
                "Voir Commandes"
            ));
        } else if (activeOrders != null && activeOrders > 0) {
            recommendations.add(new RecommendationDto(
                "LIVRAISON_OK",
                "Livraisons à jour",
                String.format(Locale.FRANCE,
                    "Toutes vos commandes fournisseurs en cours (%d) seront livrées dans les délais. %s",
                    activeOrders,
                    pick(v,
                        "Continuez à suivre les confirmations de vos fournisseurs pour garder ce niveau.",
                        "Anticipez vos commandes pour les périodes de forte demande.",
                        "Bonne gestion des approvisionnements — gardez le rythme.")),
                "INFO",
                "fa-truck",
                "Voir Commandes"
            ));
        }

        
        
        
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
                    "Trop dépendant de quelques clients",
                    String.format(Locale.FRANCE,
                        "Vos 3 plus gros clients représentent %.1f %% de vos ventes (le premier, « %s », pèse à lui seul %.1f %%). Si l'un d'eux part, vos ventes chuteront fortement. %s",
                        concentration, topCustomerName, topShare,
                        pick(v,
                            "Cherchez de nouveaux clients pour réduire ce risque.",
                            "Lancez une action commerciale pour gagner de nouveaux clients.",
                            "Fidélisez ces clients clés avec des conditions privilégiées ou un contrat.")),
                    "HAUTE",
                    "fa-users",
                    "Voir Clients"
                ));
            }
        }

        
        
        
        
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
                recommendations.add(new RecommendationDto(
                    "PRODUITS_LENTS",
                    "Articles qui se vendent peu",
                    String.format(Locale.FRANCE,
                        "%d article(s) font partie des 5 qui se vendent le moins.%s %s",
                        slowCount, detail,
                        pick(v,
                            "Faites une promotion ou retirez-les du catalogue pour récupérer la trésorerie immobilisée dans le stock.",
                            "Vendez-les en lot avec d'autres articles pour les écouler plus vite.",
                            "Réduisez les quantités commandées pour ces articles afin de libérer de la trésorerie.")),
                    "MOYENNE",
                    "fa-boxes-stacked",
                    "Voir Produits"
                ));
            }
        }

        
        
        
        
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
                        "Trop de commandes annulées",
                        String.format(Locale.FRANCE,
                            "%.1f %% de vos commandes de vente sont annulées (%d annulations sur %d commandes). %s",
                            rate, cancelled, totalStatus,
                            pick(v,
                                "Vérifiez que le stock est bien disponible au moment où le client commande.",
                                "Demandez aux clients pourquoi ils annulent pour comprendre la cause.",
                                "Vérifiez vos délais de confirmation et la qualité des fiches articles.")),
                        rate > 25.0 ? "HAUTE" : "MOYENNE",
                        "fa-ban",
                        "Voir Commandes"
                    ));
                }
            }
        }

        
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
                            "Trop dépendant d'une seule famille de produits",
                            String.format(Locale.FRANCE,
                                "La famille « %s » représente à elle seule %.1f %% de vos ventes. Si cette famille perd des ventes, votre activité sera fortement impactée. %s",
                                topCat, topShare,
                                pick(v,
                                    "Développez d'autres familles de produits pour réduire ce risque.",
                                    "Choisissez une ou deux familles proches à développer en priorité.",
                                    "Mettez en avant les autres familles de produits pour mieux équilibrer vos ventes.")),
                            "MOYENNE",
                            "fa-layer-group",
                            "Voir Produits"
                        ));
                    }
                }
            }
        } catch (RuntimeException ignored) {
            
        }

        
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
                                "Trop dépendant d'un seul fournisseur",
                                String.format(Locale.FRANCE,
                                    "Le fournisseur « %s » représente %.1f %% de vos achats. Si ce fournisseur a un problème, vos approvisionnements seront bloqués. %s",
                                    name, share,
                                    pick(v,
                                        "Trouvez un fournisseur alternatif pour vos articles les plus importants.",
                                        "Mettez vos fournisseurs en concurrence pour obtenir de meilleurs prix.",
                                        "Sécurisez vos approvisionnements en ajoutant un deuxième fournisseur principal.")),
                                "MOYENNE",
                                "fa-truck-arrow-right",
                                "Voir Commandes"
                            ));
                        }
                    }
                    
                    
                    
                    Object[] best = null;
                    double bestScore = -1.0;
                    for (Object[] row : suppliers) {
                        if (row == null || row.length < 7) continue;
                        double rawNotation = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
                        
                        double notation = normaliseNotation(rawNotation);
                        double tauxLiv = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
                        
                        double score = notation * 20.0 + tauxLiv;
                        if (notation > 0 && score > bestScore) {
                            bestScore = score;
                            best = row;
                        }
                    }
                    if (best != null && best[1] != null && best[6] != null) {
                        double rawNotation = ((Number) best[6]).doubleValue();
                        
                        double notation = normaliseNotation(rawNotation);
                        double tauxLiv = best[5] != null ? ((Number) best[5]).doubleValue() : 0.0;
                        if (notation >= 4.0) {
                            recommendations.add(new RecommendationDto(
                                "FOURNISSEUR_PERFORMANT",
                                "Très bon fournisseur",
                                String.format(Locale.FRANCE,
                                    "Le fournisseur « %s » a une note de %.1f/5 et livre %.0f %% de ses commandes dans les délais. %s",
                                    best[1].toString(), notation, tauxLiv,
                                    pick(v,
                                        "Privilégiez-le pour vos articles les plus importants.",
                                        "Sécurisez votre relation avec un contrat de longue durée.",
                                        "Étendez votre collaboration à d'autres familles de produits.")),
                                "INFO",
                                "fa-medal",
                                "Voir Commandes"
                            ));
                        }
                    }
                }
            }
        } catch (RuntimeException ignored) {
            
        }

        
        if (totalSales != null && totalOrders != null && totalOrders > 0 && totalSales > 0) {
            double aov = totalSales / totalOrders;
            
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
                        "Vos clients dépensent moins par commande",
                        String.format(Locale.FRANCE,
                            "Sur les 90 derniers jours, vos clients dépensent en moyenne %,.0f par commande, soit %.0f %% de moins qu'avant (moyenne historique : %,.0f). %s",
                            recentAov, Math.abs(delta), aov,
                            pick(v,
                                "Proposez des lots ou des articles complémentaires pour augmenter le montant des commandes.",
                                "Limitez les remises pour préserver la valeur de chaque commande.",
                                "Identifiez les clients qui commandent moins et adaptez votre offre pour eux.")),
                        "MOYENNE",
                        "fa-cart-shopping",
                        "Analyser Ventes"
                    ));
                } else if (delta > 15.0) {
                    recommendations.add(new RecommendationDto(
                        "PANIER_MOYEN_HAUSSE",
                        "Vos clients dépensent plus par commande",
                        String.format(Locale.FRANCE,
                            "Sur les 90 derniers jours, vos clients dépensent en moyenne %,.0f par commande, soit %.0f %% de plus qu'avant (moyenne historique : %,.0f). %s",
                            recentAov, delta, aov,
                            pick(v,
                                "Profitez-en pour proposer des articles haut de gamme et des produits complémentaires.",
                                "Continuez à vendre ensemble les articles qui font monter le montant des commandes.",
                                "Identifiez ce qui marche actuellement et appliquez-le à tous vos clients.")),
                        "INFO",
                        "fa-cart-arrow-down",
                        "Analyser Ventes"
                    ));
                }
            }
        }

        
        
        
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
                    "Attention à votre trésorerie",
                    String.format(Locale.FRANCE,
                        "Sur les 30 derniers jours, vous avez dépensé %,.0f en achats pour seulement %,.0f de ventes (vous dépensez %.1f fois plus que ce que vous encaissez). %s",
                        recentPurchases, recentSales30, ratio,
                        pick(v,
                            "Étalez vos commandes fournisseurs ou demandez des délais de paiement plus longs.",
                            "Vérifiez que ces achats correspondent vraiment à la demande prévue.",
                            "Vendez d'abord le stock que vous avez avant de passer de nouvelles commandes.")),
                    "HAUTE",
                    "fa-wallet",
                    "Voir Commandes"
                ));
            }
        }

        
        try {
            Double stockValue = stockRepository.getTotalStockValue(entrepriseId);
            if (stockValue != null && stockValue > 0
                    && totalSales != null && totalSales > 0) {
                
                double monthlyAvgSales = last12MonthSales != null && last12MonthSales > 0
                        ? last12MonthSales / 12.0
                        : totalSales / 12.0;
                if (monthlyAvgSales > 0) {
                    double monthsOfStock = stockValue / monthlyAvgSales;
                    if (monthsOfStock > 6.0) {
                        recommendations.add(new RecommendationDto(
                            "VALEUR_STOCK",
                            "Trop de stock immobilisé",
                            String.format(Locale.FRANCE,
                                "Vous avez %,.0f de marchandise en stock, soit environ %.1f mois de ventes. C'est beaucoup de trésorerie bloquée. %s",
                                stockValue, monthsOfStock,
                                pick(v,
                                    "Repérez les articles en surstock pour libérer de la trésorerie.",
                                    "Lancez un déstockage sur les articles qui se vendent peu.",
                                    "Réduisez les quantités commandées sur les articles trop stockés.")),
                            "MOYENNE",
                            "fa-warehouse",
                            "Voir Stock"
                        ));
                    }
                }
            }
        } catch (RuntimeException ignored) {
            
        }

        
        if (totalOrders != null && totalOrders < 10) {
            recommendations.add(new RecommendationDto(
                "DONNEES_INSUFFISANTES",
                "Ajoutez plus de données",
                String.format(Locale.FRANCE,
                    "Vous n'avez que %d commande(s) enregistrée(s). C'est trop peu pour fournir des analyses fiables. %s",
                    totalOrders,
                    pick(v,
                        "Plus vous ajoutez de données, plus les analyses et prévisions seront précises.",
                        "Les prévisions automatiques s'activent à partir de 30 commandes — continuez à enrichir votre historique.",
                        "Importez votre historique de commandes pour activer toutes les prévisions et recommandations.")),
                "INFO",
                "fa-database",
                "Ajouter Commandes"
            ));
        }

        return recommendations;
    }

    




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
            
            
            double demandVariance = Math.max(1.0, avgMonthly * 0.5);
            double dailyDemand = avgMonthly / 30.0;
            double daysOfCover = dailyDemand > 0
                    ? stockLevel / dailyDemand
                    : (stockLevel > 0 ? 365.0 : 0.0);
            
            
            
            double leadTimeProxy = 7.0;
            double seasonalityPhase = (LocalDate.now().getMonthValue() - 1) / 11.0;
            String articleName = articleName(s);
            rows.add(new MlRecommendationEngine.StockRow(
                    articleName, stockLevel, avgMonthly, demandVariance,
                    leadTimeProxy, daysOfCover, seasonalityPhase));
        }
        List<RecommendationDto> all = mlRecommendationEngine.stockRiskRecommendations(
                rows, STOCK_RISK_HORIZON_DAYS);
        
        for (int i = 0; i < all.size() && i < MAX_ML_STOCK_RECOMMENDATIONS; i++) {
            out.add(all.get(i));
        }
        return out;
    }

    
    List<RecommendationDto> mlOpportunityRecommendations(List<Object[]> topProducts) {
        if (topProducts == null || topProducts.isEmpty()) return new ArrayList<>();
        
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
            
            
            double trend = n > 1 ? (double) (n - 1 - i) / (n - 1) - 0.5 : 0.0;
            rows.add(new MlRecommendationEngine.ProductRow(
                    name, rev, qty, marginProxy, catShare, trend));
        }
        return mlRecommendationEngine.opportunityRecommendations(rows);
    }

    private static double toDouble(BigDecimal b) {
        return b != null ? b.doubleValue() : 0.0;
    }

    





    private static double normaliseNotation(double rawNotation) {
        return rawNotation > 5.0 ? rawNotation / 20.0 : rawNotation;
    }

    private static String articleName(Stock s) {
        try {
            if (s.getArticleAchat() != null && s.getArticleAchat().getNom() != null) {
                return s.getArticleAchat().getNom();
            }
        } catch (RuntimeException ignored) {
            
        }
        return "Article #" + s.getId();
    }
}
