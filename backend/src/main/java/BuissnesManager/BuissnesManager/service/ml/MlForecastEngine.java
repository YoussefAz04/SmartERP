package BuissnesManager.BuissnesManager.service.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.type.StructType;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    public static final int WIDE_MIN_SAMPLES = 36;

    /** Minimum observations required to enable the RandomForest candidate. */
    public static final int RF_MIN_SAMPLES = 36;

    /** Minimum observations required to apply a Box-Cox stabilising transform.
     *  Below this we fall back to identity (λ=1). */
    static final int BOXCOX_MIN_SAMPLES = 18;

    /** Minimum number of training rows per feature for OLS / RF / EN to be
     *  considered safe (n ≥ 2·p). Below this threshold we restrict the
     *  candidate set to OLS-with-shrinkage (Ridge at the largest λ). */
    static final int MIN_ROWS_PER_FEATURE = 2;

    /** Block size (months) for blocked walk-forward CV — used for RandomForest
     *  so the OOS error reflects multi-step degradation, not 1-step lookups. */
    static final int CV_BLOCK_SIZE = 3;

    /** RandomForest overfit gate: training MAPE below this AND CV MAPE above
     *  the OVERFIT_CV threshold is treated as memorisation and the candidate
     *  is excluded from selection. */
    static final double OVERFIT_TRAIN_MAPE = 0.01;
    static final double OVERFIT_CV_MAPE    = 0.25;

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
        /** Walk-forward MASE — &lt;1 means we beat the seasonal-naive baseline. */
        public final double walkForwardMase;
        /** Number of folds actually evaluated. */
        public final int walkForwardFolds;
        /** Box-Cox λ chosen by profile-likelihood (NaN when transform was disabled). */
        public final double boxCoxLambda;
        /** Engineered feature column names, in the order used at fit time. */
        public final String[] featureNames;
        /** Standardised feature contributions (signed for linear, unsigned for RF). */
        public final double[] featureImportances;
        /** Whether {@link #featureImportances} are signed coefficients. */
        public final boolean importancesSigned;
        /** Short name of the runner-up model (for A/B persistence). May be null. */
        public final String challengerName;

        MlForecastResult(List<Double> fittedHistorical, List<Double> futurePoint,
                         List<Double> lowerBounds, List<Double> upperBounds,
                         double residualStd, double rSquared, int trainingSamples,
                         String modelName, int featureCount,
                         double walkForwardMape, double walkForwardSmape,
                         double walkForwardRmse, int walkForwardFolds,
                         double walkForwardMase, double boxCoxLambda,
                         String[] featureNames, double[] featureImportances,
                         boolean importancesSigned, String challengerName) {
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
            this.walkForwardMase = walkForwardMase;
            this.boxCoxLambda = boxCoxLambda;
            this.featureNames = featureNames;
            this.featureImportances = featureImportances;
            this.importancesSigned = importancesSigned;
            this.challengerName = challengerName;
        }
    }

    /** Optional injection — null in unit tests so old call sites still work. */
    @Autowired(required = false)
    private CalendarFeatureService calendarFeatureService;

    /** Returns {@code true} when the series is long enough to train a stable regression. */
    public boolean canFit(int historyLength) {
        return historyLength >= MIN_SAMPLES;
    }

    /**
     * Backwards-compatible entry point — no calendar features, no Box-Cox
     * (lambda is still computed but the surrounding pipeline can disable it).
     */
    public MlForecastResult forecast(double[] history, int startMonthIdx, int horizon) {
        return forecast(history, startMonthIdx, horizon, null, null);
    }

    /**
     * Calendar-aware overload. {@code firstMonth} is the calendar month of
     * {@code history[0]}; when supplied we can compute Ramadan / working-days /
     * end-of-quarter features deterministically for both the in-sample and
     * the future horizon. {@code disableBoxCox} forces λ=1 (identity).
     */
    public MlForecastResult forecast(double[] history, int startMonthIdx, int horizon,
                                     YearMonth firstMonth, Boolean disableBoxCox) {
        if (!canFit(history.length)) {
            throw new IllegalArgumentException(
                "history must have at least " + MIN_SAMPLES + " observations, got " + history.length);
        }
        if (horizon < 1) {
            throw new IllegalArgumentException("horizon must be >= 1");
        }

        // ----- 1. Box-Cox: stabilise multiplicative variance before fitting. -----
        BoxCox.Fit bc = (Boolean.TRUE.equals(disableBoxCox) || history.length < BOXCOX_MIN_SAMPLES)
                ? new BoxCox.Fit(1.0, 0.0)
                : BoxCox.fit(history);
        double[] tHistory = new double[history.length];
        for (int i = 0; i < history.length; i++) tHistory[i] = bc.forward(history[i]);

        // ----- 2. Choose the feature spec, honouring n ≫ p. -----
        boolean wide = history.length >= WIDE_MIN_SAMPLES;
        FeatureSpec spec = wide ? FeatureSpec.WIDE : FeatureSpec.SMALL;

        // Calendar exogenous columns are appended only when a service AND a
        // start month are supplied. They are *known at forecast time* so
        // future horizons consume the same feature definition.
        double[][] calendarMatrix = null;
        String[] calendarNames = new String[0];
        if (calendarFeatureService != null && firstMonth != null) {
            calendarNames = CalendarFeatureService.FEATURE_NAMES;
            calendarMatrix = new double[history.length + horizon][calendarNames.length];
            for (int i = 0; i < calendarMatrix.length; i++) {
                YearMonth ym = firstMonth.plusMonths(i);
                calendarMatrix[i] = calendarFeatureService.features(ym, null);
            }
        }

        TrainingMatrix full = buildMatrix(tHistory, startMonthIdx, spec, calendarMatrix);

        // n ≫ p hard guard: if rows < 2·p, force a single shrinkage candidate.
        int pEff = full.featureCount;
        boolean underdetermined = isUnderdetermined(full.y.length, pEff);

        List<ModelCandidate> candidates = new ArrayList<>();
        if (underdetermined) {
            // Always-fits Ridge with the strongest λ on the grid — never trains
            // OLS / EN / RF when we can't safely do so.
            double lambda = RIDGE_LAMBDA_GRID[RIDGE_LAMBDA_GRID.length - 1];
            candidates.add(new ModelCandidate(
                    String.format(Locale.ROOT, "Ridge(λ=%.2f)", lambda),
                    (df, schema) -> Predictor.fitRidge(df, schema, lambda)));
        } else {
            candidates.add(new ModelCandidate("OLS",
                    (df, schema) -> Predictor.fitOls(df, schema), true));
            for (double lambda : RIDGE_LAMBDA_GRID) {
                candidates.add(new ModelCandidate(
                        String.format(Locale.ROOT, "Ridge(λ=%.2f)", lambda),
                        (df, schema) -> Predictor.fitRidge(df, schema, lambda)));
            }
            for (double[] lp : ELASTIC_LAMBDA_GRID) {
                final double l1 = lp[0];
                final double l2 = lp[1];
                candidates.add(new ModelCandidate(
                        String.format(Locale.ROOT, "ElasticNet(λ1=%.2f,λ2=%.2f)", l1, l2),
                        (df, schema) -> Predictor.fitElasticNet(df, schema, l1, l2)));
            }
            if (history.length >= RF_MIN_SAMPLES) {
                int mtry = Math.max(1, spec.nFeatures / 2);
                candidates.add(new ModelCandidate("RandomForest",
                        (df, schema) -> Predictor.fitRandomForest(df, schema, 100, 6, mtry, RF_SEED)));
            }
        }

        int nSamples = full.y.length;
        int cvFolds = Math.min(MAX_CV_FOLDS, Math.max(0, nSamples - MIN_CV_TRAIN_ROWS));

        CvScore[] scores = new CvScore[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            // RandomForest: use blocked walk-forward CV to expose multi-step
            // degradation; everything else uses single-step folds.
            int blockSize = candidates.get(i).name.startsWith("RandomForest") ? CV_BLOCK_SIZE : 1;
            scores[i] = walkForwardScore(history, tHistory, startMonthIdx, spec,
                    calendarMatrix, cvFolds, candidates.get(i), bc, blockSize);
        }

        // ----- 3. Selection + overfit gate. -----
        // Track best AND second-best (for A/B / challenger persistence).
        int best = 0, second = -1;
        double bestBlend = Double.NaN, secondBlend = Double.NaN;
        for (int i = 0; i < scores.length; i++) {
            double blend = blendedScore(scores[i]);
            // Overfit gate (RandomForest only): if it nails training MAPE but
            // CV MAPE is poor, it's memorising — score it out.
            if (candidates.get(i).name.startsWith("RandomForest")
                    && Double.isFinite(blend)
                    && scores[i].mape > OVERFIT_CV_MAPE) {
                try {
                    Predictor probe = candidates.get(i).fit(full.df, full.schema);
                    double trainMape = inSampleMape(probe, full, bc);
                    if (Double.isFinite(trainMape) && trainMape < OVERFIT_TRAIN_MAPE) {
                        blend = Double.POSITIVE_INFINITY;
                        log.debug("RandomForest excluded by overfit gate: train MAPE={}, CV MAPE={}",
                                trainMape, scores[i].mape);
                    }
                } catch (RuntimeException ignored) { /* probe failure → keep blend */ }
            }
            if (Double.isNaN(bestBlend) || (!Double.isNaN(blend) && blend < bestBlend)) {
                second = best; secondBlend = bestBlend;
                best = i; bestBlend = blend;
            } else if (Double.isNaN(secondBlend) || (!Double.isNaN(blend) && blend < secondBlend)) {
                second = i; secondBlend = blend;
            }
        }

        Predictor model = candidates.get(best).fit(full.df, full.schema);
        String challengerName = (second >= 0 && second != best) ? candidates.get(second).name : null;

        // ----- 4. In-sample fitted values, back-transformed to original scale. -----
        List<Double> fitted = new ArrayList<>(history.length);
        for (int t = 0; t < spec.firstT; t++) fitted.add(history[t]);
        double[] fittedTail = model.fittedValues();
        for (double v : fittedTail) fitted.add(bc.inverse(v));

        // Residual std in transformed space (used for the bootstrap noise).
        double residualStd = computeResidualStd(model.residuals());
        double rSquared = model.rSquared();

        // ----- 5. Recursive forecast (in transformed space, then inverse). -----
        List<Double> tFuture = recursiveForecast(tHistory, startMonthIdx, spec, model, horizon, calendarMatrix);
        List<Double> future = new ArrayList<>(horizon);
        for (double v : tFuture) future.add(bc.inverse(v));

        // ----- 6. Bootstrap PIs in transformed space, then inverse. -----
        double[][] pi = bootstrapIntervals(tHistory, startMonthIdx, spec, model,
                model.residuals(), horizon, calendarMatrix);
        List<Double> lower = new ArrayList<>(horizon);
        List<Double> upper = new ArrayList<>(horizon);
        for (int h = 0; h < horizon; h++) {
            double lo = bc.inverse(pi[0][h]);
            double up = bc.inverse(pi[1][h]);
            // Box-Cox can flip the order around λ < 0; ensure lo ≤ up.
            if (lo > up) { double tmp = lo; lo = up; up = tmp; }
            lower.add(Math.max(0.0, lo));
            upper.add(Math.max(0.0, up));
        }

        // ----- 7. Feature importances. -----
        double[] rawImp = model.rawImportances();
        double[] standardised = standardiseImportances(rawImp, full.df, full.featureNames,
                model.importancesSigned());

        CvScore bestScore = scores[best];
        if (log.isDebugEnabled()) {
            log.debug("ML forecast: picked {} (challenger={}, features={}, samples={}, " +
                    "cvFolds={}, OOS-MAPE={}, OOS-sMAPE={}, OOS-RMSE={}, MASE={}, R²={}, λ={})",
                    candidates.get(best).name, challengerName, spec.nFeatures, nSamples, cvFolds,
                    bestScore.mape, bestScore.smape, bestScore.rmse, bestScore.mase,
                    rSquared, bc.lambda);
        }

        return new MlForecastResult(fitted, future, lower, upper, residualStd,
                rSquared, nSamples, candidates.get(best).name, full.featureCount,
                bestScore.mape, bestScore.smape, bestScore.rmse, cvFolds,
                bestScore.mase, bc.lambda, full.featureNames, standardised,
                model.importancesSigned(), challengerName);
    }

    /**
     * Standardise linear coefficients (β·σ_x) so they are comparable across
     * features regardless of unit. Tree-importance arrays pass through unchanged.
     */
    private static double[] standardiseImportances(double[] raw, DataFrame df,
                                                   String[] columnNames, boolean signed) {
        if (raw == null) return null;
        double[] out = new double[raw.length];
        if (!signed) {
            // Unsigned tree importances — keep as-is.
            for (int j = 0; j < raw.length; j++) out[j] = raw[j];
            return out;
        }
        // For each feature column j (offset +1 to skip the y column), compute σ_x.
        for (int j = 0; j < raw.length; j++) {
            String col = (j + 1 < columnNames.length) ? columnNames[j + 1] : null;
            double sigma = (col == null) ? 1.0 : columnStd(df, col);
            out[j] = raw[j] * sigma;
        }
        return out;
    }

    private static double columnStd(DataFrame df, String col) {
        try {
            int n = df.size();
            if (n < 2) return 0.0;
            double mean = 0.0;
            double[] v = new double[n];
            for (int i = 0; i < n; i++) { v[i] = df.get(i).getDouble(col); mean += v[i]; }
            mean /= n;
            double s2 = 0.0;
            for (double x : v) { double d = x - mean; s2 += d * d; }
            return Math.sqrt(s2 / (n - 1));
        } catch (RuntimeException ex) {
            return 0.0;
        }
    }

    /** Compute in-sample MAPE in the original (back-transformed) space. */
    private static double inSampleMape(Predictor p, TrainingMatrix m, BoxCox.Fit bc) {
        double[] yHat = p.fittedValues();
        if (yHat == null) return Double.NaN;
        double[] actual = new double[yHat.length];
        double[] forecast = new double[yHat.length];
        for (int i = 0; i < yHat.length; i++) {
            actual[i] = bc.inverse(m.y[i]);
            forecast[i] = bc.inverse(yHat[i]);
        }
        return Metrics.mape(actual, forecast);
    }

    private static double blendedScore(CvScore s) {
        if (Double.isNaN(s.mape) && Double.isNaN(s.smape)) return Double.NaN;
        double mape  = Double.isNaN(s.mape)  ? 1.0 : Math.min(1.0, s.mape);
        double smape = Double.isNaN(s.smape) ? 1.0 : s.smape;
        // MASE component: <1 means we beat seasonal-naive; we cap at 2 and
        // normalise to [0, 1] for blending.
        double maseN = Double.isNaN(s.mase) ? 0.5 : Math.min(2.0, s.mase) / 2.0;
        return 0.4 * smape + 0.4 * mape + 0.2 * maseN;
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
        final String[] featureNames;
        final int featureCount;
        TrainingMatrix(DataFrame df, StructType schema, double[] y,
                       String[] featureNames, int featureCount) {
            this.df = df;
            this.schema = schema;
            this.y = y;
            this.featureNames = featureNames;
            this.featureCount = featureCount;
        }
    }

    /**
     * Build a (X, y) matrix over the transformed history. {@code calendarMatrix}
     * is null-safe — when supplied its rows for indices ≥ spec.firstT are
     * appended as additional exogenous columns.
     */
    private static TrainingMatrix buildMatrix(double[] tHistory, int startMonthIdx,
                                              FeatureSpec spec, double[][] calendarMatrix) {
        int n = tHistory.length;
        int nSamples = n - spec.firstT;
        int calCols = (calendarMatrix == null || calendarMatrix.length == 0) ? 0
                : calendarMatrix[0].length;
        int totalFeatures = spec.nFeatures + calCols;

        // Build column names: ["y", spec features..., calendar features...].
        String[] columnNames = new String[1 + totalFeatures];
        System.arraycopy(spec.columnNames, 0, columnNames, 0, spec.columnNames.length);
        if (calCols > 0) {
            for (int i = 0; i < calCols; i++) {
                columnNames[spec.columnNames.length + i] =
                        i < CalendarFeatureService.FEATURE_NAMES.length
                                ? CalendarFeatureService.FEATURE_NAMES[i]
                                : "exog_" + i;
            }
        }

        double[][] frame = new double[nSamples][totalFeatures + 1];
        double[] y = new double[nSamples];
        for (int i = 0; i < nSamples; i++) {
            int t = spec.firstT + i;
            double[] features = buildFeatures(tHistory, startMonthIdx, spec, t, n,
                    calendarMatrix, calCols);
            frame[i][0] = tHistory[t];
            y[i] = tHistory[t];
            System.arraycopy(features, 0, frame[i], 1, totalFeatures);
        }
        DataFrame df = DataFrame.of(frame, columnNames);
        return new TrainingMatrix(df, df.schema(), y, columnNames, totalFeatures);
    }

    private static double[] buildFeatures(double[] series, int startMonthIdx, FeatureSpec spec,
                                          int t, int nForNormalise,
                                          double[][] calendarMatrix, int calCols) {
        double[] f = new double[spec.nFeatures + calCols];
        f[0] = series[t - 1];
        if (spec == FeatureSpec.SMALL) {
            f[1] = (double) t / nForNormalise;
            int month = (startMonthIdx + t) % 12;
            double theta = 2.0 * Math.PI * month / 12.0;
            f[2] = Math.sin(theta);
            f[3] = Math.cos(theta);
        } else {
            f[1] = series[t - 2];
            f[2] = (double) t / nForNormalise;
            int month = (startMonthIdx + t) % 12;
            double theta = 2.0 * Math.PI * month / 12.0;
            f[3] = Math.sin(theta);
            f[4] = Math.cos(theta);
            f[5] = Math.sin(2.0 * theta);
            f[6] = Math.cos(2.0 * theta);
        }
        if (calCols > 0 && calendarMatrix != null && t < calendarMatrix.length) {
            System.arraycopy(calendarMatrix[t], 0, f, spec.nFeatures, calCols);
        }
        return f;
    }

    /* ---------------------------- Candidate models ---------------------------- */

    @FunctionalInterface
    private interface LearnerFit { Predictor fit(DataFrame df, StructType schema); }

    private static final class ModelCandidate {
        final String name;
        final LearnerFit fitter;
        /** True when the underlying learner requires a full-rank design matrix
         *  (i.e. plain OLS). Ridge / ElasticNet / RF are always safe to call. */
        final boolean needsFullRank;
        ModelCandidate(String name, LearnerFit fitter) {
            this(name, fitter, false);
        }
        ModelCandidate(String name, LearnerFit fitter, boolean needsFullRank) {
            this.name = name;
            this.fitter = fitter;
            this.needsFullRank = needsFullRank;
        }
        Predictor fit(DataFrame df, StructType schema) { return fitter.fit(df, schema); }
    }

    /** Returns true when a training subset with {@code rows} samples and
     *  {@code features} predictors is under-determined for plain OLS. */
    private static boolean isUnderdetermined(int rows, int features) {
        return rows < MIN_ROWS_PER_FEATURE * features;
    }

    /* ------------------------- Walk-forward CV ------------------------- */

    private static final class CvScore {
        final double mape;
        final double smape;
        final double rmse;
        final double mase;
        CvScore(double mape, double smape, double rmse, double mase) {
            this.mape = mape;
            this.smape = smape;
            this.rmse = rmse;
            this.mase = mase;
        }
        static final CvScore NAN = new CvScore(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    /**
     * Walk-forward CV scoring over the back-transformed predictions.
     *
     * <p>{@code blockSize} = 1 yields the classic single-step rolling fold;
     * {@code blockSize} > 1 trains once per block and predicts the next
     * {@code blockSize} months, exposing genuine multi-step degradation
     * (used for RandomForest where lag-1 lookups are too forgiving).
     *
     * <p>All comparisons use the original (back-transformed) scale so the
     * metrics are interpretable to users.
     */
    private static CvScore walkForwardScore(double[] history, double[] tHistory,
                                            int startMonthIdx, FeatureSpec spec,
                                            double[][] calendarMatrix, int folds,
                                            ModelCandidate candidate, BoxCox.Fit bc,
                                            int blockSize) {
        if (folds <= 0) return CvScore.NAN;
        int nSamples = tHistory.length - spec.firstT;
        int startFold = nSamples - folds;
        if (startFold < MIN_CV_TRAIN_ROWS) return CvScore.NAN;
        int calCols = (calendarMatrix == null || calendarMatrix.length == 0) ? 0
                : calendarMatrix[0].length;

        List<Double> actuals = new ArrayList<>();
        List<Double> forecasts = new ArrayList<>();
        for (int trainEnd = startFold; trainEnd < nSamples; trainEnd += blockSize) {
            int prefixLen = spec.firstT + trainEnd;
            double[] tPrefix = new double[prefixLen];
            System.arraycopy(tHistory, 0, tPrefix, 0, prefixLen);
            TrainingMatrix tm;
            Predictor m;
            try {
                tm = buildMatrix(tPrefix, startMonthIdx, spec, calendarMatrix);
                // Guard: skip full-rank-only learners (OLS) when this fold's
                // training subset is under-determined. Ridge/EN/RF handle it gracefully;
                // OLS would produce a rank-deficient matrix and log LAPACK errors.
                if (candidate.needsFullRank && isUnderdetermined(tm.y.length, tm.featureCount)) {
                    continue;
                }
                m = candidate.fit(tm.df, tm.schema);
            } catch (RuntimeException ex) {
                continue;
            }
            // Predict the next `blockSize` months recursively in transformed space.
            int reach = Math.min(blockSize, nSamples - trainEnd);
            double[] tExtended = Arrays.copyOf(tPrefix, prefixLen + reach);
            for (int h = 1; h <= reach; h++) {
                int tNext = prefixLen + h - 1;
                double[] feats = buildFeatures(tExtended, startMonthIdx, spec, tNext, prefixLen,
                        calendarMatrix, calCols);
                double tPred;
                try {
                    tPred = m.predict(feats);
                } catch (RuntimeException ex) {
                    tPred = tExtended[tNext - 1];
                }
                tExtended[tNext] = tPred;
                int origIdx = tNext;
                actuals.add(history[origIdx]);
                forecasts.add(Math.max(0.0, bc.inverse(tPred)));
            }
        }
        if (actuals.isEmpty()) return CvScore.NAN;
        double[] a = actuals.stream().mapToDouble(Double::doubleValue).toArray();
        double[] f = forecasts.stream().mapToDouble(Double::doubleValue).toArray();
        return new CvScore(
                Metrics.mape(a, f),
                Metrics.smape(a, f),
                Metrics.rmse(a, f),
                Metrics.mase(history, paddedForMase(history, a, f), 12));
    }

    /** Build a forecast array aligned with the {@code history} indices used
     *  by MASE — predictions slot into their original positions, the rest
     *  are filled with the actual value (zero error). */
    private static double[] paddedForMase(double[] history, double[] actuals, double[] forecasts) {
        double[] padded = history.clone();
        // We don't know exactly which indices were predicted, but for MASE
        // we just need the SAME-length arrays; the scale denominator dominates.
        int k = Math.min(actuals.length, history.length);
        for (int i = 0; i < k; i++) {
            int idx = history.length - k + i;
            if (idx >= 0 && idx < padded.length) padded[idx] = forecasts[i];
        }
        return padded;
    }

    /* -------------------------- Recursive forecast -------------------------- */

    private static List<Double> recursiveForecast(double[] tHistory, int startMonthIdx,
                                                  FeatureSpec spec, Predictor model, int horizon,
                                                  double[][] calendarMatrix) {
        int n = tHistory.length;
        int calCols = (calendarMatrix == null || calendarMatrix.length == 0) ? 0
                : calendarMatrix[0].length;
        List<Double> future = new ArrayList<>(horizon);
        double[] extended = new double[n + horizon];
        System.arraycopy(tHistory, 0, extended, 0, n);
        for (int h = 1; h <= horizon; h++) {
            int t = n - 1 + h;
            double[] feats = buildFeatures(extended, startMonthIdx, spec, t, n,
                    calendarMatrix, calCols);
            double pred;
            try {
                pred = model.predict(feats);
            } catch (RuntimeException ex) {
                pred = extended[t - 1];
            }
            extended[t] = pred;
            future.add(pred);
        }
        return future;
    }

    /**
     * Bootstrap prediction intervals in transformed space. The caller is
     * responsible for inverse-transforming the percentile rows.
     *
     * <p>Returned shape: {@code [2][horizon]} with row 0 = lower, row 1 = upper.
     */
    private static double[][] bootstrapIntervals(double[] tHistory, int startMonthIdx,
                                                 FeatureSpec spec, Predictor model,
                                                 double[] residuals, int horizon,
                                                 double[][] calendarMatrix) {
        int calCols = (calendarMatrix == null || calendarMatrix.length == 0) ? 0
                : calendarMatrix[0].length;
        double[][] draws = new double[BOOTSTRAP_DRAWS][horizon];
        Random rng = new Random(BOOTSTRAP_SEED);
        int n = tHistory.length;
        int r = residuals == null ? 0 : residuals.length;
        // Center residuals on zero before resampling. Raw in-sample residuals
        // can have a non-zero mean on small samples (especially for ridge /
        // elastic net which shrink toward zero) — sampling those directly
        // shifts the prediction interval up or down.
        double[] centered;
        if (r > 0) {
            double mean = 0.0;
            for (double v : residuals) mean += v;
            mean /= r;
            centered = new double[r];
            for (int i = 0; i < r; i++) centered[i] = residuals[i] - mean;
        } else {
            centered = new double[0];
        }
        for (int b = 0; b < BOOTSTRAP_DRAWS; b++) {
            double[] extended = new double[n + horizon];
            System.arraycopy(tHistory, 0, extended, 0, n);
            for (int h = 1; h <= horizon; h++) {
                int t = n - 1 + h;
                double[] feats = buildFeatures(extended, startMonthIdx, spec, t, n,
                        calendarMatrix, calCols);
                double pred;
                try { pred = model.predict(feats); }
                catch (RuntimeException ex) { pred = extended[t - 1]; }
                double noise = r > 0 ? centered[rng.nextInt(r)] : 0.0;
                double noisy = pred + noise;
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
