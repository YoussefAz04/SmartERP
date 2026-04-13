package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.MonthlySalesDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.dto.SalesAnalysisDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AnalyticService {

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private StockRepository stockRepository;

    private static final String[] MONTH_NAMES_FR = {
        "Jan", "Fév", "Mars", "Avr", "Mai", "Juin",
        "Juil", "Août", "Sept", "Oct", "Nov", "Déc"
    };

    public SalesAnalysisDto analyzeSales(Long entrepriseId) {
        int currentYear = LocalDate.now().getYear();
        List<Object[]> rawData = commandeVenteRepository.getMonthlySales(entrepriseId, currentYear);

        Map<Integer, Double> monthMap = new HashMap<>();
        for (Object[] row : rawData) {
            int month = ((Number) row[0]).intValue();
            double amount = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            monthMap.put(month, amount);
        }

        List<MonthlySalesDto> monthlySales = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            double amount = monthMap.getOrDefault(m, 0.0);
            monthlySales.add(new MonthlySalesDto(MONTH_NAMES_FR[m - 1], amount));
            values.add(amount);
        }

        double totalRevenue = values.stream().mapToDouble(Double::doubleValue).sum();
        double avgRevenue = totalRevenue / 12.0;

        // Find best month
        double bestRevenue = 0.0;
        String bestMonth = MONTH_NAMES_FR[0];
        for (int i = 0; i < 12; i++) {
            if (values.get(i) > bestRevenue) {
                bestRevenue = values.get(i);
                bestMonth = MONTH_NAMES_FR[i];
            }
        }

        // Growth rate: compare current half-year vs previous half-year
        double firstHalf = values.subList(0, 6).stream().mapToDouble(Double::doubleValue).sum();
        double secondHalf = values.subList(6, 12).stream().mapToDouble(Double::doubleValue).sum();
        double growthRate = firstHalf > 0 ? ((secondHalf - firstHalf) / firstHalf) * 100.0 : 0.0;

        // Trend line (3-month moving average)
        List<Double> trendLine = computeMovingAverage(values, 3);

        SalesAnalysisDto dto = new SalesAnalysisDto();
        dto.setMonthlySales(monthlySales);
        dto.setTotalRevenue(totalRevenue);
        dto.setAverageMonthlyRevenue(avgRevenue);
        dto.setGrowthRate(Math.round(growthRate * 10.0) / 10.0);
        dto.setBestMonth(bestMonth);
        dto.setBestMonthRevenue(bestRevenue);
        dto.setTrendLine(trendLine);

        return dto;
    }

    public ProductIntelligenceDto analyzeProducts(Long entrepriseId) {
        List<Object[]> topRaw = commandeVenteRepository.getTopProducts(entrepriseId);

        double totalRevenue = 0.0;
        List<ProductIntelligenceDto.ProductPerformanceDto> products = new ArrayList<>();
        for (Object[] row : topRaw) {
            String name = row[0] != null ? row[0].toString() : "Inconnu";
            Long qty = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Double rev = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            products.add(new ProductIntelligenceDto.ProductPerformanceDto(name, qty, rev));
            totalRevenue += rev;
        }

        // Calculate revenue share and rank
        for (int i = 0; i < products.size(); i++) {
            ProductIntelligenceDto.ProductPerformanceDto p = products.get(i);
            p.setRank(i + 1);
            p.setRevenueShare(totalRevenue > 0 ? Math.round((p.getRevenue() / totalRevenue) * 1000.0) / 10.0 : 0.0);
            if (i < 3) {
                p.setPerformanceLabel("TOP");
            } else if (i < 10) {
                p.setPerformanceLabel("BON");
            } else {
                p.setPerformanceLabel("NORMAL");
            }
        }

        List<ProductIntelligenceDto.ProductPerformanceDto> topProducts = products.subList(0, Math.min(10, products.size()));

        // Slow moving: products with lowest sales (bottom 5 from total, if enough data)
        List<ProductIntelligenceDto.ProductPerformanceDto> slowMoving = new ArrayList<>();
        if (products.size() > 5) {
            List<ProductIntelligenceDto.ProductPerformanceDto> reversed = new ArrayList<>(products);
            java.util.Collections.reverse(reversed);
            slowMoving = reversed.subList(0, Math.min(5, reversed.size()));
            for (ProductIntelligenceDto.ProductPerformanceDto p : slowMoving) {
                p.setPerformanceLabel("LENT");
            }
        }

        // Category analysis: group by category (using product name prefix as proxy if no category data)
        List<ProductIntelligenceDto.CategoryPerformanceDto> categoryPerformance = computeCategoryPerformance(entrepriseId);

        long totalSKUs = stockRepository.getTotalSKUs(entrepriseId);

        ProductIntelligenceDto dto = new ProductIntelligenceDto();
        dto.setTopProducts(topProducts);
        dto.setSlowMovingProducts(slowMoving);
        dto.setCategoryPerformance(categoryPerformance);
        dto.setTotalProducts((int) totalSKUs);
        dto.setActiveProducts((int) totalSKUs);

        return dto;
    }

    private List<ProductIntelligenceDto.CategoryPerformanceDto> computeCategoryPerformance(Long entrepriseId) {
        // Query category performance from ligne_commande_vente via article category
        List<Object[]> rawData = commandeVenteRepository.getTopProducts(entrepriseId);
        // Group by first word as category proxy (since no direct category query yet)
        Map<String, Double[]> catMap = new HashMap<>();
        for (Object[] row : rawData) {
            String name = row[0] != null ? row[0].toString() : "Autre";
            Long qty = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Double rev = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            String cat = name.length() > 0 ? name.substring(0, 1).toUpperCase() + "..." : "Autre";
            catMap.merge(cat, new Double[]{rev, qty.doubleValue()}, (a, b) -> new Double[]{a[0] + b[0], a[1] + b[1]});
        }

        double totalRev = catMap.values().stream().mapToDouble(v -> v[0]).sum();
        List<ProductIntelligenceDto.CategoryPerformanceDto> result = new ArrayList<>();
        for (Map.Entry<String, Double[]> entry : catMap.entrySet()) {
            ProductIntelligenceDto.CategoryPerformanceDto cat = new ProductIntelligenceDto.CategoryPerformanceDto(
                entry.getKey(), entry.getValue()[0], entry.getValue()[1].longValue()
            );
            cat.setRevenueShare(totalRev > 0 ? Math.round((entry.getValue()[0] / totalRev) * 1000.0) / 10.0 : 0.0);
            result.add(cat);
        }
        result.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));
        return result.subList(0, Math.min(5, result.size()));
    }

    public List<Double> computeMovingAverage(List<Double> data, int window) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            int start = Math.max(0, i - window + 1);
            double sum = 0.0;
            for (int j = start; j <= i; j++) {
                sum += data.get(j);
            }
            result.add(Math.round((sum / (i - start + 1)) * 100.0) / 100.0);
        }
        return result;
    }
}
