package BuissnesManager.BuissnesManager.service.ml;

import BuissnesManager.BuissnesManager.dto.RecommendationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import smile.classification.LogisticRegression;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.LinearModel;
import smile.regression.OLS;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


























@Component
public class MlRecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(MlRecommendationEngine.class);

    
    public static final int MIN_CLASSIFIER_SAMPLES = 8;

    
    public static final int MIN_REGRESSION_SAMPLES = 8;

    
    public static final class StockRow {
        public final String articleName;
        public final double stockLevel;
        public final double avgMonthlyDemand;
        public final double demandVariance;
        public final double leadTimeDays;
        public final double daysOfCover;
        public final double seasonalityPhase; 
        public StockRow(String articleName, double stockLevel, double avgMonthlyDemand,
                        double demandVariance, double leadTimeDays, double daysOfCover,
                        double seasonalityPhase) {
            this.articleName = articleName;
            this.stockLevel = Math.max(0.0, stockLevel);
            this.avgMonthlyDemand = Math.max(0.0, avgMonthlyDemand);
            this.demandVariance = Math.max(0.0, demandVariance);
            this.leadTimeDays = Math.max(0.0, leadTimeDays);
            this.daysOfCover = Math.max(0.0, daysOfCover);
            this.seasonalityPhase = seasonalityPhase;
        }
    }

    
    public static final class ProductRow {
        public final String productName;
        public final double revenue;
        public final double quantitySold;
        public final double marginProxy;    
        public final double categoryShare;  
        public final double velocityTrend;  
        public ProductRow(String productName, double revenue, double quantitySold,
                          double marginProxy, double categoryShare, double velocityTrend) {
            this.productName = productName;
            this.revenue = revenue;
            this.quantitySold = quantitySold;
            this.marginProxy = marginProxy;
            this.categoryShare = categoryShare;
            this.velocityTrend = velocityTrend;
        }
    }

    




    public List<RecommendationDto> stockRiskRecommendations(List<StockRow> rows, int horizonDays) {
        List<RecommendationDto> out = new ArrayList<>();
        if (rows == null || rows.size() < MIN_CLASSIFIER_SAMPLES) return out;

        int n = rows.size();
        double[][] x = new double[n][5];
        int[] y = new int[n];
        int positives = 0;
        for (int i = 0; i < n; i++) {
            StockRow r = rows.get(i);
            
            double coverRatio = r.avgMonthlyDemand > 0
                    ? r.stockLevel / r.avgMonthlyDemand : (r.stockLevel > 0 ? 10.0 : 0.0);
            double cv = r.avgMonthlyDemand > 0
                    ? Math.sqrt(Math.max(0.0, r.demandVariance)) / r.avgMonthlyDemand : 0.0;
            x[i][0] = Math.log1p(r.stockLevel);
            x[i][1] = Math.log1p(r.avgMonthlyDemand);
            x[i][2] = coverRatio;
            x[i][3] = cv;
            x[i][4] = r.seasonalityPhase;
            
            
            y[i] = (r.daysOfCover < horizonDays + r.leadTimeDays && r.avgMonthlyDemand > 0) ? 1 : 0;
            if (y[i] == 1) positives++;
        }
        
        if (positives == 0 || positives == n) return out;

        
        double[][] xs = FeatureScaler.scale(x, FeatureScaler.Mode.Z_SCORE);
        LogisticRegression.Binomial clf;
        try {
            clf = LogisticRegression.binomial(xs, y);
        } catch (RuntimeException ex) {
            log.debug("Stock-risk classifier fit failed ({}); skipping ML recommendations.",
                    ex.getMessage());
            return out;
        }

        
        
        double[] probs = new double[n];
        for (int i = 0; i < n; i++) {
            double[] proba = new double[2];
            clf.predict(xs[i], proba);
            probs[i] = proba[1];
        }
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        java.util.Arrays.sort(order, (a, b) -> Double.compare(probs[b], probs[a]));

        for (int rank = 0; rank < n; rank++) {
            int i = order[rank];
            double pStockout = probs[i];
            if (pStockout <= 0.6) break; 
            StockRow r = rows.get(i);
            String prio = pStockout > 0.85 ? "CRITIQUE" : "HAUTE";
            String icon = pStockout > 0.85 ? "fa-triangle-exclamation" : "fa-box-open";
            
            
            double dailyDemand = r.avgMonthlyDemand / 30.0;
            double targetCoverDays = horizonDays + r.leadTimeDays + 30.0;
            double suggestedQty = Math.max(1.0,
                    Math.ceil(dailyDemand * targetCoverDays - r.stockLevel));
            String coverStr = r.daysOfCover >= 365.0
                    ? "≥ 1 an"
                    : String.format(Locale.FRANCE, "%.0f j", r.daysOfCover);
            String urgencyHint = pStockout > 0.85
                    ? "Action urgente : passez une commande fournisseur dès aujourd'hui."
                    : "Passez une commande fournisseur avant que le stock ne soit épuisé.";
            out.add(new RecommendationDto(
                "ML_RISQUE_STOCK",
                "Stockout risk - " + r.articleName,
                String.format(Locale.FRANCE,
                    "Cet article a %.0f %% de risque de tomber en rupture dans les %d prochains jours. Il vous reste environ %s de stock. Vous en vendez %.0f par mois en moyenne, et le fournisseur livre en %.0f jours. Quantity à commander : %.0f unité(s). %s",
                    pStockout * 100.0, horizonDays, coverStr,
                    r.avgMonthlyDemand, r.leadTimeDays, suggestedQty, urgencyHint),
                prio, icon, "View Stock"
            ));
        }
        return out;
    }

    




    public List<RecommendationDto> opportunityRecommendations(List<ProductRow> rows) {
        List<RecommendationDto> out = new ArrayList<>();
        if (rows == null || rows.size() < MIN_REGRESSION_SAMPLES) return out;

        int n = rows.size();
        double[][] frame = new double[n][5];
        double sumRev = 0.0;
        for (ProductRow r : rows) sumRev += Math.max(0.0, r.revenue);
        double avgRev = sumRev / n;
        for (int i = 0; i < n; i++) {
            ProductRow r = rows.get(i);
            
            
            
            double target = Math.log1p(Math.max(0.0, r.revenue))
                    - Math.log1p(Math.max(0.0, avgRev));
            frame[i][0] = target;
            frame[i][1] = Math.log1p(Math.max(0.0, r.quantitySold));
            frame[i][2] = Math.log1p(Math.max(0.0, r.marginProxy));
            frame[i][3] = r.categoryShare;
            frame[i][4] = r.velocityTrend;
        }
        DataFrame df = DataFrame.of(frame,
                new String[]{"y", "logQty", "logMargin", "catShare", "trend"});
        LinearModel model;
        try {
            model = OLS.fit(Formula.lhs("y"), df);
        } catch (RuntimeException ex) {
            log.debug("Opportunity regression fit failed ({}); skipping.", ex.getMessage());
            return out;
        }
        double[] fitted = model.fittedValues();
        double[] residuals = model.residuals();
        
        
        
        
        
        
        
        double[] scores;
        double resAbsMax = 0.0;
        for (double v : residuals) resAbsMax = Math.max(resAbsMax, Math.abs(v));
        double fitAbsMax = 0.0;
        for (double v : fitted) fitAbsMax = Math.max(fitAbsMax, Math.abs(v));
        
        boolean collapsed = resAbsMax < 1e-9
                || (fitAbsMax > 0 && resAbsMax / fitAbsMax < 0.01);
        scores = collapsed ? fitted : residuals;

        
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> Double.compare(scores[b], scores[a]));

        
        int top = idx[0];
        ProductRow topRow = rows.get(top);
        out.add(new RecommendationDto(
            "PRODUIT_STAR",
            "Article à fort potentiel — " + topRow.productName,
            String.format(Locale.FRANCE,
                "« %s » est l'article qui a le plus fort potentiel de croissance d'après l'analyse. Ventes actuelles : %,.0f — Quantity vendue : %.0f unité(s) — Marge par unité estimée : %,.0f. Gardez un stock suffisant, mettez-le en avant et proposez-le avec d'autres articles complémentaires pour vendre encore plus.",
                topRow.productName, topRow.revenue, topRow.quantitySold, topRow.marginProxy),
            "BASSE", "fa-star", "Voir Produits"
        ));

        
        int opportunityCap = Math.min(3, Math.max(0, n / 10));
        for (int rank = 1; rank <= opportunityCap && rank < n; rank++) {
            int i = idx[rank];
            ProductRow r = rows.get(i);
            String hint;
            if (r.velocityTrend > 0.1) {
                hint = "Ses ventes progressent — mettez-le davantage en avant pour profiter de la tendance.";
            } else if (r.categoryShare > 0.15) {
                hint = "Il représente %.0f %% de sa famille — étoffez les articles autour de lui.";
                hint = String.format(Locale.FRANCE, hint, r.categoryShare * 100.0);
            } else {
                hint = "Mettez-le en avant pour profiter de cette opportunité de croissance.";
            }
            out.add(new RecommendationDto(
                "OPPORTUNITE",
                "Opportunité de vente — " + r.productName,
                String.format(Locale.FRANCE,
                    "« %s » fait partie des articles qui peuvent encore beaucoup progresser. Ventes actuelles : %,.0f — Quantity vendue : %.0f unité(s). %s",
                    r.productName, r.revenue, r.quantitySold, hint),
                "MOYENNE", "fa-arrow-trend-up", "Voir Produits"
            ));
        }
        return out;
    }

    







    public boolean[] classifyDormant(double[][] features, int[] provisionalLabels) {
        if (features == null || features.length < MIN_CLASSIFIER_SAMPLES) return new boolean[0];
        if (provisionalLabels == null || provisionalLabels.length != features.length)
            return new boolean[0];
        int pos = 0;
        for (int v : provisionalLabels) if (v == 1) pos++;
        if (pos == 0 || pos == provisionalLabels.length) return new boolean[0];
        double[][] xs = FeatureScaler.scale(features, FeatureScaler.Mode.Z_SCORE);
        LogisticRegression.Binomial clf;
        try {
            clf = LogisticRegression.binomial(xs, provisionalLabels);
        } catch (RuntimeException ex) {
            return new boolean[0];
        }
        boolean[] dormant = new boolean[features.length];
        double[] proba = new double[2];
        for (int i = 0; i < features.length; i++) {
            clf.predict(xs[i], proba);
            dormant[i] = proba[1] > 0.5;
        }
        return dormant;
    }
}
