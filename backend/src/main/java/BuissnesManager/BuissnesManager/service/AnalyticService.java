package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.MonthlySalesDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.dto.SalesAnalysisDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smile.clustering.KMeans;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AnalyticService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticService.class);

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private StockRepository stockRepository;

    private static final String[] MONTH_NAMES_FR = {
        "Jan", "Fév", "Mars", "Avr", "Mai", "Juin",
        "Juil", "Août", "Sept", "Oct", "Nov", "Déc"
    };

    /**
     * Minimum catalogue size before we attempt K-Means clustering. Below this
     * threshold the three-cluster split would be arbitrary (3 clusters on
     * fewer than ~15 points typically just reproduces a trivial sort), so we
     * fall back to simple rank-based rules instead.
     */
    private static final int MIN_KMEANS_PRODUCTS = 15;

    /** Number of clusters used for product performance tiers: TOP, BON, LENT. */
    private static final int KMEANS_K = 3;

    /** Fixed seed so clustering is deterministic between requests. */
    private static final long KMEANS_SEED = 42L;

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
        // Average monthly revenue over months that have at least some activity, so
        // businesses that only started selling in, say, September don't get their
        // average diluted by eight empty months.
        long activeMonths = values.stream().filter(v -> v > 0).count();
        double avgRevenue = activeMonths > 0 ? totalRevenue / activeMonths : 0.0;

        // Find best month
        double bestRevenue = 0.0;
        String bestMonth = MONTH_NAMES_FR[0];
        for (int i = 0; i < 12; i++) {
            if (values.get(i) > bestRevenue) {
                bestRevenue = values.get(i);
                bestMonth = MONTH_NAMES_FR[i];
            }
        }

        // Growth rate: compare the most recent half-year of activity against the
        // preceding half-year. Only compute when the first half has meaningful
        // revenue (prior code returned 0% when firstHalf==0, which wrongly read
        // as "no growth" for brand-new businesses — still OK, but we also cap
        // absurd ratios so a single tiny order doesn't claim +999 900% growth).
        double firstHalf = values.subList(0, 6).stream().mapToDouble(Double::doubleValue).sum();
        double secondHalf = values.subList(6, 12).stream().mapToDouble(Double::doubleValue).sum();
        double growthRate = 0.0;
        if (firstHalf > 0) {
            growthRate = ((secondHalf - firstHalf) / firstHalf) * 100.0;
            if (growthRate >  9999.0) growthRate =  9999.0;
            if (growthRate < -9999.0) growthRate = -9999.0;
        }

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
        }

        // === K-Means clustering (Smile) for product performance tiers ===
        // With a reasonably-sized catalogue (>= MIN_KMEANS_PRODUCTS items) we
        // cluster products on (revenue, quantity) using K-Means(k=3) and label
        // clusters TOP / BON / LENT in decreasing order of mean revenue. This
        // replaces arbitrary top-N / bottom-N thresholds with data-driven tiers.
        // For smaller catalogues the simple rank-based rule (below) is kept
        // because K-Means is underdetermined with so few points.
        int[] clusterLabels = null;
        if (products.size() >= MIN_KMEANS_PRODUCTS) {
            clusterLabels = clusterProductsByPerformance(products);
        }

        if (clusterLabels != null) {
            String[] tierNames = {"TOP", "BON", "LENT"};
            for (int i = 0; i < products.size(); i++) {
                int tier = clusterLabels[i];
                if (tier >= 0 && tier < tierNames.length) {
                    products.get(i).setPerformanceLabel(tierNames[tier]);
                }
            }
        } else {
            for (int i = 0; i < products.size(); i++) {
                ProductIntelligenceDto.ProductPerformanceDto p = products.get(i);
                if (i < 3) {
                    p.setPerformanceLabel("TOP");
                } else if (i < 10) {
                    p.setPerformanceLabel("BON");
                } else {
                    p.setPerformanceLabel("NORMAL");
                }
            }
        }

        // Defensive copies: subList returns a *view* of the backing list, so
        // later mutation of `products` (e.g. tagging slow-movers with the "LENT"
        // performance label below) would leak back into the top-products slice
        // and corrupt the ranking sent to the frontend.
        List<ProductIntelligenceDto.ProductPerformanceDto> topProducts =
            new ArrayList<>(products.subList(0, Math.min(10, products.size())));

        // Slow moving: products with lowest sales (bottom 5 from total, if enough data)
        List<ProductIntelligenceDto.ProductPerformanceDto> slowMoving = new ArrayList<>();
        if (clusterLabels != null) {
            // When clustering was used, "slow movers" are the products the ML
            // model assigned to the lowest-revenue cluster (label index 2).
            // Sort ascending by revenue and keep up to 5 for display.
            for (int i = 0; i < products.size(); i++) {
                if (clusterLabels[i] == 2) {
                    slowMoving.add(products.get(i));
                }
            }
            slowMoving.sort(Comparator.comparingDouble(
                ProductIntelligenceDto.ProductPerformanceDto::getRevenue));
            if (slowMoving.size() > 5) {
                slowMoving = new ArrayList<>(slowMoving.subList(0, 5));
            }
            // Defensive copy not strictly needed — slowMoving contains the same
            // objects as `products` — but we ensure the label is consistent.
            for (ProductIntelligenceDto.ProductPerformanceDto p : slowMoving) {
                p.setPerformanceLabel("LENT");
            }
        } else if (products.size() > 5) {
            List<ProductIntelligenceDto.ProductPerformanceDto> reversed = new ArrayList<>(products);
            java.util.Collections.reverse(reversed);
            slowMoving = new ArrayList<>(reversed.subList(0, Math.min(5, reversed.size())));
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
        // Use a dedicated query that joins through articleVente -> articleAchat -> categorieArticle
        // so we report real category revenue instead of grouping on the first letter of the product name.
        List<Object[]> rawData = commandeVenteRepository.getCategoryPerformance(entrepriseId);

        double totalRev = 0.0;
        List<ProductIntelligenceDto.CategoryPerformanceDto> result = new ArrayList<>();
        for (Object[] row : rawData) {
            String name = row[0] != null ? row[0].toString() : "Non catégorisé";
            Double rev = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            Long qty = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            result.add(new ProductIntelligenceDto.CategoryPerformanceDto(name, rev, qty));
            totalRev += rev;
        }

        for (ProductIntelligenceDto.CategoryPerformanceDto cat : result) {
            cat.setRevenueShare(totalRev > 0 ? Math.round((cat.getRevenue() / totalRev) * 1000.0) / 10.0 : 0.0);
        }
        // Defensive copy again to avoid returning a subList view.
        return new ArrayList<>(result.subList(0, Math.min(5, result.size())));
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

    /**
     * Clusters products into {@link #KMEANS_K} performance tiers using Smile's
     * {@link KMeans} on a (revenue, quantity) feature vector. Features are
     * standardised (z-score) so a product with a large revenue doesn't
     * completely dominate the Euclidean distance used by K-Means.
     *
     * <p>The clusters returned by the algorithm carry arbitrary numeric labels
     * (0, 1, 2). We remap them so that label <b>0 = highest mean revenue (TOP),
     * 1 = middle (BON), 2 = lowest (LENT)</b>. The returned array is aligned
     * with the input list so caller can look up the tier of product i in O(1).
     *
     * <p>Returns {@code null} when clustering fails for any numerical reason so
     * the caller can gracefully fall back to rule-based labelling.
     */
    int[] clusterProductsByPerformance(List<ProductIntelligenceDto.ProductPerformanceDto> products) {
        int n = products.size();
        if (n < KMEANS_K) return null;

        double[] revenues = new double[n];
        double[] quantities = new double[n];
        for (int i = 0; i < n; i++) {
            revenues[i] = products.get(i).getRevenue();
            quantities[i] = products.get(i).getQuantitySold();
        }

        double[] revStats = meanStd(revenues);
        double[] qtyStats = meanStd(quantities);
        double revStd = revStats[1] > 0 ? revStats[1] : 1.0;
        double qtyStd = qtyStats[1] > 0 ? qtyStats[1] : 1.0;

        double[][] features = new double[n][2];
        for (int i = 0; i < n; i++) {
            features[i][0] = (revenues[i] - revStats[0]) / revStd;
            features[i][1] = (quantities[i] - qtyStats[0]) / qtyStd;
        }

        KMeans km;
        try {
            // Seed Smile's shared RNG to keep the output deterministic across
            // requests; otherwise two identical datasets could get opposite
            // cluster indices and confuse the UI.
            smile.math.MathEx.setSeed(KMEANS_SEED);
            km = KMeans.fit(features, KMEANS_K);
        } catch (RuntimeException ex) {
            log.warn("K-Means clustering failed ({}); falling back to rule-based tiers.",
                    ex.getMessage());
            return null;
        }

        int[] rawLabels = km.y;
        if (rawLabels == null || rawLabels.length != n) return null;

        // Compute mean revenue per raw cluster so we can sort clusters by
        // performance and remap their indices to 0=TOP..2=LENT.
        double[] sumRev = new double[KMEANS_K];
        int[] countRev = new int[KMEANS_K];
        for (int i = 0; i < n; i++) {
            int c = rawLabels[i];
            if (c >= 0 && c < KMEANS_K) {
                sumRev[c] += revenues[i];
                countRev[c]++;
            }
        }
        Integer[] order = new Integer[KMEANS_K];
        for (int c = 0; c < KMEANS_K; c++) order[c] = c;
        Arrays.sort(order, (a, b) -> {
            double ma = countRev[a] > 0 ? sumRev[a] / countRev[a] : Double.NEGATIVE_INFINITY;
            double mb = countRev[b] > 0 ? sumRev[b] / countRev[b] : Double.NEGATIVE_INFINITY;
            return Double.compare(mb, ma); // descending
        });
        int[] remap = new int[KMEANS_K];
        for (int tier = 0; tier < KMEANS_K; tier++) {
            remap[order[tier]] = tier;
        }

        int[] tiers = new int[n];
        for (int i = 0; i < n; i++) {
            int c = rawLabels[i];
            tiers[i] = (c >= 0 && c < KMEANS_K) ? remap[c] : -1;
        }
        return tiers;
    }

    /** Returns [mean, sampleStdDev] for a non-empty array. */
    private static double[] meanStd(double[] v) {
        if (v.length == 0) return new double[]{0.0, 0.0};
        double sum = 0.0;
        for (double x : v) sum += x;
        double mean = sum / v.length;
        if (v.length < 2) return new double[]{mean, 0.0};
        double sq = 0.0;
        for (double x : v) { double d = x - mean; sq += d * d; }
        return new double[]{mean, Math.sqrt(sq / (v.length - 1))};
    }
}
