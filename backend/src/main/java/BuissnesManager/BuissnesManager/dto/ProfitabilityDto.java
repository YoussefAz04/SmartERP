package BuissnesManager.BuissnesManager.dto;

import java.util.ArrayList;
import java.util.List;

public class ProfitabilityDto {

    private ProfitabilitySummaryDto summary;
    private List<ProductProfitDto> topProducts = new ArrayList<>();
    private List<CategoryProfitDto> topCategories = new ArrayList<>();
    private List<CustomerProfitDto> topCustomers = new ArrayList<>();
    private List<MarginTrendDto> marginTrend = new ArrayList<>();
    private List<SupplierProfitDto> supplierAnalysis = new ArrayList<>();

    public static class ProfitabilitySummaryDto {
        private double totalRevenue;
        private double totalCost;
        private double grossProfit;
        private double grossMargin;
        private double netMargin;
        private double contributionMargin;
        private double breakEvenPoint;
        private double profitPerOrder;
        private double roi;

        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
        public double getGrossProfit() { return grossProfit; }
        public void setGrossProfit(double grossProfit) { this.grossProfit = grossProfit; }
        public double getGrossMargin() { return grossMargin; }
        public void setGrossMargin(double grossMargin) { this.grossMargin = grossMargin; }
        public double getNetMargin() { return netMargin; }
        public void setNetMargin(double netMargin) { this.netMargin = netMargin; }
        public double getContributionMargin() { return contributionMargin; }
        public void setContributionMargin(double contributionMargin) { this.contributionMargin = contributionMargin; }
        public double getBreakEvenPoint() { return breakEvenPoint; }
        public void setBreakEvenPoint(double breakEvenPoint) { this.breakEvenPoint = breakEvenPoint; }
        public double getProfitPerOrder() { return profitPerOrder; }
        public void setProfitPerOrder(double profitPerOrder) { this.profitPerOrder = profitPerOrder; }
        public double getRoi() { return roi; }
        public void setRoi(double roi) { this.roi = roi; }
    }

    public static class ProductProfitDto {
        private Long productId;
        private String productName;
        private double revenue;
        private double cost;
        private double profit;
        private double marginPercent;
        private int unitsSold;
        private double profitPerUnit;
        private String marginClass;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }
        public double getProfit() { return profit; }
        public void setProfit(double profit) { this.profit = profit; }
        public double getMarginPercent() { return marginPercent; }
        public void setMarginPercent(double marginPercent) { this.marginPercent = marginPercent; }
        public int getUnitsSold() { return unitsSold; }
        public void setUnitsSold(int unitsSold) { this.unitsSold = unitsSold; }
        public double getProfitPerUnit() { return profitPerUnit; }
        public void setProfitPerUnit(double profitPerUnit) { this.profitPerUnit = profitPerUnit; }
        public String getMarginClass() { return marginClass; }
        public void setMarginClass(String marginClass) { this.marginClass = marginClass; }
    }

    public static class CategoryProfitDto {
        private String categoryName;
        private double revenue;
        private double cost;
        private double profit;
        private double marginPercent;
        private int productCount;
        private String trend;

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }
        public double getProfit() { return profit; }
        public void setProfit(double profit) { this.profit = profit; }
        public double getMarginPercent() { return marginPercent; }
        public void setMarginPercent(double marginPercent) { this.marginPercent = marginPercent; }
        public int getProductCount() { return productCount; }
        public void setProductCount(int productCount) { this.productCount = productCount; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }

    public static class CustomerProfitDto {
        private Long customerId;
        private String customerName;
        private double revenue;
        private double cost;
        private double profit;
        private double marginPercent;
        private int orderCount;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }
        public double getProfit() { return profit; }
        public void setProfit(double profit) { this.profit = profit; }
        public double getMarginPercent() { return marginPercent; }
        public void setMarginPercent(double marginPercent) { this.marginPercent = marginPercent; }
        public int getOrderCount() { return orderCount; }
        public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    }

    public static class MarginTrendDto {
        private String month;
        private double grossMargin;
        private double netMargin;
        private double revenue;
        private double profit;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public double getGrossMargin() { return grossMargin; }
        public void setGrossMargin(double grossMargin) { this.grossMargin = grossMargin; }
        public double getNetMargin() { return netMargin; }
        public void setNetMargin(double netMargin) { this.netMargin = netMargin; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
        public double getProfit() { return profit; }
        public void setProfit(double profit) { this.profit = profit; }
    }

    public static class SupplierProfitDto {
        private Long supplierId;
        private String supplierName;
        private double totalPurchased;
        private double avgCost;
        private double marginImpact;
        private String rating;

        public Long getSupplierId() { return supplierId; }
        public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public double getTotalPurchased() { return totalPurchased; }
        public void setTotalPurchased(double totalPurchased) { this.totalPurchased = totalPurchased; }
        public double getAvgCost() { return avgCost; }
        public void setAvgCost(double avgCost) { this.avgCost = avgCost; }
        public double getMarginImpact() { return marginImpact; }
        public void setMarginImpact(double marginImpact) { this.marginImpact = marginImpact; }
        public String getRating() { return rating; }
        public void setRating(String rating) { this.rating = rating; }
    }

    public ProfitabilitySummaryDto getSummary() { return summary; }
    public void setSummary(ProfitabilitySummaryDto summary) { this.summary = summary; }
    public List<ProductProfitDto> getTopProducts() { return topProducts; }
    public void setTopProducts(List<ProductProfitDto> topProducts) { this.topProducts = topProducts; }
    public List<CategoryProfitDto> getTopCategories() { return topCategories; }
    public void setTopCategories(List<CategoryProfitDto> topCategories) { this.topCategories = topCategories; }
    public List<CustomerProfitDto> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<CustomerProfitDto> topCustomers) { this.topCustomers = topCustomers; }
    public List<MarginTrendDto> getMarginTrend() { return marginTrend; }
    public void setMarginTrend(List<MarginTrendDto> marginTrend) { this.marginTrend = marginTrend; }
    public List<SupplierProfitDto> getSupplierAnalysis() { return supplierAnalysis; }
    public void setSupplierAnalysis(List<SupplierProfitDto> supplierAnalysis) { this.supplierAnalysis = supplierAnalysis; }
}
