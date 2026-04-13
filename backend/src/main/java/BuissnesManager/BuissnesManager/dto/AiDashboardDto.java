package BuissnesManager.BuissnesManager.dto;

import java.util.List;

public class AiDashboardDto {

    private SalesAnalysisDto salesAnalysis;
    private ForecastDto forecast;
    private ProductIntelligenceDto productIntelligence;
    private List<RecommendationDto> recommendations;
    private KpiSummaryDto kpiSummary;
    private ComparisonMetricsDto comparison;
    private KpiDetailsDto kpiDetails;

    public AiDashboardDto() {}

    public SalesAnalysisDto getSalesAnalysis() { return salesAnalysis; }
    public void setSalesAnalysis(SalesAnalysisDto salesAnalysis) { this.salesAnalysis = salesAnalysis; }

    public ForecastDto getForecast() { return forecast; }
    public void setForecast(ForecastDto forecast) { this.forecast = forecast; }

    public ProductIntelligenceDto getProductIntelligence() { return productIntelligence; }
    public void setProductIntelligence(ProductIntelligenceDto productIntelligence) { this.productIntelligence = productIntelligence; }

    public List<RecommendationDto> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationDto> recommendations) { this.recommendations = recommendations; }

    public KpiSummaryDto getKpiSummary() { return kpiSummary; }
    public void setKpiSummary(KpiSummaryDto kpiSummary) { this.kpiSummary = kpiSummary; }

    public ComparisonMetricsDto getComparison() { return comparison; }
    public void setComparison(ComparisonMetricsDto comparison) { this.comparison = comparison; }

    public KpiDetailsDto getKpiDetails() { return kpiDetails; }
    public void setKpiDetails(KpiDetailsDto kpiDetails) { this.kpiDetails = kpiDetails; }

    public static class KpiSummaryDto {
        private Double totalRevenue;
        private Double revenueGrowth;
        private Long totalOrders;
        private Double orderGrowth;
        private Long totalProducts;
        private Long lowStockAlerts;
        private Double profitMargin;
        private Double avgOrderValue;

        public KpiSummaryDto() {}

        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
        public Double getRevenueGrowth() { return revenueGrowth; }
        public void setRevenueGrowth(Double revenueGrowth) { this.revenueGrowth = revenueGrowth; }
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Double getOrderGrowth() { return orderGrowth; }
        public void setOrderGrowth(Double orderGrowth) { this.orderGrowth = orderGrowth; }
        public Long getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Long totalProducts) { this.totalProducts = totalProducts; }
        public Long getLowStockAlerts() { return lowStockAlerts; }
        public void setLowStockAlerts(Long lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; }
        public Double getProfitMargin() { return profitMargin; }
        public void setProfitMargin(Double profitMargin) { this.profitMargin = profitMargin; }
        public Double getAvgOrderValue() { return avgOrderValue; }
        public void setAvgOrderValue(Double avgOrderValue) { this.avgOrderValue = avgOrderValue; }
    }

    // ===== Comparison Metrics =====
    public static class ComparisonMetricsDto {
        private PeriodComparisonDto mtdVsLtd;
        private PeriodComparisonDto ytdVsLastYear;

        public ComparisonMetricsDto() {}

        public PeriodComparisonDto getMtdVsLtd() { return mtdVsLtd; }
        public void setMtdVsLtd(PeriodComparisonDto mtdVsLtd) { this.mtdVsLtd = mtdVsLtd; }

        public PeriodComparisonDto getYtdVsLastYear() { return ytdVsLastYear; }
        public void setYtdVsLastYear(PeriodComparisonDto ytdVsLastYear) { this.ytdVsLastYear = ytdVsLastYear; }
    }

    public static class PeriodComparisonDto {
        private Double current;
        private Double previous;
        private Double change;
        private Double changePercent;
        private String trend; // "up", "down", "stable"

        public PeriodComparisonDto() {}

        public Double getCurrent() { return current; }
        public void setCurrent(Double current) { this.current = current; }
        public Double getPrevious() { return previous; }
        public void setPrevious(Double previous) { this.previous = previous; }
        public Double getChange() { return change; }
        public void setChange(Double change) { this.change = change; }
        public Double getChangePercent() { return changePercent; }
        public void setChangePercent(Double changePercent) { this.changePercent = changePercent; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }

    // ===== KPI Details =====
    public static class KpiDetailsDto {
        private RevenueDetailsDto revenue;
        private OrdersDetailsDto orders;
        private ProductsDetailsDto products;
        private ForecastDetailsDto forecast;

        public KpiDetailsDto() {}

        public RevenueDetailsDto getRevenue() { return revenue; }
        public void setRevenue(RevenueDetailsDto revenue) { this.revenue = revenue; }
        public OrdersDetailsDto getOrders() { return orders; }
        public void setOrders(OrdersDetailsDto orders) { this.orders = orders; }
        public ProductsDetailsDto getProducts() { return products; }
        public void setProducts(ProductsDetailsDto products) { this.products = products; }
        public ForecastDetailsDto getForecast() { return forecast; }
        public void setForecast(ForecastDetailsDto forecast) { this.forecast = forecast; }
    }

    public static class RevenueDetailsDto {
        private List<NameValueDto> topProducts;
        private List<NameValueDto> topCustomers;
        private List<StatusCountDto> paymentMethods;

        public RevenueDetailsDto() {}

        public List<NameValueDto> getTopProducts() { return topProducts; }
        public void setTopProducts(List<NameValueDto> topProducts) { this.topProducts = topProducts; }
        public List<NameValueDto> getTopCustomers() { return topCustomers; }
        public void setTopCustomers(List<NameValueDto> topCustomers) { this.topCustomers = topCustomers; }
        public List<StatusCountDto> getPaymentMethods() { return paymentMethods; }
        public void setPaymentMethods(List<StatusCountDto> paymentMethods) { this.paymentMethods = paymentMethods; }
    }

    public static class OrdersDetailsDto {
        private List<StatusCountDto> byStatus;
        private Long totalOrders;
        private Double avgOrderValue;
        private Double totalRevenue;

        public OrdersDetailsDto() {}

        public List<StatusCountDto> getByStatus() { return byStatus; }
        public void setByStatus(List<StatusCountDto> byStatus) { this.byStatus = byStatus; }
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Double getAvgOrderValue() { return avgOrderValue; }
        public void setAvgOrderValue(Double avgOrderValue) { this.avgOrderValue = avgOrderValue; }
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class ProductsDetailsDto {
        private Long totalSKUs;
        private Long lowStockCount;
        private Long outOfStockCount;
        private Double totalStockValue;
        private List<NameValueDto> topProducts;
        private List<NameValueDto> slowMovers;

        public ProductsDetailsDto() {}

        public Long getTotalSKUs() { return totalSKUs; }
        public void setTotalSKUs(Long totalSKUs) { this.totalSKUs = totalSKUs; }
        public Long getLowStockCount() { return lowStockCount; }
        public void setLowStockCount(Long lowStockCount) { this.lowStockCount = lowStockCount; }
        public Long getOutOfStockCount() { return outOfStockCount; }
        public void setOutOfStockCount(Long outOfStockCount) { this.outOfStockCount = outOfStockCount; }
        public Double getTotalStockValue() { return totalStockValue; }
        public void setTotalStockValue(Double totalStockValue) { this.totalStockValue = totalStockValue; }
        public List<NameValueDto> getTopProducts() { return topProducts; }
        public void setTopProducts(List<NameValueDto> topProducts) { this.topProducts = topProducts; }
        public List<NameValueDto> getSlowMovers() { return slowMovers; }
        public void setSlowMovers(List<NameValueDto> slowMovers) { this.slowMovers = slowMovers; }
    }

    public static class ForecastDetailsDto {
        private Double accuracy;
        private Double predictedNextMonth;
        private Double predictedNext3Months;
        private Double predictedNext6Months;
        private String methodology;

        public ForecastDetailsDto() {}

        public Double getAccuracy() { return accuracy; }
        public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
        public Double getPredictedNextMonth() { return predictedNextMonth; }
        public void setPredictedNextMonth(Double predictedNextMonth) { this.predictedNextMonth = predictedNextMonth; }
        public Double getPredictedNext3Months() { return predictedNext3Months; }
        public void setPredictedNext3Months(Double predictedNext3Months) { this.predictedNext3Months = predictedNext3Months; }
        public Double getPredictedNext6Months() { return predictedNext6Months; }
        public void setPredictedNext6Months(Double predictedNext6Months) { this.predictedNext6Months = predictedNext6Months; }
        public String getMethodology() { return methodology; }
        public void setMethodology(String methodology) { this.methodology = methodology; }
    }

    public static class NameValueDto {
        private String name;
        private Double value;

        public NameValueDto() {}
        public NameValueDto(String name, Double value) { this.name = name; this.value = value; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
    }

    public static class StatusCountDto {
        private String status;
        private Long count;
        private Double amount;

        public StatusCountDto() {}
        public StatusCountDto(String status, Long count, Double amount) {
            this.status = status; this.count = count; this.amount = amount;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }
}
