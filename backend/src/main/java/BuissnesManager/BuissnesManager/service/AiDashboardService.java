package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.AiDashboardDto;
import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.repository.CommandeAchatRepository;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import BuissnesManager.BuissnesManager.service.ml.SupplierRatingEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private CommandeAchatRepository commandeAchatRepository;

    @Autowired
    private StockRepository stockRepository;

    private final SupplierRatingEngine supplierRatingEngine = new SupplierRatingEngine();

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
        dashboard.setSupplierPerformance(buildSupplierPerformanceMetrics(entrepriseId));

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

        // Year-over-year revenue growth. When the prior year has no data we
        // report null (nullable Double) rather than 0.0 so the UI can show a
        // "NOUV." badge instead of misleading "0% growth". We also cap extreme
        // ratios (tiny prev-year anchor → millions of % growth) to ±9999%.
        Double revenueGrowth = null;
        if (revenuePrevYear > 0) {
            double raw = ((revenueCurrentYear - revenuePrevYear) / revenuePrevYear) * 100.0;
            if (raw >  9999.0) raw =  9999.0;
            if (raw < -9999.0) raw = -9999.0;
            revenueGrowth = Math.round(raw * 10.0) / 10.0;
        }
        kpi.setRevenueGrowth(revenueGrowth);

        Long totalOrders = commandeVenteRepository.getTotalSalesOrders(entrepriseId);
        kpi.setTotalOrders(totalOrders != null ? totalOrders : 0L);

        // Order growth: # of orders this year vs last year
        Long ordersCurrentYear = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, startCurrentYear, endCurrentYear);
        Long ordersPrevYear = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, startPrevYear, endPrevYear);
        long ocy = ordersCurrentYear != null ? ordersCurrentYear : 0L;
        long opy = ordersPrevYear != null ? ordersPrevYear : 0L;
        // Same treatment as revenueGrowth: null for "no prior data" and ±9999%
        // clamp so a single prior-year order doesn't produce absurd percentages.
        Double orderGrowth = null;
        if (opy > 0) {
            double raw = ((ocy - opy) / (double) opy) * 100.0;
            if (raw >  9999.0) raw =  9999.0;
            if (raw < -9999.0) raw = -9999.0;
            orderGrowth = Math.round(raw * 10.0) / 10.0;
        }
        kpi.setOrderGrowth(orderGrowth);

        Long totalSKUs = stockRepository.getTotalSKUs(entrepriseId);
        kpi.setTotalProducts(totalSKUs != null ? totalSKUs : 0L);

        Long lowStock = stockRepository.getLowStockItemsCount(entrepriseId);
        kpi.setLowStockAlerts(lowStock != null ? lowStock : 0L);

        if (totalOrders != null && totalOrders > 0 && totalRevenue > 0) {
            kpi.setAvgOrderValue(Math.round((totalRevenue / totalOrders) * 100.0) / 100.0);
        } else {
            kpi.setAvgOrderValue(0.0);
        }

        // Profit margin: (revenue - COGS) / revenue. Uses the same "cost of goods sold"
        // logic as the YTD gross-margin comparison so we don't treat inventory build-up
        // as a loss. All-time window so it matches totalRevenue.
        // NOTE: when the COGS query returns null (no purchase data tracked at all),
        // treating it as 0 would falsely report a 100% profit margin. In that case we
        // report 0.0 so the UI doesn't mislead the user. A legitimate COGS of 0 (digital
        // goods, etc.) still resolves to a real 100% margin.
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
        // Use minusYears(1) so that Feb 29 in a leap year is safely mapped to Feb 28
        // in the previous non-leap year instead of throwing DateTimeException.
        LocalDate lastYearSameDay = today.minusYears(1);

        Double ytd = commandeVenteRepository.getSalesInPeriod(entrepriseId, ytdStart, today);
        Double lastYear = commandeVenteRepository.getSalesInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        ytd = ytd != null ? ytd : 0.0;
        lastYear = lastYear != null ? lastYear : 0.0;

        comparison.setYtdVsLastYear(buildPeriodComparison(ytd, lastYear));

        // Purchase YTD vs last year same period (money spent buying inventory)
        Double purchaseYtd = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, ytdStart, today);
        Double purchaseLastYear = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        purchaseYtd = purchaseYtd != null ? purchaseYtd : 0.0;
        purchaseLastYear = purchaseLastYear != null ? purchaseLastYear : 0.0;
        comparison.setPurchaseYtd(buildPeriodComparison(purchaseYtd, purchaseLastYear));

        // Gross margin YTD = sales YTD - COGS YTD (cost of items actually sold),
        // NOT sales - total purchases (which would incorrectly count stock build-up as a loss).
        Double cogsYtd = commandeVenteRepository.getCOGSInPeriod(entrepriseId, ytdStart, today);
        Double cogsLastYear = commandeVenteRepository.getCOGSInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        cogsYtd = cogsYtd != null ? cogsYtd : 0.0;
        cogsLastYear = cogsLastYear != null ? cogsLastYear : 0.0;
        double grossMargin = ytd - cogsYtd;
        comparison.setGrossMarginYtd(Math.round(grossMargin * 100.0) / 100.0);

        // Order counts YTD
        Long salesOrderCount = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, ytdStart, today);
        comparison.setSalesOrderCountYtd(salesOrderCount != null ? salesOrderCount : 0L);

        Long purchaseOrderCount = commandeAchatRepository.getPurchaseOrderCountInPeriod(entrepriseId, ytdStart, today);
        comparison.setPurchaseOrderCountYtd(purchaseOrderCount != null ? purchaseOrderCount : 0L);

        // === Enhanced MTD multi-dimensional metrics ===

        // Purchase MTD vs LTD
        Double purchaseMtd = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, mtdStart, mtdEnd);
        Double purchaseLtd = commandeAchatRepository.getPurchasesInPeriod(entrepriseId, lastMonthStart, lastMonthEnd);
        purchaseMtd = purchaseMtd != null ? purchaseMtd : 0.0;
        purchaseLtd = purchaseLtd != null ? purchaseLtd : 0.0;
        comparison.setPurchaseMtdVsLtd(buildPeriodComparison(purchaseMtd, purchaseLtd));

        // Gross margin MTD vs LTD = sales - COGS for each period
        Double cogsMtd = commandeVenteRepository.getCOGSInPeriod(entrepriseId, mtdStart, mtdEnd);
        Double cogsLtd = commandeVenteRepository.getCOGSInPeriod(entrepriseId, lastMonthStart, lastMonthEnd);
        cogsMtd = cogsMtd != null ? cogsMtd : 0.0;
        cogsLtd = cogsLtd != null ? cogsLtd : 0.0;
        comparison.setMarginMtdVsLtd(buildPeriodComparison(mtd - cogsMtd, ltd - cogsLtd));

        // Gross margin YTD vs last year same period (sales - COGS)
        comparison.setMarginYtdVsLastYear(buildPeriodComparison(ytd - cogsYtd, lastYear - cogsLastYear));

        // Sales order count MTD vs LTD
        Long orderCountMtd = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, mtdStart, mtdEnd);
        Long orderCountLtd = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, lastMonthStart, lastMonthEnd);
        comparison.setOrderCountMtdVsLtd(buildPeriodComparison(
                orderCountMtd != null ? orderCountMtd.doubleValue() : 0.0,
                orderCountLtd != null ? orderCountLtd.doubleValue() : 0.0
        ));

        // Sales order count YTD vs last year same period
        Long orderCountLastYear = commandeVenteRepository.getSalesOrderCountInPeriod(entrepriseId, lastYearStart, lastYearSameDay);
        comparison.setOrderCountYtdVsLastYear(buildPeriodComparison(
                salesOrderCount != null ? salesOrderCount.doubleValue() : 0.0,
                orderCountLastYear != null ? orderCountLastYear.doubleValue() : 0.0
        ));

        return comparison;
    }

    private AiDashboardDto.PeriodComparisonDto buildPeriodComparison(double current, double previous) {
        AiDashboardDto.PeriodComparisonDto dto = new AiDashboardDto.PeriodComparisonDto();
        dto.setCurrent(Math.round(current * 100.0) / 100.0);
        dto.setPrevious(Math.round(previous * 100.0) / 100.0);
        double change = current - previous;
        dto.setChange(Math.round(change * 100.0) / 100.0);

        if (previous == 0.0) {
            // Starting from nothing: a %-change is mathematically undefined. Expose it as
            // null so the UI can render a dedicated "NOUV." badge instead of a misleading 0%.
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

    private List<AiDashboardDto.SupplierPerformanceDto> buildSupplierPerformanceMetrics(Long entrepriseId) {
        List<AiDashboardDto.SupplierPerformanceDto> result = new ArrayList<>();

        List<Object[]> supplierRows = commandeAchatRepository.getSupplierMetrics(entrepriseId);
        List<Object[]> statusRows = commandeAchatRepository.getSupplierStatusBreakdown(entrepriseId);

        // Build status breakdown map: supplierId -> list of StatusCountDto
        Map<Long, List<AiDashboardDto.StatusCountDto>> statusMap = new HashMap<>();
        // Also count cancelled/delivered orders per supplier for the rating features.
        Map<Long, Long> cancelledMap = new HashMap<>();
        Map<Long, Long> deliveredMap = new HashMap<>();
        Map<Long, Long> totalMap = new HashMap<>();
        for (Object[] row : statusRows) {
            Long supplierId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String status = row[1] != null ? row[1].toString() : "INCONNU";
            Long count = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            if (supplierId != null) {
                statusMap.computeIfAbsent(supplierId, k -> new ArrayList<>())
                         .add(new AiDashboardDto.StatusCountDto(status, count, null));
                totalMap.merge(supplierId, count, Long::sum);
                if ("ANNULEE".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                    cancelledMap.merge(supplierId, count, Long::sum);
                }
                if ("LIVREE".equalsIgnoreCase(status) || "DELIVERED".equalsIgnoreCase(status)) {
                    deliveredMap.merge(supplierId, count, Long::sum);
                }
            }
        }

        // First pass: collect up to 10 suppliers' raw data so we can peer-group
        // them with the ML rating engine before emitting the DTOs.
        List<Object[]> selectedRows = new ArrayList<>();
        List<SupplierRatingEngine.Input> inputs = new ArrayList<>();
        for (Object[] row : supplierRows) {
            if (selectedRows.size() >= 10) break;
            Long supplierId = row[0] != null ? ((Number) row[0]).longValue() : null;
            Long orderCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            Double totalSpend = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            double totalForSupplier = totalMap.getOrDefault(supplierId, orderCount);
            double delivered = deliveredMap.getOrDefault(supplierId, 0L);
            double cancelled = cancelledMap.getOrDefault(supplierId, 0L);
            // Compute delivery rate as percentage of delivered vs non-cancelled orders
            double nonCancelled = totalForSupplier - cancelled;
            double deliveryRate = nonCancelled > 0 ? (delivered / nonCancelled) * 100.0 : 0.0;
            double cancelledRatio = totalForSupplier > 0 ? cancelled / totalForSupplier : 0.0;
            selectedRows.add(row);
            inputs.add(new SupplierRatingEngine.Input(
                    orderCount, totalSpend,
                    deliveryRate / 100.0,
                    cancelledRatio));
        }
        List<SupplierRatingEngine.Output> ratings = supplierRatingEngine.rate(inputs);

        for (int i = 0; i < selectedRows.size(); i++) {
            Object[] row = selectedRows.get(i);
            Long supplierId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String nom = row[1] != null ? row[1].toString() : "";
            String prenom = row[2] != null ? row[2].toString().trim() : "";
            String supplierName = prenom.isEmpty() ? nom : nom + " " + prenom;
            Long orderCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            Double totalSpend = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            // Compute delivery rate dynamically from actual order statuses
            double totalForSupplier = totalMap.getOrDefault(supplierId, orderCount);
            double delivered = deliveredMap.getOrDefault(supplierId, 0L);
            double cancelled = cancelledMap.getOrDefault(supplierId, 0L);
            double nonCancelled = totalForSupplier - cancelled;
            double deliveryRate = nonCancelled > 0 ? (delivered / nonCancelled) * 100.0 : 0.0;
            // Replace the legacy persisted `notation` with the ML peer-group
            // rating so the dashboard reflects real-time relative performance.
            Double rating = ratings.get(i).ratingStars;

            AiDashboardDto.SupplierPerformanceDto dto = new AiDashboardDto.SupplierPerformanceDto();
            dto.setSupplierId(supplierId);
            dto.setSupplierName(supplierName.trim());
            dto.setOrderCount(orderCount);
            dto.setTotalSpend(Math.round(totalSpend * 100.0) / 100.0);
            dto.setDeliveryRate(Math.round(deliveryRate * 10.0) / 10.0);
            dto.setRating(rating);
            dto.setStatusBreakdown(statusMap.getOrDefault(supplierId, new ArrayList<>()));
            result.add(dto);
        }

        return result;
    }
}
