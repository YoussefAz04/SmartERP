package BuissnesManager.BuissnesManager.service.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.LinearModel;
import smile.regression.OLS;

import java.util.ArrayList;
import java.util.List;

/**
 * Supervised machine-learning forecast engine.
 *
 * <p>This replaces the hand-coded Holt-Winters smoothing for series long enough
 * to let a real model learn. A single {@link smile.regression.OLS ordinary
 * least squares} linear regression is fitted on engineered features derived
 * from the monthly sales history:
 *
 * <ul>
 *   <li><b>lag1</b> — last month's observation (captures short-term momentum)</li>
 *   <li><b>t</b> — normalised time index in [0, 1] (captures long-run trend)</li>
 *   <li><b>sin(2πm/12), cos(2πm/12)</b> — Fourier seasonal components where
 *       {@code m} is the calendar month index. These two features jointly
 *       encode the full yearly cycle with only 2 degrees of freedom, instead
 *       of the 11 month dummies a naive parameterisation would require —
 *       critical for small datasets where over-parameterising would make the
 *       normal equations singular.</li>
 * </ul>
 *
 * <p>The Fourier + lag-1 + trend design is a standard feature-engineering
 * recipe for short-ish monthly demand series. Using OLS keeps the model
 * interpretable (every coefficient has a clear meaning) while still being a
 * legitimate supervised-learning approach — training data is split into
 * (X, y) samples and the model minimises residual sum of squares to find its
 * parameters.
 *
 * <p>Forecasting is done recursively: to predict month h+1 we use the known
 * calendar month index and the lag-1 equal to the previous step's forecast.
 * Residual standard deviation from the in-sample fit is used as the base for
 * prediction intervals, widened by {@code sqrt(h)} on the forecast horizon —
 * the standard assumption when residuals are treated as approximately iid.
 */
@Component
public class MlForecastEngine {

    private static final Logger log = LoggerFactory.getLogger(MlForecastEngine.class);

    /**
     * Minimum observations required to fit the model. With 12 samples the
     * feature matrix (4 columns: lag1, t, sin, cos) still leaves 11 training
     * rows vs. 5 parameters (4 + intercept), comfortably over-determined.
     */
    public static final int MIN_SAMPLES = 12;

    /** Number of features (excluding the intercept). */
    private static final int N_FEATURES = 4; // lag1, t, sin(2πm/12), cos(2πm/12)

    /** Result bundle returned by {@link #forecast(double[], int, int)}. */
    public static final class MlForecastResult {
        public final List<Double> fittedHistorical; // length = history.length
        public final List<Double> futurePoint;      // length = horizon
        public final double residualStd;            // in-sample residual std-dev
        public final double rSquared;               // model fit score in [0, 1]
        public final int trainingSamples;           // rows used to fit

        MlForecastResult(List<Double> fittedHistorical, List<Double> futurePoint,
                         double residualStd, double rSquared, int trainingSamples) {
            this.fittedHistorical = fittedHistorical;
            this.futurePoint = futurePoint;
            this.residualStd = residualStd;
            this.rSquared = rSquared;
            this.trainingSamples = trainingSamples;
        }
    }

    /**
     * Returns {@code true} when the series is long enough to train a stable OLS
     * regression with the feature set used here. Callers should fall back to
     * simpler smoothing for shorter histories.
     *
     * @param historyLength total number of monthly observations available
     */
    public boolean canFit(int historyLength) {
        return historyLength >= MIN_SAMPLES;
    }

    /**
     * Fits the OLS model on {@code history} and produces in-sample fits and a
     * recursive multi-step-ahead forecast of length {@code horizon}.
     *
     * @param history     chronological monthly values (index 0 = {@code startMonthIdx}).
     *                    Missing months MUST be represented as zeros rather than
     *                    omitted, so calendar alignment is preserved.
     * @param startMonthIdx calendar month index (0-based; 0 = January) of
     *                      {@code history[0]}. Used to build the correct month
     *                      dummies.
     * @param horizon     number of future months to forecast (typically 6).
     * @return fitted values, forecast, residual std, R² and training sample count.
     * @throws IllegalArgumentException if {@code !canFit(history.length)}
     */
    public MlForecastResult forecast(double[] history, int startMonthIdx, int horizon) {
        if (!canFit(history.length)) {
            throw new IllegalArgumentException(
                "history must have at least " + MIN_SAMPLES + " observations, got " + history.length);
        }
        if (horizon < 1) {
            throw new IllegalArgumentException("horizon must be >= 1");
        }

        // Build (X, y) training set. We can only create a sample for index t
        // when lag1 = history[t-1] is available, so training starts at t = 1.
        int n = history.length;
        int firstT = 1;
        int nSamples = n - firstT;
        double[][] frame = new double[nSamples][N_FEATURES + 1]; // +1 for y column
        String[] columnNames = columnNames();

        for (int i = 0; i < nSamples; i++) {
            int t = firstT + i;
            double[] row = frame[i];
            row[0] = history[t];                     // y
            row[1] = history[t - 1];                 // lag1
            row[2] = ((double) t) / n;               // normalised time index in [0, 1]
            int month = (startMonthIdx + t) % 12;
            double theta = 2.0 * Math.PI * month / 12.0;
            row[3] = Math.sin(theta);                // seasonal sin
            row[4] = Math.cos(theta);                // seasonal cos
        }

        DataFrame df = DataFrame.of(frame, columnNames);
        LinearModel model = OLS.fit(Formula.lhs("y"), df);

        // In-sample fitted values. For the first observation we don't have
        // lag1, so we fall back to the observed value itself (residual = 0);
        // this keeps the aligned fitted series the same length as `history`
        // without biasing the residual std of the actually-trained window.
        List<Double> fitted = new ArrayList<>(n);
        for (int t = 0; t < firstT; t++) {
            fitted.add(history[t]);
        }
        double[] fittedTail = model.fittedValues();
        for (double v : fittedTail) {
            fitted.add(Math.max(0.0, v));
        }

        double residualStd = computeResidualStd(model.residuals());
        double rSquared = model.RSquared();

        // Recursive forecast. lag1 starts as the most recent actual; after each
        // step we advance it to the freshly-predicted value.
        List<Double> future = new ArrayList<>(horizon);
        List<Double> extended = new ArrayList<>(n + horizon);
        for (double v : history) extended.add(v);

        for (int h = 1; h <= horizon; h++) {
            int t = n - 1 + h; // position in the extended series
            double lag1 = extended.get(t - 1);
            double tNorm = ((double) t) / n;
            int month = (startMonthIdx + t) % 12;
            double theta = 2.0 * Math.PI * month / 12.0;
            double[] features = new double[N_FEATURES];
            features[0] = lag1;
            features[1] = tNorm;
            features[2] = Math.sin(theta);
            features[3] = Math.cos(theta);
            double pred = Math.max(0.0, model.predict(features));
            future.add(pred);
            extended.add(pred);
        }

        if (log.isDebugEnabled()) {
            log.debug("OLS forecast fitted on {} samples, R²={}, residualStd={}, horizon={}",
                    nSamples, rSquared, residualStd, horizon);
        }

        return new MlForecastResult(fitted, future, residualStd, rSquared, nSamples);
    }

    /** Column names matching the layout of {@link #forecast(double[], int, int)}. */
    private static String[] columnNames() {
        return new String[]{"y", "lag1", "t", "sin_m", "cos_m"};
    }

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
