package BuissnesManager.BuissnesManager.dto;

import java.util.List;

public class SalesAnalysisDto {

    private List<MonthlySalesDto> monthlySales;
    private Double totalRevenue;
    private Double averageMonthlyRevenue;
    private Double growthRate;
    private String bestMonth;
    private Double bestMonthRevenue;
    private List<Double> trendLine;

    public SalesAnalysisDto() {}

    public List<MonthlySalesDto> getMonthlySales() { return monthlySales; }
    public void setMonthlySales(List<MonthlySalesDto> monthlySales) { this.monthlySales = monthlySales; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getAverageMonthlyRevenue() { return averageMonthlyRevenue; }
    public void setAverageMonthlyRevenue(Double averageMonthlyRevenue) { this.averageMonthlyRevenue = averageMonthlyRevenue; }

    public Double getGrowthRate() { return growthRate; }
    public void setGrowthRate(Double growthRate) { this.growthRate = growthRate; }

    public String getBestMonth() { return bestMonth; }
    public void setBestMonth(String bestMonth) { this.bestMonth = bestMonth; }

    public Double getBestMonthRevenue() { return bestMonthRevenue; }
    public void setBestMonthRevenue(Double bestMonthRevenue) { this.bestMonthRevenue = bestMonthRevenue; }

    public List<Double> getTrendLine() { return trendLine; }
    public void setTrendLine(List<Double> trendLine) { this.trendLine = trendLine; }
}
