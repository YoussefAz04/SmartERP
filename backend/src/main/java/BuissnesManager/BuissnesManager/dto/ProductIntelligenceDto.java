package BuissnesManager.BuissnesManager.dto;

import java.util.List;

public class ProductIntelligenceDto {

    private List<ProductPerformanceDto> topProducts;
    private List<CategoryPerformanceDto> categoryPerformance;
    private Integer totalProducts;
    private Integer activeProducts;

    public ProductIntelligenceDto() {}

    public List<ProductPerformanceDto> getTopProducts() { return topProducts; }
    public void setTopProducts(List<ProductPerformanceDto> topProducts) { this.topProducts = topProducts; }

    public List<CategoryPerformanceDto> getCategoryPerformance() { return categoryPerformance; }
    public void setCategoryPerformance(List<CategoryPerformanceDto> categoryPerformance) { this.categoryPerformance = categoryPerformance; }

    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }

    public Integer getActiveProducts() { return activeProducts; }
    public void setActiveProducts(Integer activeProducts) { this.activeProducts = activeProducts; }

    public static class ProductPerformanceDto {
        private String productName;
        private Long quantitySold;
        private Double revenue;
        private Double revenueShare;
        private Integer rank;
        private String performanceLabel;

        public ProductPerformanceDto() {}

        public ProductPerformanceDto(String productName, Long quantitySold, Double revenue) {
            this.productName = productName;
            this.quantitySold = quantitySold;
            this.revenue = revenue;
        }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Long getQuantitySold() { return quantitySold; }
        public void setQuantitySold(Long quantitySold) { this.quantitySold = quantitySold; }
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        public Double getRevenueShare() { return revenueShare; }
        public void setRevenueShare(Double revenueShare) { this.revenueShare = revenueShare; }
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        public String getPerformanceLabel() { return performanceLabel; }
        public void setPerformanceLabel(String performanceLabel) { this.performanceLabel = performanceLabel; }
    }

    public static class CategoryPerformanceDto {
        private String categoryName;
        private Double revenue;
        private Long quantity;
        private Double revenueShare;

        public CategoryPerformanceDto() {}

        public CategoryPerformanceDto(String categoryName, Double revenue, Long quantity) {
            this.categoryName = categoryName;
            this.revenue = revenue;
            this.quantity = quantity;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
        public Double getRevenueShare() { return revenueShare; }
        public void setRevenueShare(Double revenueShare) { this.revenueShare = revenueShare; }
    }
}
