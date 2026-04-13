package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.AiDashboardDto;
import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiDashboardService {

    @Autowired
    private AnalyticService analyticService;

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private StockRepository stockRepository;

    public AiDashboardDto getAiDashboard(Long entrepriseId) {
        AiDashboardDto dashboard = new AiDashboardDto();

        dashboard.setSalesAnalysis(analyticService.analyzeSales(entrepriseId));
        ForecastDto forecast = forecastService.generateForecast(entrepriseId);
        dashboard.setForecast(forecast);
        ProductIntelligenceDto productIntelligence = analyticService.analyzeProducts(entrepriseId);
        dashboard.setProductIntelligence(productIntelligence);
        dashboard.setRecommendations(recommendationService.generateRecommendations(entrepriseId));
        dashboard.setKpiSummary(buildKpiSummary(entrepriseId));
        dashboard.setComparison(buildComparisonMetrics(entrepriseId));
        dashboard.setKpiDetails(buildKpiDetails(entrepriseId, productIntelligence, forecast));

        return dashboard;
    }

    private AiDashboardDto.KpiSummaryDto buildKpiSummary(Long entrepriseId) {
        AiDashboardDto.KpiSummaryDto kpi = new AiDashboardDto.KpiSummaryDto();

        int currentYear = LocalDate.now().getYear();
        int prevYear = currentYear - 1;

        Double totalRevenue = commandeVenteRepository.getTotalSalesRevenue(entrepriseId);
        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        kpi.setTotalRevenue(totalRevenue);

        // Revenue growth: compare this year vs last year
        LocalDate startCurrentYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endCurrentYear = LocalDate.of(currentYear, 12, 31);
        LocalDate startPrevYear = LocalDate.of(prevYear, 1, 1);
        LocalDate endPrevYear = LocalDate.of(prevYear, 12, 31);

        Double revenueCurrentYear = commandeVenteRepository.getSalesInPeriod(entrepriseId, startCurrentYear, endCurrentYear);
        Double revenuePrevYear = commandeVenteRepository.getSalesInPeriod(entrepriseId, startPrevYear, endPrevYear);
        revenueCurrentYear = revenueCurrentYear != null ? revenueCurrentYear : 0.0;
        revenuePrevYear = revenuePrevYear != null ? revenuePrevYear : 0.0;

        double revenueGrowth = revenuePrevYear > 0
            ? Math.round(((revenueCurrentYear - revenuePrevYear) / revenuePrevYear) * 1000.0) / 10.0
            : 0.0;
        kpi.setRevenueGrowth(revenueGrowth);

        Long totalOrders = commandeVenteRepository.getTotalSalesOrders(entrepriseId);
        kpi.setTotalOrders(totalOrders != null ? totalOrders : 0L);
        kpi.setOrderGrowth(0.0); // Simplified – would need date-filtered counts

        Long totalSKUs = stockRepository.getTotalSKUs(entrepriseId);
        kpi.setTotalProducts(totalSKUs != null ? totalSKUs : 0L);

        Long lowStock = stockRepository.getLowStockItemsCount(entrepriseId);
        kpi.setLowStockAlerts(lowStock != null ? lowStock : 0L);

        if (totalOrders != null && totalOrders > 0 && totalRevenue > 0) {
            kpi.setAvgOrderValue(Math.round((totalRevenue / totalOrders) * 100.0) / 100.0);
        } else {
            kpi.setAvgOrderValue(0.0);
        }

        // Profit margin estimate (revenue vs purchases proxy)
        kpi.setProfitMargin(0.0); // Populated in recommendations service context

        return kpi;
    }

    private AiDashboardDto.ComparisonMetricsDto buildComparisonMetrics(Long entrepriseId) {
        AiDashboardDto.ComparisonMetricsDto comparison = new AiDashboardDto.ComparisonMetricsDto();

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        // MTD: first day of current month to today
        LocalDate mtdStart = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate mtdEnd = today;

        // LTD: first day of last month to last day of last month
        LocalDate lastMonthStart = mtdStart.minusMonths(1);
        LocalDate lastMonthEnd = mtdStart.minusDays(1);

        Double mtd = commandeVenteRepository.getSalesInPeriod(entrepriseId, mtdStart, mtdEnd);
        Double ltd = commandeVenteRepository.getSalesInPeriod(entrepriseId, lastMonthStart, lastMonthEnd);
        mtd = mtd != null ? mtd : 0.0;
        ltd = ltd != null ? ltd : 0.0;

        comparison.setMtdVsLtd(buildPeriodComparison(mtd, ltd));

        // YTD: Jan 1 of current year to today
        LocalDate ytdStart = LocalDate.of(currentYear, 1, 1);
        LocalDate lastYearStart = LocalDate.of(currentYear - 1, 1, 1);
        LocalDate lastYearSameDay = LocalDate.of(currentYear - 1, today.getMonthValue(), today.getDayOfMonth());

        Double ytd = commandeVenteRepository.getSalesInPeriod(entrepriseId, ytdStart, today);
        Double lastYear = commandeVenteRepository.getSalesInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        ytd = ytd != null ? ytd : 0.0;
        lastYear = lastYear != null ? lastYear : 0.0;

        comparison.setYtdVsLastYear(buildPeriodComparison(ytd, lastYear));

        return comparison;
    }

    private AiDashboardDto.PeriodComparisonDto buildPeriodComparison(double current, double previous) {
        AiDashboardDto.PeriodComparisonDto dto = new AiDashboardDto.PeriodComparisonDto();
        dto.setCurrent(Math.round(current * 100.0) / 100.0);
        dto.setPrevious(Math.round(previous * 100.0) / 100.0);
        double change = current - previous;
        dto.setChange(Math.round(change * 100.0) / 100.0);
        double changePercent = previous > 0 ? Math.round((change / previous) * 1000.0) / 10.0 : 0.0;
        dto.setChangePercent(changePercent);
        if (changePercent > 1.0) {
            dto.setTrend("up");
        } else if (changePercent < -1.0) {
            dto.setTrend("down");
        } else {
            dto.setTrend("stable");
        }
        return dto;
    }

    private AiDashboardDto.KpiDetailsDto buildKpiDetails(Long entrepriseId,
            ProductIntelligenceDto productIntelligence, ForecastDto forecast) {
        AiDashboardDto.KpiDetailsDto details = new AiDashboardDto.KpiDetailsDto();

        // Revenue details
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

        // Orders details
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

        // Products details
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
            List<AiDashboardDto.NameValueDto> slowMovers = new ArrayList<>();
            if (productIntelligence.getSlowMovingProducts() != null) {
                for (ProductIntelligenceDto.ProductPerformanceDto p : productIntelligence.getSlowMovingProducts()) {
                    slowMovers.add(new AiDashboardDto.NameValueDto(p.getProductName(), (double) p.getQuantitySold()));
                }
            }
            products.setSlowMovers(slowMovers);
        }
        details.setProducts(products);

        // Forecast details
        AiDashboardDto.ForecastDetailsDto forecastDetails = new AiDashboardDto.ForecastDetailsDto();
        if (forecast != null) {
            forecastDetails.setAccuracy(forecast.getConfidenceScore());
            forecastDetails.setPredictedNextMonth(forecast.getPredictedNextMonth());
            forecastDetails.setPredictedNext3Months(forecast.getPredictedNext3Months());
            forecastDetails.setPredictedNext6Months(forecast.getPredictedNext6Months());
            forecastDetails.setMethodology(forecast.getMethodology());
        }
        details.setForecast(forecastDetails);

        return details;
    }
}
