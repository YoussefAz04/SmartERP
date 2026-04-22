package BuissnesManager.BuissnesManager.dto;

import java.util.ArrayList;
import java.util.List;

public class CustomerAnalyticsDto {

    private List<CustomerSegmentationDto> customerSegments = new ArrayList<>();
    private List<TopCustomerDto> topCustomers = new ArrayList<>();
    private List<CustomerLifetimeValueDto> lifetimeValueTop = new ArrayList<>();
    private CustomerChurnRiskDto churnRisk;
    private CustomerMetricsDto metrics;
    private List<CustomerTrendDto> monthlyTrend = new ArrayList<>();
    private List<PaymentBehaviorDto> paymentBehavior = new ArrayList<>();

    public static class CustomerSegmentationDto {
        private String segment;
        private int customerCount;
        private double revenue;
        private double revenueShare;
        private String riskLevel;

        public String getSegment() { return segment; }
        public void setSegment(String segment) { this.segment = segment; }
        public int getCustomerCount() { return customerCount; }
        public void setCustomerCount(int customerCount) { this.customerCount = customerCount; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
        public double getRevenueShare() { return revenueShare; }
        public void setRevenueShare(double revenueShare) { this.revenueShare = revenueShare; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }

    public static class TopCustomerDto {
        private Long customerId;
        private String customerName;
        private double totalRevenue;
        private int orderCount;
        private double avgOrderValue;
        private String lastOrderDate;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        public int getOrderCount() { return orderCount; }
        public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
        public double getAvgOrderValue() { return avgOrderValue; }
        public void setAvgOrderValue(double avgOrderValue) { this.avgOrderValue = avgOrderValue; }
        public String getLastOrderDate() { return lastOrderDate; }
        public void setLastOrderDate(String lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    }

    public static class CustomerLifetimeValueDto {
        private Long customerId;
        private String customerName;
        private double lifetimeValue;
        private double predictedValue;
        private int monthsActive;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public double getLifetimeValue() { return lifetimeValue; }
        public void setLifetimeValue(double lifetimeValue) { this.lifetimeValue = lifetimeValue; }
        public double getPredictedValue() { return predictedValue; }
        public void setPredictedValue(double predictedValue) { this.predictedValue = predictedValue; }
        public int getMonthsActive() { return monthsActive; }
        public void setMonthsActive(int monthsActive) { this.monthsActive = monthsActive; }
    }

    public static class CustomerChurnRiskDto {
        private int highRiskCount;
        private int mediumRiskCount;
        private int lowRiskCount;
        private double churnProbability;
        private List<AtRiskCustomerDto> atRiskCustomers = new ArrayList<>();

        public int getHighRiskCount() { return highRiskCount; }
        public void setHighRiskCount(int highRiskCount) { this.highRiskCount = highRiskCount; }
        public int getMediumRiskCount() { return mediumRiskCount; }
        public void setMediumRiskCount(int mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }
        public int getLowRiskCount() { return lowRiskCount; }
        public void setLowRiskCount(int lowRiskCount) { this.lowRiskCount = lowRiskCount; }
        public double getChurnProbability() { return churnProbability; }
        public void setChurnProbability(double churnProbability) { this.churnProbability = churnProbability; }
        public List<AtRiskCustomerDto> getAtRiskCustomers() { return atRiskCustomers; }
        public void setAtRiskCustomers(List<AtRiskCustomerDto> atRiskCustomers) { this.atRiskCustomers = atRiskCustomers; }
    }

    public static class AtRiskCustomerDto {
        private Long customerId;
        private String customerName;
        private String riskLevel;
        private double riskScore;
        private String reason;
        private int daysSinceLastOrder;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public double getRiskScore() { return riskScore; }
        public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public int getDaysSinceLastOrder() { return daysSinceLastOrder; }
        public void setDaysSinceLastOrder(int daysSinceLastOrder) { this.daysSinceLastOrder = daysSinceLastOrder; }
    }

    public static class CustomerMetricsDto {
        private int totalCustomers;
        private int activeCustomers;
        private double avgCustomerValue;
        private double retentionRate;
        private double growthRate;

        public int getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }
        public int getActiveCustomers() { return activeCustomers; }
        public void setActiveCustomers(int activeCustomers) { this.activeCustomers = activeCustomers; }
        public double getAvgCustomerValue() { return avgCustomerValue; }
        public void setAvgCustomerValue(double avgCustomerValue) { this.avgCustomerValue = avgCustomerValue; }
        public double getRetentionRate() { return retentionRate; }
        public void setRetentionRate(double retentionRate) { this.retentionRate = retentionRate; }
        public double getGrowthRate() { return growthRate; }
        public void setGrowthRate(double growthRate) { this.growthRate = growthRate; }
    }

    public static class CustomerTrendDto {
        private String month;
        private int newCustomers;
        private int returningCustomers;
        private double revenue;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public int getNewCustomers() { return newCustomers; }
        public void setNewCustomers(int newCustomers) { this.newCustomers = newCustomers; }
        public int getReturningCustomers() { return returningCustomers; }
        public void setReturningCustomers(int returningCustomers) { this.returningCustomers = returningCustomers; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
    }

    public static class PaymentBehaviorDto {
        private String customerName;
        private int avgPaymentDays;
        private String paymentRating;
        private double totalPaid;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public int getAvgPaymentDays() { return avgPaymentDays; }
        public void setAvgPaymentDays(int avgPaymentDays) { this.avgPaymentDays = avgPaymentDays; }
        public String getPaymentRating() { return paymentRating; }
        public void setPaymentRating(String paymentRating) { this.paymentRating = paymentRating; }
        public double getTotalPaid() { return totalPaid; }
        public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }
    }

    public List<CustomerSegmentationDto> getCustomerSegments() { return customerSegments; }
    public void setCustomerSegments(List<CustomerSegmentationDto> customerSegments) { this.customerSegments = customerSegments; }
    public List<TopCustomerDto> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<TopCustomerDto> topCustomers) { this.topCustomers = topCustomers; }
    public List<CustomerLifetimeValueDto> getLifetimeValueTop() { return lifetimeValueTop; }
    public void setLifetimeValueTop(List<CustomerLifetimeValueDto> lifetimeValueTop) { this.lifetimeValueTop = lifetimeValueTop; }
    public CustomerChurnRiskDto getChurnRisk() { return churnRisk; }
    public void setChurnRisk(CustomerChurnRiskDto churnRisk) { this.churnRisk = churnRisk; }
    public CustomerMetricsDto getMetrics() { return metrics; }
    public void setMetrics(CustomerMetricsDto metrics) { this.metrics = metrics; }
    public List<CustomerTrendDto> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<CustomerTrendDto> monthlyTrend) { this.monthlyTrend = monthlyTrend; }
    public List<PaymentBehaviorDto> getPaymentBehavior() { return paymentBehavior; }
    public void setPaymentBehavior(List<PaymentBehaviorDto> paymentBehavior) { this.paymentBehavior = paymentBehavior; }
}
