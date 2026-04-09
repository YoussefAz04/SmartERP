package BuissnesManager.BuissnesManager.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

    // Sales
    private Double totalSalesRevenue;
    private Double salesThisMonth;
    private Double salesThisYear;
    private Long totalOrders;
    private Double averageOrderValue;

    // Purchases
    private Double totalPurchases;
    private Double purchasesThisMonth;
    private Long totalPurchaseOrders;

    // Inventory
    private Double totalStockValue;
    private Long lowStockItemsCount;
    private Long outOfStockItemsCount;
    private Long totalSKUs;

    // Financial
    private Double totalRevenue;
    private Double totalExpenses;
    private Double profitLoss;
    private Double accountsReceivable;
    private Double accountsPayable;

    // Customers
    private Long totalCustomers;
    private Long newCustomersThisMonth;
    private List<TopCustomerDto> topCustomers;

    // Orders
    private Map<String, Long> orderStatusBreakdown;
    private Map<String, Long> purchaseOrderStatusBreakdown;

    // Charts Data
    private List<MonthlySalesDto> monthlySalesChart;
    private List<MonthlySalesDto> monthlyPurchasesChart;
    private List<TopProductDto> topProducts;

    public DashboardStatsDto() {}

    // Getters and Setters

    public Double getTotalSalesRevenue() { return totalSalesRevenue; }
    public void setTotalSalesRevenue(Double totalSalesRevenue) { this.totalSalesRevenue = totalSalesRevenue; }

    public Double getSalesThisMonth() { return salesThisMonth; }
    public void setSalesThisMonth(Double salesThisMonth) { this.salesThisMonth = salesThisMonth; }

    public Double getSalesThisYear() { return salesThisYear; }
    public void setSalesThisYear(Double salesThisYear) { this.salesThisYear = salesThisYear; }

    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public Double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public Double getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(Double totalPurchases) { this.totalPurchases = totalPurchases; }

    public Double getPurchasesThisMonth() { return purchasesThisMonth; }
    public void setPurchasesThisMonth(Double purchasesThisMonth) { this.purchasesThisMonth = purchasesThisMonth; }

    public Long getTotalPurchaseOrders() { return totalPurchaseOrders; }
    public void setTotalPurchaseOrders(Long totalPurchaseOrders) { this.totalPurchaseOrders = totalPurchaseOrders; }

    public Double getTotalStockValue() { return totalStockValue; }
    public void setTotalStockValue(Double totalStockValue) { this.totalStockValue = totalStockValue; }

    public Long getLowStockItemsCount() { return lowStockItemsCount; }
    public void setLowStockItemsCount(Long lowStockItemsCount) { this.lowStockItemsCount = lowStockItemsCount; }

    public Long getOutOfStockItemsCount() { return outOfStockItemsCount; }
    public void setOutOfStockItemsCount(Long outOfStockItemsCount) { this.outOfStockItemsCount = outOfStockItemsCount; }

    public Long getTotalSKUs() { return totalSKUs; }
    public void setTotalSKUs(Long totalSKUs) { this.totalSKUs = totalSKUs; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(Double totalExpenses) { this.totalExpenses = totalExpenses; }

    public Double getProfitLoss() { return profitLoss; }
    public void setProfitLoss(Double profitLoss) { this.profitLoss = profitLoss; }

    public Double getAccountsReceivable() { return accountsReceivable; }
    public void setAccountsReceivable(Double accountsReceivable) { this.accountsReceivable = accountsReceivable; }

    public Double getAccountsPayable() { return accountsPayable; }
    public void setAccountsPayable(Double accountsPayable) { this.accountsPayable = accountsPayable; }

    public Long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }

    public Long getNewCustomersThisMonth() { return newCustomersThisMonth; }
    public void setNewCustomersThisMonth(Long newCustomersThisMonth) { this.newCustomersThisMonth = newCustomersThisMonth; }

    public List<TopCustomerDto> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<TopCustomerDto> topCustomers) { this.topCustomers = topCustomers; }

    public Map<String, Long> getOrderStatusBreakdown() { return orderStatusBreakdown; }
    public void setOrderStatusBreakdown(Map<String, Long> orderStatusBreakdown) { this.orderStatusBreakdown = orderStatusBreakdown; }

    public Map<String, Long> getPurchaseOrderStatusBreakdown() { return purchaseOrderStatusBreakdown; }
    public void setPurchaseOrderStatusBreakdown(Map<String, Long> purchaseOrderStatusBreakdown) { this.purchaseOrderStatusBreakdown = purchaseOrderStatusBreakdown; }

    public List<MonthlySalesDto> getMonthlySalesChart() { return monthlySalesChart; }
    public void setMonthlySalesChart(List<MonthlySalesDto> monthlySalesChart) { this.monthlySalesChart = monthlySalesChart; }

    public List<MonthlySalesDto> getMonthlyPurchasesChart() { return monthlyPurchasesChart; }
    public void setMonthlyPurchasesChart(List<MonthlySalesDto> monthlyPurchasesChart) { this.monthlyPurchasesChart = monthlyPurchasesChart; }

    public List<TopProductDto> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProductDto> topProducts) { this.topProducts = topProducts; }
}
