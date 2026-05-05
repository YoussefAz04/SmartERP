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

/**
 * ML-driven recommendation engine. Complements (does not replace) the
 * rule-based {@code RecommendationService} by adding data-learned signals:
 *
 * <ul>
 *   <li><b>Stock-risk classifier</b> — logistic regression trained on
 *       per-article features (stock level, velocity, days-of-cover, relative
 *       stock ratio, velocity trend). Label: will the article stock out in
 *       the next N days at its current velocity? When training data is
 *       insufficient (&lt; {@value #MIN_CLASSIFIER_SAMPLES} rows) the engine
 *       returns an empty list so the rule-based fallback stays authoritative.
 *   </li>
 *
 *   <li><b>Opportunity regression</b> — OLS of next-period revenue delta
 *       on (revenue velocity, quantity velocity, margin proxy, category
 *       share). Articles whose predicted delta is in the top decile are
 *       surfaced as <code>OPPORTUNITÉ</code>; the single best-predicted
 *       article is tagged <code>PRODUIT_STAR</code>.</li>
 * </ul>
 *
 * <p>All outputs use the existing {@link RecommendationDto} (type / title
 * / description / priority / icon / action) contract so the Angular frontend
 * keeps rendering the current shape — only the source of truth becomes
 * model-driven rather than threshold-driven.
 */
@Component
public class MlRecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(MlRecommendationEngine.class);

    /** Minimum training rows required to fit the stock-risk classifier. */
    public static final int MIN_CLASSIFIER_SAMPLES = 8;

    /** Minimum rows required to fit the opportunity regression. */
    public static final int MIN_REGRESSION_SAMPLES = 8;

    /** Article stock evidence row. */
    public static final class StockRow {
        public final String articleName;
        public final double stockLevel;
        public final double avgMonthlyDemand;
        public final double demandVariance;
        public final double leadTimeDays;
        public final double daysOfCover;
        public final double seasonalityPhase; // [0, 1]
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

    /** Product opportunity evidence row. */
    public static final class ProductRow {
        public final String productName;
        public final double revenue;
        public final double quantitySold;
        public final double marginProxy;    // revenue / max(qty, 1)
        public final double categoryShare;  // in [0, 1]
        public final double velocityTrend;  // slope of recent-month velocity
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

    /**
     * Runs the stock-risk classifier over the article rows and emits one
     * {@link RecommendationDto} per high-risk article (probability &gt; 0.6).
     * Returns an empty list when training data is insufficient.
     */
    public List<RecommendationDto> stockRiskRecommendations(List<StockRow> rows, int horizonDays) {
        List<RecommendationDto> out = new ArrayList<>();
        if (rows == null || rows.size() < MIN_CLASSIFIER_SAMPLES) return out;

        int n = rows.size();
        double[][] x = new double[n][5];
        int[] y = new int[n];
        int positives = 0;
        for (int i = 0; i < n; i++) {
            StockRow r = rows.get(i);
            // Derived features
            double coverRatio = r.avgMonthlyDemand > 0
                    ? r.stockLevel / r.avgMonthlyDemand : (r.stockLevel > 0 ? 10.0 : 0.0);
            double cv = r.avgMonthlyDemand > 0
                    ? Math.sqrt(Math.max(0.0, r.demandVariance)) / r.avgMonthlyDemand : 0.0;
            x[i][0] = Math.log1p(r.stockLevel);
            x[i][1] = Math.log1p(r.avgMonthlyDemand);
            x[i][2] = coverRatio;
            x[i][3] = cv;
            x[i][4] = r.seasonalityPhase;
            // Label: will stock out in next horizonDays?
            // stock-out happens if daysOfCover < horizonDays + leadTime
            y[i] = (r.daysOfCover < horizonDays + r.leadTimeDays && r.avgMonthlyDemand > 0) ? 1 : 0;
            if (y[i] == 1) positives++;
        }
        // Can't fit a classifier if one class is absent — fall back to silence.
        if (positives == 0 || positives == n) return out;

        // Standardise features so the logistic optimiser converges.
        double[][] xs = FeatureScaler.scale(x, FeatureScaler.Mode.Z_SCORE);
        LogisticRegression.Binomial clf;
        try {
            clf = LogisticRegression.binomial(xs, y);
        } catch (RuntimeException ex) {
            log.debug("Stock-risk classifier fit failed ({}); skipping ML recommendations.",
                    ex.getMessage());
            return out;
        }

        // Compute and rank probabilities so we can surface the most at-risk
        // articles first instead of relying on an arbitrary 0.6 cutoff alone.
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
            if (pStockout <= 0.6) break; // ranked DESC → all remaining are below threshold
            StockRow r = rows.get(i);
            String prio = pStockout > 0.85 ? "CRITIQUE" : "HAUTE";
            String icon = pStockout > 0.85 ? "fa-triangle-exclamation" : "fa-box-open";
            // Suggested order qty: cover (horizon + lead-time + 30j safety) of demand
            // minus current stock; floor at 1 unit so the message is always actionable.
            double dailyDemand = r.avgMonthlyDemand / 30.0;
            double targetCoverDays = horizonDays + r.leadTimeDays + 30.0;
            double suggestedQty = Math.max(1.0,
                    Math.ceil(dailyDemand * targetCoverDays - r.stockLevel));
            String coverStr = r.daysOfCover >= 365.0
                    ? "≥ 1 an"
                    : String.format(Locale.FRANCE, "%.0f j", r.daysOfCover);
            String urgencyHint = pStockout > 0.85
                    ? "Action immédiate requise — émettez un bon de commande sans délai."
                    : "Planifiez le réapprovisionnement avant expiration de la couverture actuelle.";
            out.add(new RecommendationDto(
                "ML_RISQUE_STOCK",
                "Alerte Réapprovisionnement — " + r.articleName,
                String.format(Locale.FRANCE,
                    "Probabilité de rupture estimée à %.0f %% sur l'horizon de %d jours. Couverture actuelle : %s | Demande moyenne : %.0f unités/mois | Délai fournisseur : %.0f j. Quantité de réapprovisionnement recommandée : %.0f unité(s). %s",
                    pStockout * 100.0, horizonDays, coverStr,
                    r.avgMonthlyDemand, r.leadTimeDays, suggestedQty, urgencyHint),
                prio, icon, "Voir Stock"
            ));
        }
        return out;
    }

    /**
     * Fits an OLS regression of log-revenue-growth proxy on product features
     * and emits PRODUIT_STAR / OPPORTUNITÉ recommendations for the top-scored
     * articles. Returns an empty list on insufficient data.
     */
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
            // Target: log-ratio of revenue to the category average. This acts
            // as a relative-performance proxy when the dataset doesn't expose
            // forward-looking truth labels.
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
        // Rank by residual (actual − predicted) so we surface articles that
        // genuinely *outperform* the regression's expectation given their
        // features — that is the real "opportunity" signal. Ranking by the
        // raw fitted value (the previous behaviour) collapses to "sort by
        // revenue" because revenue dominates the target. If residuals are
        // numerically tiny (model overfits the small sample and residuals ~0)
        // we fall back to the fitted ranking to avoid surfacing pure noise.
        double[] scores;
        double resAbsMax = 0.0;
        for (double v : residuals) resAbsMax = Math.max(resAbsMax, Math.abs(v));
        double fitAbsMax = 0.0;
        for (double v : fitted) fitAbsMax = Math.max(fitAbsMax, Math.abs(v));
        // "Collapsed" residuals: residual scale is < 1% of the fitted scale.
        boolean collapsed = resAbsMax < 1e-9
                || (fitAbsMax > 0 && resAbsMax / fitAbsMax < 0.01);
        scores = collapsed ? fitted : residuals;

        // Rank by predicted score.
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (a, b) -> Double.compare(scores[b], scores[a]));

        // Top article → PRODUIT_STAR (ML-sourced).
        int top = idx[0];
        ProductRow topRow = rows.get(top);
        out.add(new RecommendationDto(
            "PRODUIT_STAR",
            "Produit Vedette — " + topRow.productName,
            String.format(Locale.FRANCE,
                "« %s » est identifié par le modèle prédictif comme la référence à plus fort potentiel de croissance. Chiffre d'affaires : %.0f — Unités vendues : %.0f — Marge unitaire estimée : %.0f. Sécurisez le niveau de stock, activez la mise en avant commerciale et testez des offres combinées pour amplifier la dynamique de vente.",
                topRow.productName, topRow.revenue, topRow.quantitySold, topRow.marginProxy),
            "BASSE", "fa-star", "Voir Produits"
        ));

        // Top 10% of remaining articles → OPPORTUNITÉ, capped at 3 to stay readable.
        int opportunityCap = Math.min(3, Math.max(0, n / 10));
        for (int rank = 1; rank <= opportunityCap && rank < n; rank++) {
            int i = idx[rank];
            ProductRow r = rows.get(i);
            String hint;
            if (r.velocityTrend > 0.1) {
                hint = "La tendance récente est favorable — accélérez la mise en avant commerciale.";
            } else if (r.categoryShare > 0.15) {
                hint = "Sa part de catégorie (%.0f %%) en fait un levier stratégique — " +
                       "renforcez l'assortiment associé.";
                hint = String.format(Locale.FRANCE, hint, r.categoryShare * 100.0);
            } else {
                hint = "Investissez en visibilité commerciale pour capitaliser sur ce signal de croissance.";
            }
            out.add(new RecommendationDto(
                "OPPORTUNITE",
                "Opportunité de Croissance — " + r.productName,
                String.format(Locale.FRANCE,
                    "« %s » se distingue parmi les références à fort potentiel de progression. Chiffre d'affaires : %.0f — Unités écoulées : %.0f. %s",
                    r.productName, r.revenue, r.quantitySold, hint),
                "MOYENNE", "fa-arrow-trend-up", "Voir Produits"
            ));
        }
        return out;
    }

    /**
     * Slow-mover classifier — given K-Means-tagged slow-mover candidates and
     * their derived features (days since last sale, velocity trend slope,
     * stock-days cover, category mean velocity), predicts which ones are
     * "truly dormant" vs "seasonally quiet". Returns one boolean per input row
     * (true = dormant, false = seasonally quiet). Returns an empty array when
     * training data is insufficient (caller should default to "dormant").
     */
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
