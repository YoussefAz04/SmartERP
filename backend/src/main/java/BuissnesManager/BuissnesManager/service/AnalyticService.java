package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ModelMetricsDto;
import BuissnesManager.BuissnesManager.dto.MonthlySalesDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.dto.SalesAnalysisDto;
import BuissnesManager.BuissnesManager.entity.ArticleVente;
import BuissnesManager.BuissnesManager.repository.ArticleVenteRepo;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.DetaisFactureDeVenteRepo;
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
import java.util.Map;

@Service
public class AnalyticService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticService.class);

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private StockRepository stockRepository;

    
    @Autowired(required = false)
    private DetaisFactureDeVenteRepo detaisFactureDeVenteRepo;

   
    @Autowired(required = false)
    private ArticleVenteRepo articleVenteRepo;

    private static final String[] MONTH_NAMES_FR = {
        "Jan", "Fév", "Mars", "Avr", "Mai", "Juin",
        "Juil", "Août", "Sept", "Oct", "Nov", "Déc"
    };

 
    private static final int MIN_KMEANS_PRODUCTS = 15;

    
    static final int KMEANS_K = 3;

    
    private static final int[] KMEANS_K_CANDIDATES = {2, 3, 4, 5};

    
    private static final long KMEANS_SEED = 42L;

  
    private static final int KMEANS_RESTARTS = 8;

   
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
        
        
        
        long activeMonths = values.stream().filter(v -> v > 0).count();
        double avgRevenue = activeMonths > 0 ? totalRevenue / activeMonths : 0.0;

        
        double bestRevenue = 0.0;
        String bestMonth = MONTH_NAMES_FR[0];
        for (int i = 0; i < 12; i++) {
            if (values.get(i) > bestRevenue) {
                bestRevenue = values.get(i);
                bestMonth = MONTH_NAMES_FR[i];
            }
        }

     
        GrowthEstimate growth = estimateAnnualGrowth(values);
        double growthRate = growth.pointEstimatePercent;
        if (firstHalfFallbackNeeded(values)) {
            
            
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

        List<Double> trendLine = computeLoessTrend(values);

        
        
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

        
        if (growth.samples >= 2) {
            ModelMetricsDto metrics = new ModelMetricsDto();
            metrics.setModelName("Log-linear OLS + bootstrap");
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

        
        
        
        
        
        if (topRaw.isEmpty() && detaisFactureDeVenteRepo != null) {
            try {
                List<Object[]> fallback = detaisFactureDeVenteRepo.getTopProductsFromFactures(entrepriseId);
                if (fallback != null && !fallback.isEmpty()) {
                    topRaw = fallback;
                }
            } catch (Exception e) {
                
                log.warn("Top-products fallback (DetailsFactureDeVente) failed for entreprise {}: {}",
                        entrepriseId, e.getMessage());
            }
        }

        double totalRevenue = 0.0;
        List<ProductIntelligenceDto.ProductPerformanceDto> products = new ArrayList<>();
        for (Object[] row : topRaw) {
            String name = row[0] != null ? row[0].toString() : "Inconnu";
            Long qty = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Double rev = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            products.add(new ProductIntelligenceDto.ProductPerformanceDto(name, qty, rev));
            totalRevenue += rev;
        }


        boolean catalogueFallback = false;
        if (products.isEmpty() && articleVenteRepo != null) {
            try {
                List<ArticleVente> catalogue = articleVenteRepo.findByEntrepriseId(entrepriseId);
                if (catalogue != null && !catalogue.isEmpty()) {
                    catalogue.sort(Comparator.comparingInt(
                        (ArticleVente av) -> av.getQuantite() != null ? av.getQuantite() : 0
                    ).reversed());
                    for (ArticleVente av : catalogue) {
                        String name = av.getNom();
                        if (name == null || name.isBlank()) {
                            if (av.getArticleAchat() != null && av.getArticleAchat().getNom() != null) {
                                name = av.getArticleAchat().getNom();
                            }
                        }
                        if (name == null || name.isBlank()) {
                            name = "Produit #" + av.getId();
                        }
                        products.add(new ProductIntelligenceDto.ProductPerformanceDto(name, 0L, 0.0));
                    }
                    catalogueFallback = true;
                }
            } catch (Exception e) {
                
                log.warn("Catalogue fallback (ArticleVente) failed for entreprise {}: {}",
                        entrepriseId, e.getMessage());
            }
        }

        
        for (int i = 0; i < products.size(); i++) {
            ProductIntelligenceDto.ProductPerformanceDto p = products.get(i);
            p.setRank(i + 1);
            p.setRevenueShare(totalRevenue > 0 ? Math.round((p.getRevenue() / totalRevenue) * 1000.0) / 10.0 : 0.0);
        }

  
        int[] clusterLabels = null;
        if (products.size() >= MIN_KMEANS_PRODUCTS) {
            clusterLabels = clusterProductsByPerformance(products);
        }

        if (catalogueFallback) {
            
            
            
            for (ProductIntelligenceDto.ProductPerformanceDto p : products) {
                p.setPerformanceLabel("NOUVEAU");
            }
        } else if (clusterLabels != null) {
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

    
        List<ProductIntelligenceDto.ProductPerformanceDto> topProducts =
            new ArrayList<>(products.subList(0, Math.min(10, products.size())));

        List<ProductIntelligenceDto.CategoryPerformanceDto> categoryPerformance = computeCategoryPerformance(entrepriseId);

        long totalSKUs = stockRepository.getTotalSKUs(entrepriseId);

        ProductIntelligenceDto dto = new ProductIntelligenceDto();
        dto.setTopProducts(topProducts);
        dto.setCategoryPerformance(categoryPerformance);
        dto.setTotalProducts((int) totalSKUs);
        dto.setActiveProducts((int) totalSKUs);

        return dto;
    }

    private List<ProductIntelligenceDto.CategoryPerformanceDto> computeCategoryPerformance(Long entrepriseId) {
        
        
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

   
    int[] clusterProductsByPerformance(List<ProductIntelligenceDto.ProductPerformanceDto> products) {
        int n = products.size();
        if (n < KMEANS_K) return null;

        double[] revenues = new double[n];
        double[] quantities = new double[n];
        double[] avgOrderValues = new double[n];
        for (int i = 0; i < n; i++) {
            revenues[i] = products.get(i).getRevenue();
            quantities[i] = products.get(i).getQuantitySold();
       
            double qty = Math.max(1.0, quantities[i]);
            avgOrderValues[i] = Math.log1p(Math.max(0.0, revenues[i]) / qty);
        }

        
        
        double[][] raw = new double[n][3];
        for (int i = 0; i < n; i++) {
            raw[i][0] = revenues[i];
            raw[i][1] = quantities[i];
            raw[i][2] = avgOrderValues[i];
        }
        double[][] features = FeatureScaler.scale(raw, FeatureScaler.Mode.Z_SCORE);

        
        
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
            return Double.compare(mb, ma); 
        });
        int[] remap = new int[bestK];
       
        int topSize = (int) Math.ceil(bestK / 3.0);
        int lentStart = bestK - topSize;
        for (int rank = 0; rank < bestK; rank++) {
            int tier;
            if (rank < topSize) tier = 0;           
            else if (rank >= lentStart) tier = 2;   
            else tier = 1;                          
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
        if (p < -99.9)   return -99.9;   
        return p;
    }

    private static boolean firstHalfFallbackNeeded(List<Double> values) {
        int nonZero = 0;
        for (double v : values) if (v > 0) nonZero++;
        return nonZero < 2;
    }

    

   
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
                "LOESS trend (bande 0.5, noyau tricube) + taux de croissance OLS log-linéaire bootstrap (%d mois actifs, R²=%.2f)",
                g.samples, g.rSquared);
        }
        return String.format(
                "LOESS trend (bande 0.5) — %d mois actifs, croissance calculée par ratio semestriel",
                nonZero);
    }
}
