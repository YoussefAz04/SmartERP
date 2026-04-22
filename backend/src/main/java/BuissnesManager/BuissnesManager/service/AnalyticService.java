package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ModelMetricsDto;
import BuissnesManager.BuissnesManager.dto.MonthlySalesDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.dto.SalesAnalysisDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import BuissnesManager.BuissnesManager.service.ml.FeatureScaler;
import BuissnesManager.BuissnesManager.service.ml.LoessSmoother;
import org.springframework.cache.annotation.Cacheable;
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
    static final int KMEANS_K = 3;

    /** Candidate cluster counts explored when auto-selecting via silhouette. */
    private static final int[] KMEANS_K_CANDIDATES = {2, 3, 4, 5};

    /** Fixed seed so clustering is deterministic between requests. */
    private static final long KMEANS_SEED = 42L;

    /**
     * Number of independent K-Means restarts. K-Means converges to a local
     * optimum that depends on the initial centroids; running the algorithm
     * N times and keeping the partition with the lowest within-cluster
     * distortion (sum of squared distances to the assigned centroid) is the
     * standard way to get a more robust, higher-quality fit. Smile's own
     * {@code KMeans.fit} already uses k-means++ seeding, but restarting is
     * still worth it on small product catalogues where a single bad init
     * could glue two tiers together.
     */
    private static final int KMEANS_RESTARTS = 8;

    /**
     * Cross-module cache key: {@code (entrepriseId, dayOfYear)}. Heavy ML
     * work (LOESS, bootstrap CI, K-Means + silhouette grid-search) is stable
     * within a calendar day for a given enterprise, so repeated calls from
     * the Angular dashboard reuse the same DTO instead of retraining every
     * time the user switches tabs.
     */
    @Cacheable(cacheNames = "salesAnalysis",
               key = "#entrepriseId + '-' + T(java.time.LocalDate).now().getDayOfYear()")
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

        // Growth rate via bootstrapped log-OLS regression on the active month
        // series. The slope β of log(y) ~ α + β·t (t in 0..n-1) is the monthly
        // log-growth rate; annualised = exp(12β) − 1. We bootstrap the active
        // months with replacement to also surface a 95% confidence interval.
        GrowthEstimate growth = estimateAnnualGrowth(values);
        double growthRate = growth.pointEstimatePercent;
        if (firstHalfFallbackNeeded(values)) {
            // Fallback: simple half-year ratio when log-OLS is under-determined
            // (< 2 non-zero months) so brand-new businesses still get a number.
            double firstHalf = values.subList(0, 6).stream().mapToDouble(Double::doubleValue).sum();
            double secondHalf = values.subList(6, 12).stream().mapToDouble(Double::doubleValue).sum();
            if (firstHalf > 0) {
                growthRate = ((secondHalf - firstHalf) / firstHalf) * 100.0;
                if (growthRate >  9999.0) growthRate =  9999.0;
                if (growthRate < -9999.0) growthRate = -9999.0;
            } else {
                growthRate = 0.0;
            }
        }

        // LOESS trend line — a locally-weighted linear regression that tracks
        // the local slope of the series instead of lagging it like a 3-month
        // moving average. Bandwidth 0.5 is the standard default.
        List<Double> trendLine = computeLoessTrend(values);

        // Anomaly flags — z-score on the residuals of the LOESS fit. Any
        // month more than ±2 residual-σ from the smoothed trend is tagged.
        List<Boolean> anomalies = detectAnomalies(values, trendLine);

        SalesAnalysisDto dto = new SalesAnalysisDto();
        dto.setMonthlySales(monthlySales);
        dto.setTotalRevenue(totalRevenue);
        dto.setAverageMonthlyRevenue(avgRevenue);
        dto.setGrowthRate(Math.round(growthRate * 10.0) / 10.0);
        dto.setBestMonth(bestMonth);
        dto.setBestMonthRevenue(bestRevenue);
        dto.setTrendLine(trendLine);
        dto.setAnomalies(anomalies);
        if (!Double.isNaN(growth.lowerPercent)) {
            dto.setGrowthRateLower(Math.round(growth.lowerPercent * 10.0) / 10.0);
        }
        if (!Double.isNaN(growth.upperPercent)) {
            dto.setGrowthRateUpper(Math.round(growth.upperPercent * 10.0) / 10.0);
        }
        dto.setMethodology(buildSalesMethodology(growth, values));

        // Diagnostics bundle for the sales-analysis block.
        if (growth.samples >= 2) {
            ModelMetricsDto metrics = new ModelMetricsDto();
            metrics.setModelName("OLS log-linéaire + bootstrap");
            metrics.setSamples(growth.samples);
            metrics.setFeatures(1);
            metrics.setRSquared(Math.round(growth.rSquared * 1000.0) / 1000.0);
            dto.setModelMetrics(metrics);
        }

        return dto;
    }

    @Cacheable(cacheNames = "productIntelligence",
               key = "#entrepriseId + '-' + T(java.time.LocalDate).now().getDayOfYear()")
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
     * Clusters products into performance tiers via Smile's {@link KMeans}
     * on a feature vector of (revenue, quantity, log-avg-unit-price). The
     * number of clusters {@code k} is selected automatically over
     * {@link #KMEANS_K_CANDIDATES} using the mean silhouette coefficient
     * — a standard unsupervised criterion ([-1, 1], higher is better) that
     * rewards tight, well-separated clusters.
     *
     * <p>The clusters returned by the algorithm carry arbitrary numeric
     * labels. To keep the frontend contract stable (3 display tiers:
     * TOP / BON / LENT) we remap each cluster to the display tier whose
     * mean revenue falls into the corresponding tercile:
     * <ul>
     *   <li>top-third mean-revenue clusters → <b>0 (TOP)</b></li>
     *   <li>middle-third → <b>1 (BON)</b></li>
     *   <li>bottom-third → <b>2 (LENT)</b></li>
     * </ul>
     * The returned array is aligned with the input list so caller can look
     * up the tier of product i in O(1). Returns {@code null} when clustering
     * fails for any numerical reason so the caller can gracefully fall back
     * to rule-based labelling.
     */
    int[] clusterProductsByPerformance(List<ProductIntelligenceDto.ProductPerformanceDto> products) {
        int n = products.size();
        if (n < KMEANS_K) return null;

        double[] revenues = new double[n];
        double[] quantities = new double[n];
        double[] avgOrderValues = new double[n];
        for (int i = 0; i < n; i++) {
            revenues[i] = products.get(i).getRevenue();
            quantities[i] = products.get(i).getQuantitySold();
            // Engineered feature: log-average unit price. Distinguishes a
            // product selling a handful of high-value items from one selling
            // many low-value items at a similar total revenue. log1p makes
            // the feature robust to zero quantities and compresses the long
            // tail of very-expensive items.
            double qty = Math.max(1.0, quantities[i]);
            avgOrderValues[i] = Math.log1p(Math.max(0.0, revenues[i]) / qty);
        }

        // Standardise via our shared scaler so the Euclidean distance used
        // by K-Means doesn't let revenue completely dominate quantity.
        double[][] raw = new double[n][3];
        for (int i = 0; i < n; i++) {
            raw[i][0] = revenues[i];
            raw[i][1] = quantities[i];
            raw[i][2] = avgOrderValues[i];
        }
        double[][] features = FeatureScaler.scale(raw, FeatureScaler.Mode.Z_SCORE);

        // Grid-search over k and pick the partition with the highest mean
        // silhouette score. Each k runs best-of-N K-Means to fight local optima.
        KMeans bestKm = null;
        int bestK = KMEANS_K;
        double bestSilhouette = Double.NEGATIVE_INFINITY;
        for (int k : KMEANS_K_CANDIDATES) {
            if (k >= n) continue;
            try {
                smile.math.MathEx.setSeed(KMEANS_SEED + k);
                KMeans km = bestOfNKMeans(features, k, KMEANS_RESTARTS);
                double sil = meanSilhouette(features, km.y, k);
                if (sil > bestSilhouette) {
                    bestSilhouette = sil;
                    bestKm = km;
                    bestK = k;
                }
            } catch (RuntimeException ex) {
                log.debug("K-Means k={} failed ({}); trying next candidate.", k, ex.getMessage());
            }
        }
        if (bestKm == null) {
            log.warn("K-Means clustering failed for all k candidates; falling back to rule-based tiers.");
            return null;
        }

        int[] rawLabels = bestKm.y;
        if (rawLabels == null || rawLabels.length != n) return null;

        // Mean revenue per raw cluster → used to rank clusters and remap them
        // into the 3 display tiers (TOP / BON / LENT).
        double[] sumRev = new double[bestK];
        int[] countRev = new int[bestK];
        for (int i = 0; i < n; i++) {
            int c = rawLabels[i];
            if (c >= 0 && c < bestK) {
                sumRev[c] += revenues[i];
                countRev[c]++;
            }
        }
        Integer[] order = new Integer[bestK];
        for (int c = 0; c < bestK; c++) order[c] = c;
        Arrays.sort(order, (a, b) -> {
            double ma = countRev[a] > 0 ? sumRev[a] / countRev[a] : Double.NEGATIVE_INFINITY;
            double mb = countRev[b] > 0 ? sumRev[b] / countRev[b] : Double.NEGATIVE_INFINITY;
            return Double.compare(mb, ma); // descending
        });
        int[] remap = new int[bestK];
        for (int rank = 0; rank < bestK; rank++) {
            // rank 0 (highest mean revenue) → display tier 0 (TOP)
            // rank bestK-1 (lowest mean revenue) → display tier 2 (LENT)
            int tier;
            if (rank < bestK / 3 + (bestK % 3 > 0 ? 1 : 0)) {
                tier = 0; // TOP third
            } else if (rank < bestK - bestK / 3) {
                tier = 1; // BON middle
            } else {
                tier = 2; // LENT bottom third
            }
            // For k=3 the above degenerates to rank==tier, which is the
            // classic mapping; for k=5 we'd get {0,0,1,2,2} for example.
            if (bestK == 3) tier = rank;
            remap[order[rank]] = tier;
        }

        int[] tiers = new int[n];
        for (int i = 0; i < n; i++) {
            int c = rawLabels[i];
            tiers[i] = (c >= 0 && c < bestK) ? remap[c] : -1;
        }
        if (log.isDebugEnabled()) {
            log.debug("Product clustering: picked k={} (silhouette={})", bestK, bestSilhouette);
        }
        return tiers;
    }

    /**
     * Mean silhouette coefficient. For each point <i>i</i>:
     * <ul>
     *   <li>a(i) = mean distance to other points in the same cluster</li>
     *   <li>b(i) = min over other clusters of the mean distance to their points</li>
     *   <li>s(i) = (b − a) / max(a, b), in [−1, 1]</li>
     * </ul>
     * A higher mean s(i) indicates tighter, better-separated clusters.
     * Pure Java because Smile 3.1.1 ships silhouette only as part of its
     * (not bundled) validation metric DataFrame API.
     */
    static double meanSilhouette(double[][] features, int[] labels, int k) {
        int n = features.length;
        if (n <= 1 || labels == null || labels.length != n) return 0.0;
        double total = 0.0;
        int counted = 0;
        for (int i = 0; i < n; i++) {
            int ci = labels[i];
            if (ci < 0 || ci >= k) continue;
            double[] sumPerCluster = new double[k];
            int[] countPerCluster = new int[k];
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                int cj = labels[j];
                if (cj < 0 || cj >= k) continue;
                sumPerCluster[cj] += euclidean(features[i], features[j]);
                countPerCluster[cj]++;
            }
            double a = countPerCluster[ci] > 0 ? sumPerCluster[ci] / countPerCluster[ci] : 0.0;
            double b = Double.POSITIVE_INFINITY;
            for (int c = 0; c < k; c++) {
                if (c == ci || countPerCluster[c] == 0) continue;
                double mean = sumPerCluster[c] / countPerCluster[c];
                if (mean < b) b = mean;
            }
            if (b == Double.POSITIVE_INFINITY) continue;
            double s = Math.max(a, b) > 0 ? (b - a) / Math.max(a, b) : 0.0;
            total += s;
            counted++;
        }
        return counted > 0 ? total / counted : 0.0;
    }

    private static double euclidean(double[] a, double[] b) {
        double sum = 0.0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }


    /**
     * Runs K-Means {@code restarts} times with different initialisations and
     * returns the partition with the lowest within-cluster distortion. Smile's
     * {@code KMeans.distortion} field holds the sum of squared distances from
     * each point to its assigned centroid — the classic K-Means objective —
     * so "best distortion" means "tightest clusters".
     *
     * <p>This is the standard multi-restart pattern for K-Means. A single
     * run is known to get stuck in local optima, especially on small
     * datasets; averaging over {@code N} runs would destroy the discrete
     * labelling, but taking the best run is always strictly better than a
     * single run.
     */
    static KMeans bestOfNKMeans(double[][] features, int k, int restarts) {
        KMeans best = null;
        for (int i = 0; i < restarts; i++) {
            KMeans candidate = KMeans.fit(features, k);
            if (best == null || candidate.distortion < best.distortion) {
                best = candidate;
            }
        }
        return best;
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

    /* =========================== log-OLS growth =========================== */

    /** Result of {@link #estimateAnnualGrowth(List)}. */
    static final class GrowthEstimate {
        final double pointEstimatePercent;
        final double lowerPercent;
        final double upperPercent;
        final double rSquared;
        final int samples;
        GrowthEstimate(double p, double lo, double hi, double r2, int n) {
            this.pointEstimatePercent = p;
            this.lowerPercent = lo;
            this.upperPercent = hi;
            this.rSquared = r2;
            this.samples = n;
        }
    }

    /**
     * Estimates the annualised growth rate (in percent) by fitting
     * {@code log(y) ~ α + β·t} on the non-zero monthly values and
     * returning <code>(exp(12·β) − 1) × 100</code>. The 95% confidence
     * interval comes from a bootstrap over the indices, which makes the
     * estimate robust to single-month outliers.
     */
    GrowthEstimate estimateAnnualGrowth(List<Double> values) {
        List<double[]> pts = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            double v = values.get(i);
            if (v > 0) pts.add(new double[]{i, Math.log(v)});
        }
        int n = pts.size();
        if (n < 2) return new GrowthEstimate(0.0, Double.NaN, Double.NaN, 0.0, n);

        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) { xs[i] = pts.get(i)[0]; ys[i] = pts.get(i)[1]; }
        double[] fit = olsSlope(xs, ys);
        double beta = fit[1];
        double r2 = fit[2];

        // Bootstrap the β coefficient for a 95% CI.
        int B = 300;
        double[] betas = new double[B];
        java.util.Random rng = new java.util.Random(7919L);
        double[] xb = new double[n];
        double[] yb = new double[n];
        for (int b = 0; b < B; b++) {
            for (int i = 0; i < n; i++) {
                int k = rng.nextInt(n);
                xb[i] = xs[k];
                yb[i] = ys[k];
            }
            try {
                betas[b] = olsSlope(xb, yb)[1];
            } catch (RuntimeException ex) {
                betas[b] = beta;
            }
        }
        double[] sorted = betas.clone();
        java.util.Arrays.sort(sorted);
        double betaLo = percentile(sorted, 0.025);
        double betaHi = percentile(sorted, 0.975);

        double pointPct = (Math.exp(12.0 * beta) - 1.0) * 100.0;
        double loPct = (Math.exp(12.0 * betaLo) - 1.0) * 100.0;
        double hiPct = (Math.exp(12.0 * betaHi) - 1.0) * 100.0;
        pointPct = clampPercent(pointPct);
        loPct = clampPercent(loPct);
        hiPct = clampPercent(hiPct);
        return new GrowthEstimate(pointPct, loPct, hiPct, r2, n);
    }

    /** Standard OLS closed-form: returns [intercept, slope, R²]. */
    private static double[] olsSlope(double[] x, double[] y) {
        int n = x.length;
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (int i = 0; i < n; i++) {
            sx += x[i]; sy += y[i];
            sxx += x[i] * x[i]; sxy += x[i] * y[i];
        }
        double denom = n * sxx - sx * sx;
        if (Math.abs(denom) < 1e-12) return new double[]{sy / n, 0.0, 0.0};
        double beta = (n * sxy - sx * sy) / denom;
        double alpha = (sy - beta * sx) / n;
        double ym = sy / n;
        double ssRes = 0, ssTot = 0;
        for (int i = 0; i < n; i++) {
            double yhat = alpha + beta * x[i];
            ssRes += (y[i] - yhat) * (y[i] - yhat);
            ssTot += (y[i] - ym) * (y[i] - ym);
        }
        double r2 = ssTot > 0 ? 1.0 - ssRes / ssTot : 0.0;
        return new double[]{alpha, beta, Math.max(0.0, r2)};
    }

    private static double percentile(double[] sorted, double q) {
        if (sorted.length == 0) return 0.0;
        double idx = q * (sorted.length - 1);
        int lo = (int) Math.floor(idx);
        int hi = (int) Math.ceil(idx);
        if (lo == hi) return sorted[lo];
        double frac = idx - lo;
        return sorted[lo] * (1.0 - frac) + sorted[hi] * frac;
    }

    private static double clampPercent(double p) {
        if (p >  9999.0) return  9999.0;
        if (p < -99.9)   return -99.9;   // losing >100% is arithmetically impossible on a log-scale
        return p;
    }

    private static boolean firstHalfFallbackNeeded(List<Double> values) {
        int nonZero = 0;
        for (double v : values) if (v > 0) nonZero++;
        return nonZero < 2;
    }

    /* ============================= LOESS trend ============================= */

    /**
     * LOESS-based trend line with a 0.5-length bandwidth. Uses tricube
     * weighting and a degree-1 local fit, which tracks the slope of the
     * series and does not lag the way a 3-month moving average does.
     */
    List<Double> computeLoessTrend(List<Double> values) {
        double[] y = new double[values.size()];
        for (int i = 0; i < values.size(); i++) y[i] = values.get(i);
        double[] smoothed = LoessSmoother.smooth(y, 0.5);
        List<Double> result = new ArrayList<>(smoothed.length);
        for (double v : smoothed) {
            result.add(Math.round(Math.max(0.0, v) * 100.0) / 100.0);
        }
        return result;
    }

    /* ========================= Anomaly detection ========================= */

    /**
     * Flags monthly observations whose residual against the LOESS trend
     * exceeds ±2 standard deviations — a standard z-score outlier rule.
     * Returns one boolean per input month (same length as {@code values});
     * returns an empty list when there isn't enough signal to compute a std.
     */
    List<Boolean> detectAnomalies(List<Double> values, List<Double> trend) {
        int n = values.size();
        if (trend == null || trend.size() != n) return new ArrayList<>();
        double[] residuals = new double[n];
        int effective = 0;
        for (int i = 0; i < n; i++) {
            if (values.get(i) > 0) {
                residuals[i] = values.get(i) - trend.get(i);
                effective++;
            } else {
                residuals[i] = 0.0;
            }
        }
        if (effective < 3) {
            List<Boolean> empty = new ArrayList<>(n);
            for (int i = 0; i < n; i++) empty.add(false);
            return empty;
        }
        double[] ms = FeatureScaler.meanStd(residuals);
        double sigma = ms[1] > 0 ? ms[1] : 1.0;
        List<Boolean> flags = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (values.get(i) > 0) {
                flags.add(Math.abs(residuals[i] - ms[0]) > 2.0 * sigma);
            } else {
                flags.add(false);
            }
        }
        return flags;
    }

    private static String buildSalesMethodology(GrowthEstimate g, List<Double> values) {
        int nonZero = 0;
        for (double v : values) if (v > 0) nonZero++;
        if (g.samples >= 2) {
            return String.format(
                "Tendance LOESS (bande 0.5, noyau tricube) + taux de croissance OLS log-linéaire bootstrap (%d mois actifs, R²=%.2f)",
                g.samples, g.rSquared);
        }
        return String.format(
                "Tendance LOESS (bande 0.5) — %d mois actifs, croissance calculée par ratio semestriel",
                nonZero);
    }
}