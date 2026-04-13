package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.AiDashboardDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
        dashboard.setForecast(forecastService.generateForecast(entrepriseId));
        dashboard.setProductIntelligence(analyticService.analyzeProducts(entrepriseId));
        dashboard.setRecommendations(recommendationService.generateRecommendations(entrepriseId));
        dashboard.setKpiSummary(buildKpiSummary(entrepriseId));

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
}
