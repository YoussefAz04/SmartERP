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














































@Component
public class MlForecastEngine {

    private static final Logger log = LoggerFactory.getLogger(MlForecastEngine.class);

    
    public static final int MIN_SAMPLES = 12;

    
    public static final int WIDE_MIN_SAMPLES = 36;

    
    public static final int RF_MIN_SAMPLES = 36;

    

    static final int BOXCOX_MIN_SAMPLES = 18;

    


    static final int MIN_ROWS_PER_FEATURE = 2;

    

    static final int CV_BLOCK_SIZE = 3;

    


    static final double OVERFIT_TRAIN_MAPE = 0.01;
    static final double OVERFIT_CV_MAPE    = 0.25;

    
    static final double RIDGE_LAMBDA = 0.5;
    


    static final double[] RIDGE_LAMBDA_GRID = { 0.1, 0.5, 1.5, 5.0 };
    
    static final double ELASTIC_LAMBDA1 = 0.1;
    
    static final double ELASTIC_LAMBDA2 = 0.3;
    

    static final double[][] ELASTIC_LAMBDA_GRID = {
            { 0.05, 0.15 },
            { 0.10, 0.30 },
            { 0.30, 0.60 },
    };

    
    private static final int MAX_CV_FOLDS = 4;

    
    private static final int MIN_CV_TRAIN_ROWS = 6;

    
    private static final int BOOTSTRAP_DRAWS = 200;

    
    private static final long BOOTSTRAP_SEED = 1337L;

    
    private static final long RF_SEED = 42L;

    
    public static final class MlForecastResult {
        
        public final List<Double> fittedHistorical;
        
        public final List<Double> futurePoint;
        
        public final List<Double> lowerBounds;
        
        public final List<Double> upperBounds;
        
        public final double residualStd;
        
        public final double rSquared;
        
        public final int trainingSamples;
        
        public final String modelName;
        
        public final int featureCount;
        
        public final double walkForwardMape;
        
        public final double walkForwardSmape;
        
        public final double walkForwardRmse;
        
        public final double walkForwardMase;
        
        public final int walkForwardFolds;
        
        public final double boxCoxLambda;
        
        public final String[] featureNames;
        
        public final double[] featureImportances;
        
        public final boolean importancesSigned;
        
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

    
    @Autowired(required = false)
    private CalendarFeatureService calendarFeatureService;

    
    public boolean canFit(int historyLength) {
        return historyLength >= MIN_SAMPLES;
    }

    



    public MlForecastResult forecast(double[] history, int startMonthIdx, int horizon) {
        return forecast(history, startMonthIdx, horizon, null, null);
    }

    





    public MlForecastResult forecast(double[] history, int startMonthIdx, int horizon,
                                     YearMonth firstMonth, Boolean disableBoxCox) {
        if (!canFit(history.length)) {
            throw new IllegalArgumentException(
                "history must have at least " + MIN_SAMPLES + " observations, got " + history.length);
        }
        if (horizon < 1) {
            throw new IllegalArgumentException("horizon must be >= 1");
        }

        
        BoxCox.Fit bc = (Boolean.TRUE.equals(disableBoxCox) || history.length < BOXCOX_MIN_SAMPLES)
                ? new BoxCox.Fit(1.0, 0.0)
                : BoxCox.fit(history);
        double[] tHistory = new double[history.length];
        for (int i = 0; i < history.length; i++) tHistory[i] = bc.forward(history[i]);

        
        boolean wide = history.length >= WIDE_MIN_SAMPLES;
        FeatureSpec spec = wide ? FeatureSpec.WIDE : FeatureSpec.SMALL;

        
        
        
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

        
        
        
        
        
        
        if (isRankDeficient(full)) {
            return naiveFlatForecast(history, horizon, full.featureCount,
                    full.featureNames, bc.lambda);
        }

        
        int pEff = full.featureCount;
        boolean underdetermined = isUnderdetermined(full.y.length, pEff);

        List<ModelCandidate> candidates = new ArrayList<>();
        if (underdetermined) {
            
            
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
            
            
            int blockSize = candidates.get(i).name.startsWith("RandomForest") ? CV_BLOCK_SIZE : 1;
            scores[i] = walkForwardScore(history, tHistory, startMonthIdx, spec,
                    calendarMatrix, cvFolds, candidates.get(i), bc, blockSize);
        }

        
        
        int best = 0, second = -1;
        double bestBlend = Double.NaN, secondBlend = Double.NaN;
        for (int i = 0; i < scores.length; i++) {
            double blend = blendedScore(scores[i]);
            
            
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
                } catch (RuntimeException ignored) {  }
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

        
        List<Double> fitted = new ArrayList<>(history.length);
        for (int t = 0; t < spec.firstT; t++) fitted.add(history[t]);
        double[] fittedTail = model.fittedValues();
        for (double v : fittedTail) fitted.add(bc.inverse(v));

        
        double residualStd = computeResidualStd(model.residuals());
        double rSquared = model.rSquared();

        
        List<Double> tFuture = recursiveForecast(tHistory, startMonthIdx, spec, model, horizon, calendarMatrix);
        List<Double> future = new ArrayList<>(horizon);
        for (double v : tFuture) future.add(bc.inverse(v));

        
        double[][] pi = bootstrapIntervals(tHistory, startMonthIdx, spec, model,
                model.residuals(), horizon, calendarMatrix);
        List<Double> lower = new ArrayList<>(horizon);
        List<Double> upper = new ArrayList<>(horizon);
        for (int h = 0; h < horizon; h++) {
            double lo = bc.inverse(pi[0][h]);
            double up = bc.inverse(pi[1][h]);
            
            if (lo > up) { double tmp = lo; lo = up; up = tmp; }
            lower.add(Math.max(0.0, lo));
            upper.add(Math.max(0.0, up));
        }

        
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

    



    private static double[] standardiseImportances(double[] raw, DataFrame df,
                                                   String[] columnNames, boolean signed) {
        if (raw == null) return null;
        double[] out = new double[raw.length];
        if (!signed) {
            
            for (int j = 0; j < raw.length; j++) out[j] = raw[j];
            return out;
        }
        
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
        
        
        double maseN = Double.isNaN(s.mase) ? 0.5 : Math.min(2.0, s.mase) / 2.0;
        return 0.4 * smape + 0.4 * mape + 0.2 * maseN;
    }

    

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

    




    private static TrainingMatrix buildMatrix(double[] tHistory, int startMonthIdx,
                                              FeatureSpec spec, double[][] calendarMatrix) {
        int n = tHistory.length;
        int nSamples = n - spec.firstT;
        int calCols = (calendarMatrix == null || calendarMatrix.length == 0) ? 0
                : calendarMatrix[0].length;
        int totalFeatures = spec.nFeatures + calCols;

        
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

    

    @FunctionalInterface
    private interface LearnerFit { Predictor fit(DataFrame df, StructType schema); }

    private static final class ModelCandidate {
        final String name;
        final LearnerFit fitter;
        

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

    

    private static boolean isUnderdetermined(int rows, int features) {
        return rows < MIN_ROWS_PER_FEATURE * features;
    }

    

    private static final double VARIANCE_EPSILON = 1e-12;

    














    private static boolean isRankDeficient(TrainingMatrix tm) {
        if (tm == null || tm.y == null || tm.y.length < 2) return true;
        if (variance(tm.y) <= VARIANCE_EPSILON) return true;
        int n = tm.y.length;
        
        
        for (int c = 1; c <= tm.featureCount; c++) {
            double[] col = new double[n];
            for (int i = 0; i < n; i++) {
                col[i] = tm.df.getDouble(i, c);
            }
            if (variance(col) <= VARIANCE_EPSILON) return true;
        }
        return false;
    }

    private static double variance(double[] v) {
        if (v == null || v.length == 0) return 0.0;
        double mean = 0.0;
        for (double x : v) mean += x;
        mean /= v.length;
        double s = 0.0;
        for (double x : v) {
            double d = x - mean;
            s += d * d;
        }
        return s / v.length;
    }

    





    private static MlForecastResult naiveFlatForecast(double[] history, int horizon,
                                                      int featureCount,
                                                      String[] featureNames,
                                                      double boxCoxLambda) {
        double mean = 0.0;
        for (double v : history) mean += v;
        mean /= Math.max(1, history.length);
        mean = Math.max(0.0, mean);

        List<Double> fitted = new ArrayList<>(history.length);
        for (int i = 0; i < history.length; i++) fitted.add(mean);
        List<Double> future = new ArrayList<>(horizon);
        List<Double> lower = new ArrayList<>(horizon);
        List<Double> upper = new ArrayList<>(horizon);
        for (int h = 0; h < horizon; h++) {
            future.add(mean);
            lower.add(mean);
            upper.add(mean);
        }
        log.debug("ML forecast: degenerate series — falling back to naive flat={} for {} months",
                mean, horizon);
        return new MlForecastResult(fitted, future, lower, upper, 0.0,
                0.0, history.length, "Naive(flat)", featureCount,
                Double.NaN, Double.NaN, Double.NaN, 0,
                Double.NaN, boxCoxLambda, featureNames, null,
                false, null);
    }

    

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
                
                
                
                
                if (candidate.needsFullRank
                        && (isUnderdetermined(tm.y.length, tm.featureCount)
                            || isRankDeficient(tm))) {
                    continue;
                }
                m = candidate.fit(tm.df, tm.schema);
            } catch (RuntimeException ex) {
                continue;
            }
            
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

    


    private static double[] paddedForMase(double[] history, double[] actuals, double[] forecasts) {
        double[] padded = history.clone();
        
        
        int k = Math.min(actuals.length, history.length);
        for (int i = 0; i < k; i++) {
            int idx = history.length - k + i;
            if (idx >= 0 && idx < padded.length) padded[idx] = forecasts[i];
        }
        return padded;
    }

    

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
