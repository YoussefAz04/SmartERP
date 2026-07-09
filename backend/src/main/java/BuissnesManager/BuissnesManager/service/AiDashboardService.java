package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.AiDashboardDto;
import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.repository.CommandeAchatRepository;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiDashboardService {

    // Limite les variations affichees pour eviter des pourcentages extremes
    // quand la periode precedente est tres faible.
    private static final double MAX_CHANGE_PERCENT = 9999.0;

    @Autowired
    private AnalyticService analyticService;

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private CommandeAchatRepository commandeAchatRepository;

    @Autowired
    private StockRepository stockRepository;

    public AiDashboardDto getAiDashboard(Long entrepriseId) {
        AiDashboardDto dashboard = new AiDashboardDto();

        //  Analyse des ventes: CA mensuel, tendance, croissance et anomalies.
        dashboard.setSalesAnalysis(analyticService.analyzeSales(entrepriseId));

        //  Prevision du chiffre d'affaires: modele ML si possible, sinon fallback statistique.
        ForecastDto forecast = forecastService.generateForecast(entrepriseId);
        dashboard.setForecast(forecast);

        //  Intelligence produits: top produits, categories et segmentation de performance.
        ProductIntelligenceDto productIntelligence = analyticService.analyzeProducts(entrepriseId);
        dashboard.setProductIntelligence(productIntelligence);

        //  Recommandations actionnables: stock, ventes, marge, fournisseurs et risques.
        dashboard.setRecommendations(recommendationService.generateRecommendations(entrepriseId));

        //  Synthese KPI: cartes principales affichees en haut du dashboard.
        dashboard.setKpiSummary(buildKpiSummary(entrepriseId));

        // Comparaisons de periode: mois courant, annee courante, achats et marge.
        dashboard.setComparison(buildComparisonMetrics(entrepriseId));

        //  Details KPI: donnees utilisees par les vues detaillees du frontend.
        dashboard.setKpiDetails(buildKpiDetails(entrepriseId, productIntelligence, forecast));

        return dashboard;
    }

    // Construit les indicateurs de pilotage affiches dans les cartes principales.
    private AiDashboardDto.KpiSummaryDto buildKpiSummary(Long entrepriseId) {
        AiDashboardDto.KpiSummaryDto kpi = new AiDashboardDto.KpiSummaryDto();

        int currentYear = LocalDate.now().getYear();
        int prevYear = currentYear - 1;

        Double totalRevenue = commandeVenteRepository.getTotalSalesRevenue(entrepriseId);
        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        kpi.setTotalRevenue(totalRevenue);

        // Prepare les bornes annuelles pour comparer l'annee courante a l'annee precedente.
        LocalDate startCurrentYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endCurrentYear = LocalDate.of(currentYear, 12, 31);
        LocalDate startPrevYear = LocalDate.of(prevYear, 1, 1);
        LocalDate endPrevYear = LocalDate.of(prevYear, 12, 31);

        Double revenueCurrentYear = commandeVenteRepository.getSalesInPeriod(entrepriseId, startCurrentYear, endCurrentYear);
        Double revenuePrevYear = commandeVenteRepository.getSalesInPeriod(entrepriseId, startPrevYear, endPrevYear);
        revenueCurrentYear = revenueCurrentYear != null ? revenueCurrentYear : 0.0;
        revenuePrevYear = revenuePrevYear != null ? revenuePrevYear : 0.0;

        // Calcule la croissance du CA seulement si l'annee precedente existe.
        Double revenueGrowth = null;
        if (revenuePrevYear > 0) {
            double raw = ((revenueCurrentYear - revenuePrevYear) / revenuePrevYear) * 100.0;
            if (raw >  MAX_CHANGE_PERCENT) raw =  MAX_CHANGE_PERCENT;
            if (raw < -MAX_CHANGE_PERCENT) raw = -MAX_CHANGE_PERCENT;
            revenueGrowth = Math.round(raw * 10.0) / 10.0;
        }
        kpi.setRevenueGrowth(revenueGrowth);

        Long totalOrders = commandeVenteRepository.getTotalSalesOrders(entrepriseId);
        kpi.setTotalOrders(totalOrders != null ? totalOrders : 0L);

        // Calcule la croissance du nombre de commandes sur le meme principe que le CA.
        Long ordersCurrentYear = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, startCurrentYear, endCurrentYear);
        Long ordersPrevYear = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, startPrevYear, endPrevYear);
        long ocy = ordersCurrentYear != null ? ordersCurrentYear : 0L;
        long opy = ordersPrevYear != null ? ordersPrevYear : 0L;

        Double orderGrowth = null;
        if (opy > 0) {
            double raw = ((ocy - opy) / (double) opy) * 100.0;
            if (raw >  MAX_CHANGE_PERCENT) raw =  MAX_CHANGE_PERCENT;
            if (raw < -MAX_CHANGE_PERCENT) raw = -MAX_CHANGE_PERCENT;
            orderGrowth = Math.round(raw * 10.0) / 10.0;
        }
        kpi.setOrderGrowth(orderGrowth);

        Long totalSKUs = stockRepository.getTotalSKUs(entrepriseId);
        kpi.setTotalProducts(totalSKUs != null ? totalSKUs : 0L);

        Long lowStock = stockRepository.getLowStockItemsCount(entrepriseId);
        kpi.setLowStockAlerts(lowStock != null ? lowStock : 0L);

        // Panier moyen = CA total / nombre total de commandes.
        if (totalOrders != null && totalOrders > 0 && totalRevenue > 0) {
            kpi.setAvgOrderValue(Math.round((totalRevenue / totalOrders) * 100.0) / 100.0);
        } else {
            kpi.setAvgOrderValue(0.0);
        }

        // Marge estimee: CA total moins cout d'achat des produits vendus.
        Double cogsAllTime = commandeVenteRepository.getCOGSInPeriod(
                entrepriseId, LocalDate.of(1970, 1, 1), LocalDate.now());
        if (totalRevenue > 0 && cogsAllTime != null) {
            double profitMargin = ((totalRevenue - cogsAllTime) / totalRevenue) * 100.0;
            kpi.setProfitMargin(Math.round(profitMargin * 10.0) / 10.0);
        } else {
            kpi.setProfitMargin(0.0);
        }

        return kpi;
    }

    // Construit les comparaisons temporelles utilisees par les cartes d'evolution.
    private AiDashboardDto.ComparisonMetricsDto buildComparisonMetrics(Long entrepriseId) {
        AiDashboardDto.ComparisonMetricsDto comparison = new AiDashboardDto.ComparisonMetricsDto();

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        // Periode MTD: du debut du mois courant jusqu'a aujourd'hui.
        LocalDate mtdStart = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate mtdEnd = today;

        // Periode LTD: mois precedent complet, utilise comme reference courte.
        LocalDate lastMonthStart = mtdStart.minusMonths(1);
        LocalDate lastMonthEnd = mtdStart.minusDays(1); 

        Double mtd = commandeVenteRepository.getSalesInPeriod(entrepriseId, mtdStart, mtdEnd);
        Double ltd = commandeVenteRepository.getSalesInPeriod(entrepriseId, lastMonthStart, lastMonthEnd);
        mtd = mtd != null ? mtd : 0.0;
        ltd = ltd != null ? ltd : 0.0;

        comparison.setMtdVsLtd(buildPeriodComparison(mtd, ltd));

        // Periode YTD: annee courante jusqu'a aujourd'hui, comparee au meme jour N-1.
        LocalDate ytdStart = LocalDate.of(currentYear, 1, 1);
        LocalDate lastYearStart = LocalDate.of(currentYear - 1, 1, 1);
        LocalDate lastYearSameDay = today.minusYears(1);

        Double ytd = commandeVenteRepository.getSalesInPeriod(entrepriseId, ytdStart, today);
        Double lastYear = commandeVenteRepository.getSalesInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        ytd = ytd != null ? ytd : 0.0;
        lastYear = lastYear != null ? lastYear : 0.0;

        comparison.setYtdVsLastYear(buildPeriodComparison(ytd, lastYear));

        // Compare les achats sur l'annee courante et l'annee precedente.
        Double purchaseYtd = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, ytdStart, today);
        Double purchaseLastYear = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        purchaseYtd = purchaseYtd != null ? purchaseYtd : 0.0;
        purchaseLastYear = purchaseLastYear != null ? purchaseLastYear : 0.0;
        comparison.setPurchaseYtd(buildPeriodComparison(purchaseYtd, purchaseLastYear));

        // Calcule la marge brute YTD a partir du CA et du cout d'achat vendu.
        Double cogsYtd = commandeVenteRepository.getCOGSInPeriod(entrepriseId, ytdStart, today);
        Double cogsLastYear = commandeVenteRepository.getCOGSInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        cogsYtd = cogsYtd != null ? cogsYtd : 0.0;
        cogsLastYear = cogsLastYear != null ? cogsLastYear : 0.0;
        double grossMargin = ytd - cogsYtd;
        comparison.setGrossMarginYtd(Math.round(grossMargin * 100.0) / 100.0);

        // Compte les commandes de vente et d'achat sur l'annee courante.
        Long salesOrderCount = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, ytdStart, today);
        comparison.setSalesOrderCountYtd(salesOrderCount != null ? salesOrderCount : 0L);

        Long purchaseOrderCount = commandeAchatRepository.getPurchaseOrderCountInPeriod(entrepriseId, ytdStart, today);
        comparison.setPurchaseOrderCountYtd(purchaseOrderCount != null ? purchaseOrderCount : 0L);

        // Compare les achats du mois courant au mois precedent.
        Double purchaseMtd = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, mtdStart, mtdEnd);
        Double purchaseLtd = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, lastMonthStart, lastMonthEnd);
        purchaseMtd = purchaseMtd != null ? purchaseMtd : 0.0;
        purchaseLtd = purchaseLtd != null ? purchaseLtd : 0.0;
        comparison.setPurchaseMtdVsLtd(buildPeriodComparison(purchaseMtd, purchaseLtd));

        // Compare la marge du mois courant a celle du mois precedent.
        Double cogsMtd = commandeVenteRepository.getCOGSInPeriod(entrepriseId, mtdStart, mtdEnd);
        Double cogsLtd = commandeVenteRepository.getCOGSInPeriod(entrepriseId, lastMonthStart, lastMonthEnd);
        cogsMtd = cogsMtd != null ? cogsMtd : 0.0;
        cogsLtd = cogsLtd != null ? cogsLtd : 0.0;
        comparison.setMarginMtdVsLtd(buildPeriodComparison(mtd - cogsMtd, ltd - cogsLtd));

        // Compare la marge YTD a la marge sur la meme periode l'annee precedente.
        comparison.setMarginYtdVsLastYear(buildPeriodComparison(ytd - cogsYtd, lastYear - cogsLastYear));

        // Compare le nombre de commandes du mois courant au mois precedent.
        Long orderCountMtd = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, mtdStart, mtdEnd);
        Long orderCountLtd = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, lastMonthStart, lastMonthEnd);
        comparison.setOrderCountMtdVsLtd(buildPeriodComparison(
                orderCountMtd != null ? orderCountMtd.doubleValue() : 0.0,
                orderCountLtd != null ? orderCountLtd.doubleValue() : 0.0
        ));

        // Compare le nombre de commandes YTD au meme intervalle de l'annee precedente.
        Long orderCountLastYear = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        comparison.setOrderCountYtdVsLastYear(buildPeriodComparison(
                salesOrderCount != null ? salesOrderCount.doubleValue() : 0.0,
                orderCountLastYear != null ? orderCountLastYear.doubleValue() : 0.0
        ));

        return comparison;
    }

    // Normalise une comparaison: valeur actuelle, reference, difference, pourcentage et tendance.
    private AiDashboardDto.PeriodComparisonDto buildPeriodComparison(double current, double previous) {
        AiDashboardDto.PeriodComparisonDto dto = new AiDashboardDto.PeriodComparisonDto();
        dto.setCurrent(Math.round(current * 100.0) / 100.0);
        dto.setPrevious(Math.round(previous * 100.0) / 100.0);
        double change = current - previous;
        dto.setChange(Math.round(change * 100.0) / 100.0);

        if (previous == 0.0) {
            // Quand la reference vaut zero, le pourcentage n'est pas defini.
            dto.setChangePercent(null);
            if (current > 0) {
                dto.setTrend("up");
            } else if (current < 0) {
                dto.setTrend("down");
            } else {
                dto.setTrend("stable");
            }
        } else {
            double changePercent = Math.round((change / Math.abs(previous)) * 1000.0) / 10.0;

            if (changePercent >  MAX_CHANGE_PERCENT) changePercent =  MAX_CHANGE_PERCENT;
            if (changePercent < -MAX_CHANGE_PERCENT) changePercent = -MAX_CHANGE_PERCENT;
            dto.setChangePercent(changePercent);
            if (changePercent > 1.0) {
                dto.setTrend("up");
            } else if (changePercent < -1.0) {
                dto.setTrend("down");
            } else {
                dto.setTrend("stable");
            }
        }
        return dto;
    }

    // Prepare les donnees detaillees qui alimentent les drill-down du dashboard IA.
    private AiDashboardDto.KpiDetailsDto buildKpiDetails(Long entrepriseId,
            ProductIntelligenceDto productIntelligence, ForecastDto forecast) {
        AiDashboardDto.KpiDetailsDto details = new AiDashboardDto.KpiDetailsDto();

        // Detail revenu: top produits et top clients.
        AiDashboardDto.RevenueDetailsDto revenue = new AiDashboardDto.RevenueDetailsDto();
        List<AiDashboardDto.NameValueDto> topProducts = new ArrayList<>();
        if (productIntelligence != null && productIntelligence.getTopProducts() != null) {
            for (ProductIntelligenceDto.ProductPerformanceDto p : productIntelligence.getTopProducts()) {
                topProducts.add(new AiDashboardDto.NameValueDto(p.getProductName(), p.getRevenue()));
            }
        }
        revenue.setTopProducts(topProducts);

        List<AiDashboardDto.NameValueDto> topCustomers = new ArrayList<>();
        List<Object[]> rawCustomers = commandeVenteRepository.getTopCustomers(entrepriseId);
        for (Object[] row : rawCustomers) {
            String name = row[0] != null ? row[0].toString() : "Inconnu";
            Double rev = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            topCustomers.add(new AiDashboardDto.NameValueDto(name, Math.round(rev * 100.0) / 100.0));
            if (topCustomers.size() >= 10) break;
        }
        revenue.setTopCustomers(topCustomers);
        revenue.setPaymentMethods(new ArrayList<>());
        details.setRevenue(revenue);

        // Detail commandes: repartition par statut, total, CA et panier moyen.
        AiDashboardDto.OrdersDetailsDto orders = new AiDashboardDto.OrdersDetailsDto();
        List<AiDashboardDto.StatusCountDto> byStatus = new ArrayList<>();
        List<Object[]> rawStatus = commandeVenteRepository.getOrderStatusBreakdown(entrepriseId);
        for (Object[] row : rawStatus) {
            String status = row[0] != null ? row[0].toString() : "Inconnu";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            byStatus.add(new AiDashboardDto.StatusCountDto(status, count, null));
        }
        orders.setByStatus(byStatus);
        Long totalOrders = commandeVenteRepository.getTotalSalesOrders(entrepriseId);
        orders.setTotalOrders(totalOrders != null ? totalOrders : 0L);
        Double totalRevenue = commandeVenteRepository.getTotalSalesRevenue(entrepriseId);
        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        orders.setTotalRevenue(totalRevenue);
        if (totalOrders != null && totalOrders > 0) {
            orders.setAvgOrderValue(Math.round((totalRevenue / totalOrders) * 100.0) / 100.0);
        } else {
            orders.setAvgOrderValue(0.0);
        }
        details.setOrders(orders);

        // Detail produits: stock total, alertes, ruptures, valeur de stock et rotations.
        AiDashboardDto.ProductsDetailsDto products = new AiDashboardDto.ProductsDetailsDto();
        Long totalSKUs = stockRepository.getTotalSKUs(entrepriseId);
        products.setTotalSKUs(totalSKUs != null ? totalSKUs : 0L);
        Long lowStock = stockRepository.getLowStockItemsCount(entrepriseId);
        products.setLowStockCount(lowStock != null ? lowStock : 0L);
        Long outOfStock = stockRepository.getOutOfStockItemsCount(entrepriseId);
        products.setOutOfStockCount(outOfStock != null ? outOfStock : 0L);
        Double stockValue = stockRepository.getTotalStockValue(entrepriseId);
        products.setTotalStockValue(stockValue != null ? Math.round(stockValue * 100.0) / 100.0 : 0.0);
        if (productIntelligence != null) {
            List<AiDashboardDto.NameValueDto> fastMovers = new ArrayList<>();
            if (productIntelligence.getTopProducts() != null) {
                for (ProductIntelligenceDto.ProductPerformanceDto p : productIntelligence.getTopProducts()) {
                    fastMovers.add(new AiDashboardDto.NameValueDto(p.getProductName(), (double) p.getQuantitySold()));
                }
            }
            products.setTopProducts(fastMovers);
        }
        details.setProducts(products);

        // Detail prevision: score de confiance, horizons et metriques de validation.
        AiDashboardDto.ForecastDetailsDto forecastDetails = new AiDashboardDto.ForecastDetailsDto();
        if (forecast != null) {
            forecastDetails.setAccuracy(forecast.getConfidenceScore());
            forecastDetails.setPredictedNextMonth(forecast.getPredictedNextMonth());
            forecastDetails.setPredictedNext3Months(forecast.getPredictedNext3Months());
            forecastDetails.setPredictedNext6Months(forecast.getPredictedNext6Months());
            forecastDetails.setMethodology(forecast.getMethodology());
            forecastDetails.setSelectedModel(forecast.getSelectedModel());
            forecastDetails.setCrossValidatedMape(forecast.getCrossValidatedMape());
            forecastDetails.setCrossValidatedSmape(forecast.getCrossValidatedSmape());
            forecastDetails.setCrossValidatedRmse(forecast.getCrossValidatedRmse());
        }
        details.setForecast(forecastDetails);

        return details;
    }

}
