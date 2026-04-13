package BuissnesManager.BuissnesManager.dto;

import java.util.List;

public class AiDashboardDto {

    private SalesAnalysisDto salesAnalysis;
    private ForecastDto forecast;
    private ProductIntelligenceDto productIntelligence;
    private List<RecommendationDto> recommendations;
    private KpiSummaryDto kpiSummary;

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
}
