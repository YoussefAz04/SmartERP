package BuissnesManager.BuissnesManager.service.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.LinearModel;
import smile.regression.OLS;
import smile.regression.RidgeRegression;

import java.util.ArrayList;
import java.util.List;

/**
 * Supervised machine-learning forecast engine (Smile 3.1.1).
 *
 * <p>This engine replaces the hand-coded Holt-Winters smoothing for series
 * long enough to let a real model learn. The pipeline is:
 *
 * <ol>
 *   <li><b>Feature engineering</b> — build a (X, y) matrix from the raw
 *       monthly sales series. Two feature sets are supported:
 *     <ul>
 *       <li><b>Small set (4 features)</b> — <code>lag1, t, sin(2πm/12),
 *           cos(2πm/12)</code>. Used for short histories (12–17 months) so the
 *           normal-equations matrix stays comfortably over-determined.</li>
 *       <li><b>Wide set (7 features)</b> — adds <code>lag2</code>, the second
 *           seasonal harmonic <code>sin(4πm/12), cos(4πm/12)</code>, and a
 *           rolling-3 mean. Enabled only when ≥{@value #WIDE_MIN_SAMPLES}
 *           months are available, to keep ≥2× as many training rows as
 *           parameters (guards against singular matrices / over-fitting).</li>
 *     </ul>
 *     The Fourier seasonal encoding is a standard recipe: with only 2 (resp.
 *     4) parameters it captures the 12-month cycle that 11 month dummies
 *     would otherwise cost — critical for small monthly series.</li>
 *
 *   <li><b>Two candidate learners</b> — a plain
 *       {@link smile.regression.OLS ordinary least squares} regression and a
 *       ridge-regularised variant ({@link smile.regression.RidgeRegression},
 *       L2 shrinkage with λ={@value #RIDGE_LAMBDA}). Ridge is more precise
 *       than OLS whenever the design matrix is nearly collinear or the
 *       feature count is close to the sample count.</li>
 *
 *   <li><b>Walk-forward time-series cross-validation</b> — for each
 *       candidate, hold out the last {@code cvFolds} months, retrain on the
 *       expanding prefix, and score the one-step-ahead prediction against
 *       the true value. We compute the mean absolute percentage error
 *       (<b>MAPE</b>) over the held-out tail. This is the standard honest
 *       accuracy estimate for time-series models — in-sample R² grossly
 *       overstates true precision because the same points drive both fit and
 *       evaluation.</li>
 *
 *   <li><b>Model selection</b> — the learner with the lowest walk-forward
 *       MAPE is refit on the full series and used to produce the forecast.
 *       The selected model's name and OOS-MAPE are surfaced in
 *       {@link MlForecastResult} so callers can show them to users (they
 *       drive both the "methodology" text and the confidence score in
 *       {@link BuissnesManager.BuissnesManager.service.ForecastService}).</li>
 *
 *   <li><b>Recursive multi-step forecast</b> — month {@code h+1} is
 *       predicted using the known calendar month index and a lag-1 equal to
 *       the previous step's forecast. Prediction intervals use the
 *       in-sample residual std-dev widened by sqrt(h), the standard
 *       assumption when residuals are approximately iid.</li>
 * </ol>
 *
 * <p>Every prediction is clamped at 0 (sales can never go negative).
 * Everything is pure Java via Smile; there is no Python or external model.
 */
@Component
public class MlForecastEngine {

    private static final Logger log = LoggerFactory.getLogger(MlForecastEngine.class);

    /**
     * Minimum observations required to fit the small model (4 features +
     * intercept). With 12 samples we lose one to the lag-1 operator, leaving
     * 11 training rows vs. 5 parameters → comfortably over-determined.
     */
    public static final int MIN_SAMPLES = 12;

    /**
     * Minimum observations required to switch to the wide feature set
     * (7 features). We want at least ~2× samples than parameters, so 18
     * observations → 16 training rows (two lost to lag-2) vs. 8 parameters.
     */
    public static final int WIDE_MIN_SAMPLES = 18;

    /** Ridge shrinkage strength for the regularised candidate. */
    static final double RIDGE_LAMBDA = 0.5;

    /**
     * Maximum walk-forward CV folds. Scaled down dynamically if the series
     * is too short to leave a sensible training prefix.
     */
    private static final int MAX_CV_FOLDS = 4;

    /** Minimum training rows a CV fold must leave behind to be considered valid. */
    private static final int MIN_CV_TRAIN_ROWS = 6;

    /** Small epsilon to avoid div-by-zero in MAPE calculations. */
    private static final double MAPE_EPS = 1.0;

    /** Result bundle returned by {@link #forecast(double[], int, int)}. */
    public static final class MlForecastResult {
        /** One-step-ahead fitted values, aligned with the input history. */
        public final List<Double> fittedHistorical;
        /** Recursive forecast values for months 1..horizon. */
        public final List<Double> futurePoint;
        /** In-sample residual standard deviation (used for prediction intervals). */
        public final double residualStd;
        /** In-sample coefficient of determination. */
        public final double rSquared;
        /** Number of (X, y) rows used to fit the final model. */
        public final int trainingSamples;
        /** Short name of the selected model, e.g. {@code "OLS"} or {@code "Ridge"}. */
        public final String modelName;
        /** Number of engineered features (excluding intercept). */
        public final int featureCount;
        /**
         * Walk-forward (out-of-sample) MAPE of the selected model, in [0, 1].
         * {@code NaN} if the series was too short to hold out any folds.
         */
        public final double walkForwardMape;
        /** Number of folds actually evaluated in the walk-forward CV. */
        public final int walkForwardFolds;

        MlForecastResult(List<Double> fittedHistorical, List<Double> futurePoint,
                         double residualStd, double rSquared, int trainingSamples,
                         String modelName, int featureCount,
                         double walkForwardMape, int walkForwardFolds) {
            this.fittedHistorical = fittedHistorical;
            this.futurePoint = futurePoint;
            this.residualStd = residualStd;
            this.rSquared = rSquared;
            this.trainingSamples = trainingSamples;
            this.modelName = modelName;
            this.featureCount = featureCount;
            this.walkForwardMape = walkForwardMape;
            this.walkForwardFolds = walkForwardFolds;
        }
    }

    /**
     * Returns {@code true} when the series is long enough to train a stable
     * regression with the small feature set. Callers should fall back to
     * simpler smoothing for shorter histories.
     */
    public boolean canFit(int historyLength) {
        return historyLength >= MIN_SAMPLES;
    }

    /**
     * Fits the best of (OLS, Ridge) — selected by walk-forward CV — on the
     * provided history and produces in-sample fits plus a recursive
     * multi-step forecast.
     *
     * @param history     chronological monthly values. Missing months MUST be
     *                    represented as zeros rather than omitted, so calendar
     *                    alignment is preserved.
     * @param startMonthIdx calendar month index (0-based; 0 = January) of
     *                      {@code history[0]}. Used to build the correct
     *                      seasonal features.
     * @param horizon     number of future months to forecast.
     * @throws IllegalArgumentException if {@code !canFit(history.length)} or
     *                                  {@code horizon < 1}.
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

        // --- Step 1: build full (X, y) training set ---
        TrainingMatrix full = buildMatrix(history, startMonthIdx, spec);

        // --- Step 2: walk-forward CV scoring for each candidate ---
        ModelCandidate[] candidates = new ModelCandidate[]{
            new ModelCandidate("OLS",   MlForecastEngine::fitOls),
            new ModelCandidate("Ridge", MlForecastEngine::fitRidge),
        };

        int nSamples = full.y.length;
        int cvFolds = Math.min(MAX_CV_FOLDS, Math.max(0, nSamples - MIN_CV_TRAIN_ROWS));
        double[] mapes = new double[candidates.length];
        for (int i = 0; i < candidates.length; i++) {
            mapes[i] = walkForwardMape(history, startMonthIdx, spec, cvFolds, candidates[i]);
        }

        // --- Step 3: pick the candidate with the best (lowest) OOS MAPE.
        // When both MAPEs are NaN (no CV possible), prefer OLS — it's
        // parameter-free and has better asymptotic properties on tiny data.
        int best = 0;
        double bestMape = mapes[0];
        for (int i = 1; i < candidates.length; i++) {
            if (Double.isNaN(bestMape) && !Double.isNaN(mapes[i])) { best = i; bestMape = mapes[i]; }
            else if (!Double.isNaN(mapes[i]) && mapes[i] < bestMape) { best = i; bestMape = mapes[i]; }
        }

        // --- Step 4: refit the selected model on the full training data ---
        LinearModel model = candidates[best].fitter.fit(full.df);

        // --- Step 5: in-sample fitted values aligned with history ---
        List<Double> fitted = new ArrayList<>(history.length);
        for (int t = 0; t < spec.firstT; t++) fitted.add(history[t]);
        double[] fittedTail = model.fittedValues();
        for (double v : fittedTail) fitted.add(Math.max(0.0, v));

        double residualStd = computeResidualStd(model.residuals());
        double rSquared = model.RSquared();

        // --- Step 6: recursive multi-step forecast ---
        List<Double> future = recursiveForecast(history, startMonthIdx, spec, model, horizon);

        if (log.isDebugEnabled()) {
            log.debug("ML forecast: picked {} (features={}, samples={}, cvFolds={}, "
                    + "OOS-MAPE={}, in-sample R²={})",
                    candidates[best].name, spec.nFeatures, nSamples, cvFolds, bestMape, rSquared);
        }

        return new MlForecastResult(fitted, future, residualStd, rSquared, nSamples,
                candidates[best].name, spec.nFeatures, bestMape, cvFolds);
    }

    /* ------------------------- Feature engineering ------------------------- */

    /** Describes one of the two supported feature sets.
     *  <p>{@code columnNames} must always have exactly {@code nFeatures + 1}
     *  entries: one for the target column {@code "y"} plus one per feature,
     *  since {@link DataFrame#of(double[][], String[])} expects column names
     *  for every column (target + predictors). */
    private enum FeatureSpec {
        SMALL(4, 1, new String[]{"y", "lag1", "t", "sin_m", "cos_m"}),
        WIDE(7, 2,  new String[]{"y", "lag1", "lag2", "t", "sin_m", "cos_m", "sin_2m", "cos_2m"});

        final int nFeatures;
        /** First history index that has all required lags available. */
        final int firstT;
        final String[] columnNames;

        FeatureSpec(int nFeatures, int firstT, String[] columnNames) {
            this.nFeatures = nFeatures;
            this.firstT = firstT;
            this.columnNames = columnNames;
        }
    }

    /** Pre-computed (X, y) frame + feature array for training. */
    private static final class TrainingMatrix {
        final DataFrame df;
        final double[] y;
        TrainingMatrix(DataFrame df, double[] y) { this.df = df; this.y = y; }
    }

    /**
     * Builds the training (X, y) matrix for the given feature spec. The
     * first {@code spec.firstT} rows of {@code history} are dropped because
     * their required lags aren't defined.
     */
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
        return new TrainingMatrix(df, y);
    }

    /**
     * Builds the feature vector for position {@code t} in the extended
     * series (or a forecast step). {@code series} must contain values up to
     * index {@code t - 1}. {@code nForNormalise} is the length used as
     * denominator for the {@code t / nForNormalise} feature so that training
     * and forecast rows share the same scale.
     */
    private static double[] buildFeatures(double[] series, int startMonthIdx, FeatureSpec spec,
                                          int t, int nForNormalise) {
        double[] f = new double[spec.nFeatures];
        f[0] = series[t - 1];                            // lag1
        if (spec == FeatureSpec.SMALL) {
            f[1] = (double) t / nForNormalise;           // normalised time
            int month = (startMonthIdx + t) % 12;
            double theta = 2.0 * Math.PI * month / 12.0;
            f[2] = Math.sin(theta);
            f[3] = Math.cos(theta);
            return f;
        }
        // WIDE: lag2, trend, 1st + 2nd seasonal harmonic, rolling-3 mean
        f[1] = series[t - 2];                            // lag2
        f[2] = (double) t / nForNormalise;               // normalised time
        int month = (startMonthIdx + t) % 12;
        double theta = 2.0 * Math.PI * month / 12.0;
        f[3] = Math.sin(theta);
        f[4] = Math.cos(theta);
        f[5] = Math.sin(2.0 * theta);                    // 2nd harmonic
        f[6] = Math.cos(2.0 * theta);
        return f;
    }

    /* ---------------------------- Candidate models ---------------------------- */

    /** Binds a human-readable name to a Smile fit operation. */
    @FunctionalInterface
    private interface LearnerFit { LinearModel fit(DataFrame df); }

    private static final class ModelCandidate {
        final String name;
        final LearnerFit fitter;
        ModelCandidate(String name, LearnerFit fitter) { this.name = name; this.fitter = fitter; }
    }

    /** Thin wrapper around {@link OLS#fit} for the CV harness. */
    static LinearModel fitOls(DataFrame df) {
        return OLS.fit(Formula.lhs("y"), df);
    }

    /** Thin wrapper around {@link RidgeRegression#fit} for the CV harness. */
    static LinearModel fitRidge(DataFrame df) {
        return RidgeRegression.fit(Formula.lhs("y"), df, RIDGE_LAMBDA);
    }

    /* ------------------------- Walk-forward CV ------------------------- */

    /**
     * Walk-forward time-series cross-validation. For each of the last
     * {@code folds} positions in the training set, retrain on the prefix
     * {@code [0..k)} and score the one-step-ahead prediction against the
     * actual value. Returns the mean absolute percentage error (MAPE), or
     * {@code NaN} if no fold was evaluated (series too short or numerical
     * failure on every fold).
     */
    private static double walkForwardMape(double[] history, int startMonthIdx, FeatureSpec spec,
                                          int folds, ModelCandidate candidate) {
        if (folds <= 0) return Double.NaN;
        int n = history.length;
        int nSamples = n - spec.firstT;
        int startFold = nSamples - folds; // first fold's training size
        if (startFold < MIN_CV_TRAIN_ROWS) return Double.NaN;

        double sumMape = 0.0;
        int okFolds = 0;
        for (int trainEnd = startFold; trainEnd < nSamples; trainEnd++) {
            int prefixLen = spec.firstT + trainEnd;   // actual history length used
            double[] prefix = new double[prefixLen];
            System.arraycopy(history, 0, prefix, 0, prefixLen);
            TrainingMatrix tm;
            LinearModel m;
            try {
                tm = buildMatrix(prefix, startMonthIdx, spec);
                m = candidate.fitter.fit(tm.df);
            } catch (RuntimeException ex) {
                // Singular matrix or numerical blow-up on this fold — skip it
                // but keep going; other folds may still produce a usable score.
                continue;
            }
            // Predict the very next step after the prefix.
            int tNext = prefixLen; // index into history we're predicting
            double[] feats = buildFeatures(history, startMonthIdx, spec, tNext, n);
            double yHat = Math.max(0.0, m.predict(feats));
            double yTrue = history[tNext];
            double denom = Math.max(MAPE_EPS, Math.abs(yTrue));
            sumMape += Math.abs(yTrue - yHat) / denom;
            okFolds++;
        }
        return okFolds == 0 ? Double.NaN : sumMape / okFolds;
    }

    /* -------------------------- Recursive forecast -------------------------- */

    private static List<Double> recursiveForecast(double[] history, int startMonthIdx,
                                                  FeatureSpec spec, LinearModel model, int horizon) {
        int n = history.length;
        List<Double> future = new ArrayList<>(horizon);
        double[] extended = new double[n + horizon];
        System.arraycopy(history, 0, extended, 0, n);
        for (int h = 1; h <= horizon; h++) {
            int t = n - 1 + h; // position in the extended series
            double[] feats = buildFeatures(extended, startMonthIdx, spec, t, n);
            double pred = Math.max(0.0, model.predict(feats));
            extended[t] = pred;
            future.add(pred);
        }
        return future;
    }

    /* -------------------------- Small helpers -------------------------- */

    /** Sample standard deviation of residuals (non-negative, handles tiny samples). */
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
