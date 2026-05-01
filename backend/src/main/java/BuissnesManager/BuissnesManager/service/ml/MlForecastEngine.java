package BuissnesManager.BuissnesManager.service.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.type.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Supervised machine-learning forecast engine (Smile 3.1.1).
 *
 * <p>This engine replaces the hand-coded Holt-Winters smoothing for series
 * long enough to let a real model learn. The pipeline is:
 *
 * <ol>
 *   <li><b>Feature engineering</b> — build a (X, y) matrix from the raw
 *       monthly sales series, using either a small 4-feature set (short
 *       histories) or a wide 7-feature set (histories ≥ 18 months).</li>
 *
 *   <li><b>Four candidate learners</b> —
 *       {@link smile.regression.OLS}, {@link smile.regression.RidgeRegression}
 *       (L2), {@link smile.regression.ElasticNet} (L1+L2) and
 *       {@link smile.regression.RandomForest} (non-linear tree ensemble).
 *       The linear family is fast and interpretable; the random forest
 *       captures non-linear interactions the linear models can't.</li>
 *
 *   <li><b>Walk-forward time-series cross-validation</b> — holds out the last
 *       {@code cvFolds} months and retrains on the expanding prefix. For each
 *       fold we compute both <b>MAPE</b> and <b>sMAPE</b>; the selection
 *       criterion is a blended score
 *       <code>0.5·sMAPE + 0.5·min(MAPE, 1)</code> which penalises MAPE's bias
 *       against near-zero actuals while still rewarding models that track
 *       larger values well.</li>
 *
 *   <li><b>Model selection</b> — the learner with the lowest blended OOS
 *       score is refit on the full series and used to produce the forecast.
 *       The cross-validated RMSE / MAPE / sMAPE are exposed in
 *       {@link MlForecastResult} so callers can show honest accuracy to
 *       users rather than only an in-sample R².</li>
 *
 *   <li><b>Bootstrap prediction intervals</b> — instead of the classic
 *       ±σ·√h band (which assumes Gaussian iid residuals), we resample the
 *       in-sample residuals with replacement and, at each bootstrap draw,
 *       re-run the recursive forecast to collect a distribution of
 *       plausible values per horizon. The 2.5-th / 97.5-th percentiles of
 *       that distribution form the 95% prediction interval — they
 *       correctly widen with horizon without assuming normality, and
 *       automatically handle the floor-at-zero clamp.</li>
 * </ol>
 *
 * <p>Every prediction is clamped at 0 (sales can never go negative).
 * Everything is pure Java via Smile; there is no Python or external model.
 */
@Component
public class MlForecastEngine {

    private static final Logger log = LoggerFactory.getLogger(MlForecastEngine.class);

    /** Minimum observations required to fit the small model. */
    public static final int MIN_SAMPLES = 12;

    /** Minimum observations required to switch to the wide feature set. */
    public static final int WIDE_MIN_SAMPLES = 18;

    /** Minimum observations required to enable the RandomForest candidate. */
    public static final int RF_MIN_SAMPLES = 24;

    /** Ridge shrinkage strength for the L2 candidate. */
    static final double RIDGE_LAMBDA = 0.5;
    /** Additional Ridge λ values explored alongside {@link #RIDGE_LAMBDA}. The
     *  candidate with the lowest blended walk-forward score is kept — this
     *  effectively performs a tiny λ grid-search per series. */
    static final double[] RIDGE_LAMBDA_GRID = { 0.1, 0.5, 1.5, 5.0 };
    /** ElasticNet L1 penalty (LASSO part). */
    static final double ELASTIC_LAMBDA1 = 0.1;
    /** ElasticNet L2 penalty (Ridge part). */
    static final double ELASTIC_LAMBDA2 = 0.3;
    /** Additional ElasticNet (λ1, λ2) pairs explored to widen regularisation
     *  search. Same grid-search rationale as {@link #RIDGE_LAMBDA_GRID}. */
    static final double[][] ELASTIC_LAMBDA_GRID = {
            { 0.05, 0.15 },
            { 0.10, 0.30 },
            { 0.30, 0.60 },
    };

    /** Maximum walk-forward CV folds. */
    private static final int MAX_CV_FOLDS = 4;

    /** Minimum training rows a CV fold must leave behind. */
    private static final int MIN_CV_TRAIN_ROWS = 6;

    /** Small epsilon to avoid div-by-zero in (s)MAPE calculations. */
    private static final double MAPE_EPS = 1.0;

    /** Number of bootstrap draws for the prediction intervals. */
    private static final int BOOTSTRAP_DRAWS = 200;

    /** RNG seed used by the bootstrap — keeps intervals reproducible. */
    private static final long BOOTSTRAP_SEED = 1337L;

    /** RNG seed used to seed RandomForest. */
    private static final long RF_SEED = 42L;

    /** Result bundle returned by {@link #forecast(double[], int, int)}. */
    public static final class MlForecastResult {
        /** One-step-ahead fitted values, aligned with the input history. */
        public final List<Double> fittedHistorical;
        /** Recursive forecast values for months 1..horizon. */
        public final List<Double> futurePoint;
        /** Per-horizon 2.5th percentile from the bootstrap (length = horizon). */
        public final List<Double> lowerBounds;
        /** Per-horizon 97.5th percentile from the bootstrap (length = horizon). */
        public final List<Double> upperBounds;
        /** In-sample residual standard deviation (kept for legacy callers). */
        public final double residualStd;
        /** In-sample coefficient of determination. */
        public final double rSquared;
        /** Number of (X, y) rows used to fit the final model. */
        public final int trainingSamples;
        /** Short name of the selected model. */
        public final String modelName;
        /** Number of engineered features (excluding intercept). */
        public final int featureCount;
        /** Walk-forward (out-of-sample) MAPE of the selected model, [0, 1] or NaN. */
        public final double walkForwardMape;
        /** Walk-forward sMAPE of the selected model, [0, 1] or NaN. */
        public final double walkForwardSmape;
        /** Walk-forward RMSE of the selected model (original units) or NaN. */
        public final double walkForwardRmse;
        /** Number of folds actually evaluated. */
        public final int walkForwardFolds;

        MlForecastResult(List<Double> fittedHistorical, List<Double> futurePoint,
                         List<Double> lowerBounds, List<Double> upperBounds,
                         double residualStd, double rSquared, int trainingSamples,
                         String modelName, int featureCount,
                         double walkForwardMape, double walkForwardSmape,
                         double walkForwardRmse, int walkForwardFolds) {
            this.fittedHistorical = fittedHistorical;
            this.futurePoint = futurePoint;
            this.lowerBounds = lowerBounds;
            this.upperBounds = upperBounds;
            this.residualStd = residualStd;
            this.rSquared = rSquared;
            this.trainingSamples = trainingSamples;
            this.modelName = modelName;
            this.featureCount = featureCount;
            this.walkForwardMape = walkForwardMape;
            this.walkForwardSmape = walkForwardSmape;
            this.walkForwardRmse = walkForwardRmse;
            this.walkForwardFolds = walkForwardFolds;
        }
    }

    /** Returns {@code true} when the series is long enough to train a stable regression. */
    public boolean canFit(int historyLength) {
        return historyLength >= MIN_SAMPLES;
    }

    /**
     * Fits the best of {OLS, Ridge, ElasticNet, RandomForest} — selected by
     * walk-forward CV — on the provided history and produces in-sample fits,
     * a recursive multi-step forecast and bootstrap prediction intervals.
     */
    public MlForecastResult forecast(double[] history, int startMonthIdx, int horizon) {
        if (!canFit(history.length)) {
            throw new IllegalArgumentException(
                "history must have at least " + MIN_SAMPLES + " observations, got " + history.length);
        }
        if (horizon < 1) {
            throw new IllegalArgumentException("horizon must be >= 1");
        }

        boolean wide = history.length >= WIDE_MIN_SAMPLES;
        FeatureSpec spec = wide ? FeatureSpec.WIDE : FeatureSpec.SMALL;

        TrainingMatrix full = buildMatrix(history, startMonthIdx, spec);

        // Candidate learners. RandomForest is only enabled when we have
        // enough samples to grow a non-trivial ensemble without over-fitting.
        // For Ridge / ElasticNet we register several (λ) candidates so the
        // walk-forward CV can pick the best regularisation strength per
        // series instead of relying on a single hand-picked value.
        List<ModelCandidate> candidates = new ArrayList<>();
        candidates.add(new ModelCandidate("OLS",
                (df, schema) -> Predictor.fitOls(df, schema)));
        for (double lambda : RIDGE_LAMBDA_GRID) {
            candidates.add(new ModelCandidate(
                    String.format(java.util.Locale.ROOT, "Ridge(λ=%.2f)", lambda),
                    (df, schema) -> Predictor.fitRidge(df, schema, lambda)));
        }
        for (double[] lp : ELASTIC_LAMBDA_GRID) {
            final double l1 = lp[0];
            final double l2 = lp[1];
            candidates.add(new ModelCandidate(
                    String.format(java.util.Locale.ROOT, "ElasticNet(λ1=%.2f,λ2=%.2f)", l1, l2),
                    (df, schema) -> Predictor.fitElasticNet(df, schema, l1, l2)));
        }
        if (history.length >= RF_MIN_SAMPLES) {
            int mtry = Math.max(1, spec.nFeatures / 2);
            candidates.add(new ModelCandidate("RandomForest",
                    (df, schema) -> Predictor.fitRandomForest(df, schema, 100, 6, mtry, RF_SEED)));
        }

        int nSamples = full.y.length;
        int cvFolds = Math.min(MAX_CV_FOLDS, Math.max(0, nSamples - MIN_CV_TRAIN_ROWS));

        CvScore[] scores = new CvScore[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            scores[i] = walkForwardScore(history, startMonthIdx, spec, cvFolds, candidates.get(i));
        }

        // Blended selection: 0.5 * sMAPE + 0.5 * min(MAPE, 1). sMAPE avoids
        // MAPE's asymmetric penalty near zero actuals; MAPE still matters at
        // larger scales. We clip MAPE at 1 so a single noisy fold doesn't
        // dominate the average.
        int best = 0;
        double bestBlend = Double.NaN;
        for (int i = 0; i < scores.length; i++) {
            double blend = blendedScore(scores[i]);
            if (Double.isNaN(bestBlend) || (!Double.isNaN(blend) && blend < bestBlend)) {
                best = i;
                bestBlend = blend;
            }
        }

        Predictor model = candidates.get(best).fit(full.df, full.schema);

        // In-sample fitted values aligned with history.
        List<Double> fitted = new ArrayList<>(history.length);
        for (int t = 0; t < spec.firstT; t++) fitted.add(history[t]);
        double[] fittedTail = model.fittedValues();
        for (double v : fittedTail) fitted.add(Math.max(0.0, v));

        double residualStd = computeResidualStd(model.residuals());
        double rSquared = model.rSquared();

        // Recursive multi-step forecast (single point estimate).
        List<Double> future = recursiveForecast(history, startMonthIdx, spec, model, horizon);

        // Bootstrap prediction intervals — per horizon, the 2.5/97.5 percentiles
        // of `future + sampled residual` across BOOTSTRAP_DRAWS resamples.
        double[][] pi = bootstrapIntervals(history, startMonthIdx, spec, model,
                model.residuals(), horizon);
        List<Double> lower = new ArrayList<>(horizon);
        List<Double> upper = new ArrayList<>(horizon);
        for (int h = 0; h < horizon; h++) {
            lower.add(pi[0][h]);
            upper.add(pi[1][h]);
        }

        CvScore bestScore = scores[best];
        if (log.isDebugEnabled()) {
            log.debug("ML forecast: picked {} (features={}, samples={}, cvFolds={}, "
                    + "OOS-MAPE={}, OOS-sMAPE={}, OOS-RMSE={}, R²={})",
                    candidates.get(best).name, spec.nFeatures, nSamples, cvFolds,
                    bestScore.mape, bestScore.smape, bestScore.rmse, rSquared);
        }

        return new MlForecastResult(fitted, future, lower, upper, residualStd,
                rSquared, nSamples, candidates.get(best).name, spec.nFeatures,
                bestScore.mape, bestScore.smape, bestScore.rmse, cvFolds);
    }

    private static double blendedScore(CvScore s) {
        if (Double.isNaN(s.mape) && Double.isNaN(s.smape)) return Double.NaN;
        double a = Double.isNaN(s.mape) ? 1.0 : Math.min(1.0, s.mape);
        double b = Double.isNaN(s.smape) ? 1.0 : s.smape;
        return 0.5 * a + 0.5 * b;
    }

    /* ------------------------- Feature engineering ------------------------- */

    private enum FeatureSpec {
        SMALL(4, 1, new String[]{"y", "lag1", "t", "sin_m", "cos_m"}),
        WIDE(7, 2,  new String[]{"y", "lag1", "lag2", "t", "sin_m", "cos_m", "sin_2m", "cos_2m"});

        final int nFeatures;
        final int firstT;
        final String[] columnNames;

        FeatureSpec(int nFeatures, int firstT, String[] columnNames) {
            this.nFeatures = nFeatures;
            this.firstT = firstT;
            this.columnNames = columnNames;
        }
    }

    private static final class TrainingMatrix {
        final DataFrame df;
        final StructType schema;
        final double[] y;
        TrainingMatrix(DataFrame df, StructType schema, double[] y) {
            this.df = df;
            this.schema = schema;
            this.y = y;
        }
    }

    private static TrainingMatrix buildMatrix(double[] history, int startMonthIdx, FeatureSpec spec) {
        int n = history.length;
        int nSamples = n - spec.firstT;
        double[][] frame = new double[nSamples][spec.nFeatures + 1];
        double[] y = new double[nSamples];
        for (int i = 0; i < nSamples; i++) {
            int t = spec.firstT + i;
            double[] features = buildFeatures(history, startMonthIdx, spec, t, n);
            frame[i][0] = history[t];
            y[i] = history[t];
            System.arraycopy(features, 0, frame[i], 1, spec.nFeatures);
        }
        DataFrame df = DataFrame.of(frame, spec.columnNames);
        return new TrainingMatrix(df, df.schema(), y);
    }

    private static double[] buildFeatures(double[] series, int startMonthIdx, FeatureSpec spec,
                                          int t, int nForNormalise) {
        double[] f = new double[spec.nFeatures];
        f[0] = series[t - 1];
        if (spec == FeatureSpec.SMALL) {
            f[1] = (double) t / nForNormalise;
            int month = (startMonthIdx + t) % 12;
            double theta = 2.0 * Math.PI * month / 12.0;
            f[2] = Math.sin(theta);
            f[3] = Math.cos(theta);
            return f;
        }
        f[1] = series[t - 2];
        f[2] = (double) t / nForNormalise;
        int month = (startMonthIdx + t) % 12;
        double theta = 2.0 * Math.PI * month / 12.0;
        f[3] = Math.sin(theta);
        f[4] = Math.cos(theta);
        f[5] = Math.sin(2.0 * theta);
        f[6] = Math.cos(2.0 * theta);
        return f;
    }

    /* ---------------------------- Candidate models ---------------------------- */

    @FunctionalInterface
    private interface LearnerFit { Predictor fit(DataFrame df, StructType schema); }

    private static final class ModelCandidate {
        final String name;
        final LearnerFit fitter;
        ModelCandidate(String name, LearnerFit fitter) {
            this.name = name;
            this.fitter = fitter;
        }
        Predictor fit(DataFrame df, StructType schema) { return fitter.fit(df, schema); }
    }

    /* ------------------------- Walk-forward CV ------------------------- */

    private static final class CvScore {
        final double mape;
        final double smape;
        final double rmse;
        CvScore(double mape, double smape, double rmse) {
            this.mape = mape;
            this.smape = smape;
            this.rmse = rmse;
        }
        static final CvScore NAN = new CvScore(Double.NaN, Double.NaN, Double.NaN);
    }

    /**
     * Walk-forward CV scoring. Returns MAPE, sMAPE, RMSE averaged over the
     * last {@code folds} one-step-ahead predictions. Any fold that throws
     * (singular matrix, numerical blow-up) is skipped rather than failing
     * the whole evaluation.
     */
    private static CvScore walkForwardScore(double[] history, int startMonthIdx,
                                            FeatureSpec spec, int folds,
                                            ModelCandidate candidate) {
        if (folds <= 0) return CvScore.NAN;
        int n = history.length;
        int nSamples = n - spec.firstT;
        int startFold = nSamples - folds;
        if (startFold < MIN_CV_TRAIN_ROWS) return CvScore.NAN;

        double sumMape = 0.0, sumSmape = 0.0, sumSqErr = 0.0;
        int okFolds = 0;
        for (int trainEnd = startFold; trainEnd < nSamples; trainEnd++) {
            int prefixLen = spec.firstT + trainEnd;
            double[] prefix = new double[prefixLen];
            System.arraycopy(history, 0, prefix, 0, prefixLen);
            TrainingMatrix tm;
            Predictor m;
            try {
                tm = buildMatrix(prefix, startMonthIdx, spec);
                m = candidate.fit(tm.df, tm.schema);
            } catch (RuntimeException ex) {
                continue;
            }
            int tNext = prefixLen;
            double[] feats = buildFeatures(history, startMonthIdx, spec, tNext, n);
            double yHat = Math.max(0.0, m.predict(feats));
            double yTrue = history[tNext];
            double denomMape = Math.max(MAPE_EPS, Math.abs(yTrue));
            double denomSmape = Math.max(MAPE_EPS, (Math.abs(yTrue) + Math.abs(yHat)) / 2.0);
            double err = yTrue - yHat;
            sumMape += Math.abs(err) / denomMape;
            sumSmape += Math.abs(err) / denomSmape;
            sumSqErr += err * err;
            okFolds++;
        }
        if (okFolds == 0) return CvScore.NAN;
        return new CvScore(sumMape / okFolds, sumSmape / okFolds,
                Math.sqrt(sumSqErr / okFolds));
    }

    /* -------------------------- Recursive forecast -------------------------- */

    private static List<Double> recursiveForecast(double[] history, int startMonthIdx,
                                                  FeatureSpec spec, Predictor model, int horizon) {
        int n = history.length;
        List<Double> future = new ArrayList<>(horizon);
        double[] extended = new double[n + horizon];
        System.arraycopy(history, 0, extended, 0, n);
        for (int h = 1; h <= horizon; h++) {
            int t = n - 1 + h;
            double[] feats = buildFeatures(extended, startMonthIdx, spec, t, n);
            double pred = Math.max(0.0, model.predict(feats));
            extended[t] = pred;
            future.add(pred);
        }
        return future;
    }

    /**
     * Bootstrap prediction intervals. For each draw we recursively forecast
     * {@code horizon} steps but add a random in-sample residual at each
     * step (with replacement). The 2.5/97.5 percentiles over
     * {@value #BOOTSTRAP_DRAWS} draws form the interval per horizon.
     *
     * <p>Returned shape: {@code [2][horizon]} with row 0 = lower, row 1 = upper.
     */
    private static double[][] bootstrapIntervals(double[] history, int startMonthIdx,
                                                 FeatureSpec spec, Predictor model,
                                                 double[] residuals, int horizon) {
        double[][] draws = new double[BOOTSTRAP_DRAWS][horizon];
        Random rng = new Random(BOOTSTRAP_SEED);
        int n = history.length;
        int r = residuals == null ? 0 : residuals.length;
        for (int b = 0; b < BOOTSTRAP_DRAWS; b++) {
            double[] extended = new double[n + horizon];
            System.arraycopy(history, 0, extended, 0, n);
            for (int h = 1; h <= horizon; h++) {
                int t = n - 1 + h;
                double[] feats = buildFeatures(extended, startMonthIdx, spec, t, n);
                double pred = model.predict(feats);
                double noise = r > 0 ? residuals[rng.nextInt(r)] : 0.0;
                double noisy = Math.max(0.0, pred + noise);
                extended[t] = noisy;
                draws[b][h - 1] = noisy;
            }
        }
        double[][] out = new double[2][horizon];
        double[] col = new double[BOOTSTRAP_DRAWS];
        for (int h = 0; h < horizon; h++) {
            for (int b = 0; b < BOOTSTRAP_DRAWS; b++) col[b] = draws[b][h];
            double[] sorted = col.clone();
            java.util.Arrays.sort(sorted);
            out[0][h] = percentile(sorted, 0.025);
            out[1][h] = percentile(sorted, 0.975);
        }
        return out;
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

    /* -------------------------- Small helpers -------------------------- */

    private static double computeResidualStd(double[] residuals) {
        if (residuals == null || residuals.length < 2) return 0.0;
        double mean = 0.0;
        for (double r : residuals) mean += r;
        mean /= residuals.length;
        double sumSq = 0.0;
        for (double r : residuals) {
            double d = r - mean;
            sumSq += d * d;
        }
        return Math.sqrt(sumSq / (residuals.length - 1));
    }
}
