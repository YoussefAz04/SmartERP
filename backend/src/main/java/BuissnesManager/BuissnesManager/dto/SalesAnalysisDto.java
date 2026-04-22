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
    /** 95% lower bound of the bootstrapped annual growth-rate estimate (percent). */
    private Double growthRateLower;
    /** 95% upper bound of the bootstrapped annual growth-rate estimate (percent). */
    private Double growthRateUpper;
    /** Boolean flag per month indicating a statistical outlier (null when not computed). */
    private List<Boolean> anomalies;
    /** Short methodology label for the sales-analysis block (LOESS / log-OLS / …). */
    private String methodology;
    /** Diagnostics bundle for the growth-rate model. */
    private ModelMetricsDto modelMetrics;

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

    public Double getGrowthRateLower() { return growthRateLower; }
    public void setGrowthRateLower(Double growthRateLower) { this.growthRateLower = growthRateLower; }
    public Double getGrowthRateUpper() { return growthRateUpper; }
    public void setGrowthRateUpper(Double growthRateUpper) { this.growthRateUpper = growthRateUpper; }
    public List<Boolean> getAnomalies() { return anomalies; }
    public void setAnomalies(List<Boolean> anomalies) { this.anomalies = anomalies; }
    public String getMethodology() { return methodology; }
    public void setMethodology(String methodology) { this.methodology = methodology; }
    public ModelMetricsDto getModelMetrics() { return modelMetrics; }
    public void setModelMetrics(ModelMetricsDto modelMetrics) { this.modelMetrics = modelMetrics; }
}
