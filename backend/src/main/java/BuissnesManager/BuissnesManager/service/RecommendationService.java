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
                            " Most affected product: \"%s\". Place a supplier order today to avoid losing sales.",
                            worstStockArticle)
                    : " Place the necessary supplier orders today to avoid losing sales.";
            String headline = pick(v,
                    outOfStockCount + " product(s) are completely out of stock.",
                    outOfStockCount + " product(s) have zero stock.",
                    "Zero stock for " + outOfStockCount + " article(s).");
            recommendations.add(new RecommendationDto(
                "STOCK_CRITIQUE",
                "Stockout Detected",
                headline + detail,
                "CRITIQUE",
                "fa-exclamation-triangle",
                "View Stock"
            ));
        }

        if (lowStockCount != null && lowStockCount > 0) {
            String detail = worstStockArticle != null && (outOfStockCount == null || outOfStockCount == 0)
                    ? String.format(Locale.FRANCE,
                            " Most exposed product: \"%s\". Order before it reaches zero.",
                            worstStockArticle)
                    : " Place supplier orders now to avoid a stockout.";
            String headline = pick(v,
                    lowStockCount + " product(s) have low stock and must be restocked.",
                    lowStockCount + " product(s) are below the alert threshold.",
                    lowStockCount + " product(s) may soon run out of stock.");
            recommendations.add(new RecommendationDto(
                "STOCK_FAIBLE",
                "Low Stock - Action Required",
                headline + detail,
                "HAUTE",
                "fa-boxes",
                "Manage Stock"
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
                        "Launch priority sales actions and follow up with existing customers.",
                        "Contact inactive customers and offer a targeted discount to restart sales.",
                        "Reconnect with former customers and relaunch your sales campaigns.");
                recommendations.add(new RecommendationDto(
                    "VENTES_FAIBLES",
                    "Monthly Sales Behind Target",
                    String.format(Locale.FRANCE,
                        "This month's sales are %.0f %% below your usual average (actual: %,.0f - expected at this stage of the month: %,.0f). %s",
                        shortfallPct, salesThisMonth, expectedMtd, levers),
                    "MOYENNE",
                    "fa-chart-line",
                    "Analyze Sales"
                ));
            } else if (expectedMtd > 0 && now.getDayOfMonth() >= 10
                    && salesThisMonth > 1.3 * expectedMtd) {
                double surgePct = ((salesThisMonth - expectedMtd) / expectedMtd) * 100.0;
                String advice = pick(v,
                        "Restock your best products to avoid stockouts.",
                        "Increase supplier orders before the end of the month to keep up with demand.",
                        "Use this momentum to retain new customers.");
                recommendations.add(new RecommendationDto(
                    "TENDANCE_CROISSANCE",
                    "Monthly Sales Rising Strongly",
                    String.format(Locale.FRANCE,
                        "This month's sales are %.0f %% above your usual average (actual: %,.0f - expected at this stage of the month: %,.0f). %s",
                        surgePct, salesThisMonth, expectedMtd, advice),
                    "INFO",
                    "fa-rocket",
                    "Analyze Sales"
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
                "Best-Selling Product",
                String.format(Locale.FRANCE,
                    "\"%s\" is the best-selling product. Always keep enough stock and offer it with complementary products to increase sales.",
                    topProduct),
                "BASSE",
                "fa-star",
                "View Products"
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
                        "Renegotiate purchase prices with suppliers or increase selling prices on your best-selling products.",
                        "Identify products sold at a loss or with very low margins and adjust their selling price.",
                        "Compare purchase prices with other suppliers to get better terms.");
                recommendations.add(new RecommendationDto(
                    "MARGE_FAIBLE",
                    "Margin Too Low",
                    String.format(Locale.FRANCE,
                        "Your margin is only %s %%. On %,.0f in sales, purchases cost %,.0f, leaving only %,.0f in profit. %s",
                        marginStr, totalSales, cogs, grossProfit, levers),
                    "HAUTE",
                    "fa-percentage",
                    "Analyze Purchases"
                ));
            } else if (margin < 35.0) {
                recommendations.add(new RecommendationDto(
                    "MARGE_CORRECTE",
                    "Healthy Margin",
                    String.format(Locale.FRANCE,
                        "Your margin is %s %%. On %,.0f in sales, you earn %,.0f. %s",
                        marginStr, totalSales, grossProfit,
                        pick(v,
                            "Aim for a 35 %% margin to improve financial safety.",
                            "Negotiate better purchase prices to reach a 35 %% margin.",
                            "Identify low-margin products to improve overall results.")),
                    "INFO",
                    "fa-percentage",
                    "View Details"
                ));
            } else {
                recommendations.add(new RecommendationDto(
                    "BONNE_PERFORMANCE",
                    "Very Good Margin",
                    String.format(Locale.FRANCE,
                        "Your margin is excellent: %s %%. On %,.0f in sales, you earn %,.0f. %s",
                        marginStr, totalSales, grossProfit,
                        pick(v,
                            "Keep negotiating purchase prices well to maintain this margin.",
                            "Use this profit to retain customers and grow the business.",
                            "Keep these good purchase prices by signing long-term agreements with your best suppliers.")),
                    "INFO",
                    "fa-thumbs-up",
                    "View Details"
                ));
            }
        } else if (totalSales != null && totalPurchases != null
                && totalSales > 0 && totalPurchases > 0) {
            double ratio = (totalPurchases / totalSales) * 100.0;
            recommendations.add(new RecommendationDto(
                "RATIO_ACHATS",
                "Purchase / Sales Comparison",
                String.format(Locale.FRANCE,
                    "Purchases represent %.1f %% of sales (purchases: %,.0f - sales: %,.0f). Enter each product's purchase price so the system can calculate your real margin automatically.",
                    ratio, totalPurchases, totalSales),
                "INFO",
                "fa-balance-scale",
                "View Details"
            ));
        }

        
        Long overdueOrders = commandeAchatRepository.getOverdueOrdersCount(entrepriseId, now);
        Long activeOrders = commandeAchatRepository.getActiveOrdersCount(entrepriseId);
        if (overdueOrders != null && overdueOrders > 0) {
            String urgency = overdueOrders > 5 ? "HAUTE" : "MOYENNE";
            String icon = overdueOrders > 5 ? "fa-exclamation-circle" : "fa-clock";
            String advice = pick(v,
                    "Contact suppliers to get the new delivery date.",
                    "Ask suppliers for a firm delivery date and change supplier if needed.",
                    "Identify suppliers that are often late and review your terms with them.");
            recommendations.add(new RecommendationDto(
                "LIVRAISON_RETARD",
                "Overdue Orders",
                String.format(Locale.FRANCE,
                    "%d supplier order(s) passed the expected delivery date. %s",
                    overdueOrders, advice),
                urgency,
                icon,
                "View Orders"
            ));
        } else if (activeOrders != null && activeOrders > 0) {
            recommendations.add(new RecommendationDto(
                "LIVRAISON_OK",
                "Deliveries On Track",
                String.format(Locale.FRANCE,
                    "All current supplier orders (%d) will be delivered on time. %s",
                    activeOrders,
                    pick(v,
                        "Keep tracking supplier confirmations to maintain this level.",
                        "Plan orders ahead for high-demand periods.",
                        "Good supply management - keep the pace.")),
                "INFO",
                "fa-truck",
                "View Orders"
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
                    "Too Dependent on a Few Customers",
                    String.format(Locale.FRANCE,
                        "Your top 3 customers represent %.1f %% of sales (the first, \"%s\", alone represents %.1f %%). If one leaves, sales could drop sharply. %s",
                        concentration, topCustomerName, topShare,
                        pick(v,
                            "Find new customers to reduce this risk.",
                            "Launch a sales action to win new customers.",
                            "Retain these key customers with preferred terms or a contract.")),
                    "HAUTE",
                    "fa-users",
                    "View Customers"
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
                        "Too Many Canceled Orders",
                        String.format(Locale.FRANCE,
                            "%.1f %% of sales orders are canceled (%d cancellations out of %d orders). %s",
                            rate, cancelled, totalStatus,
                            pick(v,
                                "Check that stock is available when the customer orders.",
                                "Ask customers why they cancel to understand the cause.",
                                "Check confirmation delays and product sheet quality.")),
                        rate > 25.0 ? "HAUTE" : "MOYENNE",
                        "fa-ban",
                        "View Orders"
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
                            "Too Dependent on One Product Family",
                            String.format(Locale.FRANCE,
                                "The \"%s\" family alone represents %.1f %% of sales. If this family loses sales, the business will be strongly affected. %s",
                                topCat, topShare,
                                pick(v,
                                    "Develop other product families to reduce this risk.",
                                    "Choose one or two related families to develop first.",
                                    "Highlight other product families to better balance sales.")),
                            "MOYENNE",
                            "fa-layer-group",
                            "View Products"
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
                                "Too Dependent on One Supplier",
                                String.format(Locale.FRANCE,
                                    "Supplier \"%s\" represents %.1f %% of purchases. If this supplier has a problem, supply will be blocked. %s",
                                    name, share,
                                    pick(v,
                                        "Find an alternative supplier for your most important products.",
                                        "Put suppliers in competition to get better prices.",
                                        "Secure supply by adding a second main supplier.")),
                                "MOYENNE",
                                "fa-truck-arrow-right",
                                "View Orders"
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
                                "Very Good Supplier",
                                String.format(Locale.FRANCE,
                                    "Supplier \"%s\" has a %.1f/5 rating and delivers %.0f %% of orders on time. %s",
                                    best[1].toString(), notation, tauxLiv,
                                    pick(v,
                                        "Prioritize this supplier for your most important products.",
                                        "Secure the relationship with a long-term contract.",
                                        "Extend collaboration to other product families.")),
                                "INFO",
                                "fa-medal",
                                "View Orders"
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
                        "Customers Spend Less per Order",
                        String.format(Locale.FRANCE,
                            "Over the last 90 days, customers spend an average of %,.0f per order, %.0f %% less than before (historical average: %,.0f). %s",
                            recentAov, Math.abs(delta), aov,
                            pick(v,
                                "Offer bundles or complementary products to increase order value.",
                                "Limit discounts to preserve each order's value.",
                                "Identify customers who order less and adapt your offer for them.")),
                        "MOYENNE",
                        "fa-cart-shopping",
                        "Analyze Sales"
                    ));
                } else if (delta > 15.0) {
                    recommendations.add(new RecommendationDto(
                        "PANIER_MOYEN_HAUSSE",
                        "Customers Spend More per Order",
                        String.format(Locale.FRANCE,
                            "Over the last 90 days, customers spend an average of %,.0f per order, %.0f %% more than before (historical average: %,.0f). %s",
                            recentAov, delta, aov,
                            pick(v,
                                "Use this to offer premium products and complementary products.",
                                "Keep selling together the products that increase order value.",
                                "Identify what currently works and apply it to all customers.")),
                        "INFO",
                        "fa-cart-arrow-down",
                        "Analyze Sales"
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
                    "Cash Flow Warning",
                    String.format(Locale.FRANCE,
                        "Over the last 30 days, you spent %,.0f on purchases for only %,.0f in sales (you spend %.1f times more than you collect). %s",
                        recentPurchases, recentSales30, ratio,
                        pick(v,
                            "Spread supplier orders over time or request longer payment terms.",
                            "Check that these purchases really match expected demand.",
                            "Sell existing stock before placing new orders.")),
                    "HAUTE",
                    "fa-wallet",
                    "View Orders"
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
                            "Too Much Stock Tied Up",
                            String.format(Locale.FRANCE,
                                "You have %,.0f worth of goods in stock, about %.1f months of sales. This is a lot of cash tied up. %s",
                                stockValue, monthsOfStock,
                                pick(v,
                                    "Identify overstocked products to free up cash.",
                                    "Run a clearance action on slow-selling products.",
                                    "Reduce ordered quantities for overstocked products.")),
                            "MOYENNE",
                            "fa-warehouse",
                            "View Stock"
                        ));
                    }
                }
            }
        } catch (RuntimeException ignored) {
            
        }

        
        if (totalOrders != null && totalOrders < 10) {
            recommendations.add(new RecommendationDto(
                "DONNEES_INSUFFISANTES",
                "Add More Data",
                String.format(Locale.FRANCE,
                    "You only have %d recorded order(s). This is too little to provide reliable analysis. %s",
                    totalOrders,
                    pick(v,
                        "The more data you add, the more accurate analysis and forecasts become.",
                        "Automatic forecasts activate from 30 orders - keep enriching your history.",
                        "Import your order history to activate all forecasts and recommendations.")),
                "INFO",
                "fa-database",
                "Add Orders"
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
