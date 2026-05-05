package BuissnesManager.BuissnesManager.service.ml;

/**
 * Time-series error metrics used across the forecasting pipeline.
 *
 * <p>All methods return values in the original (back-transformed) space and
 * never throw — empty/short inputs yield {@link Double#NaN}, which the
 * selection logic treats as "metric not available".
 *
 * <p>The metric we trust most for cross-series benchmarking is
 * {@link #mase(double[], double[], int)}: a value below 1 means the model
 * beats the seasonal-naive baseline, which is the only honest threshold for
 * SME revenue series.
 */
public final class Metrics {

    /** Tiny absolute floor used to keep MAPE finite when actuals approach zero. */
    public static final double MAPE_EPS = 1.0;

    /**
     * Series-relative floor for the MAPE denominator: any single |actual_i|
     * smaller than {@code MAPE_REL_EPS * mean(|actual|)} is bumped up to that
     * fraction of the series scale.
     *
     * <p>Without this, a single near-zero observation in a revenue series can
     * inflate the per-point ratio by 4–5 orders of magnitude (e.g. forecast of
     * 50 000 EUR vs. an actual of 0 EUR ⇒ 50 000× the 1 EUR absolute floor),
     * producing meaningless cross-validated MAPE values like 996 504 %. A 1 %
     * series-scale floor caps the per-point worst case to a more interpretable
     * range while leaving healthy actuals (≥ 1 % of mean) completely
     * untouched.
     */
    public static final double MAPE_REL_EPS = 0.01;

    private Metrics() {}

    /** Mean Absolute Percentage Error in [0, +∞). NaN if no usable rows. */
    public static double mape(double[] actual, double[] forecast) {
        if (actual == null || forecast == null) return Double.NaN;
        int n = Math.min(actual.length, forecast.length);
        if (n == 0) return Double.NaN;
        double scaleFloor = MAPE_REL_EPS * meanAbs(actual);
        double sum = 0.0; int ok = 0;
        for (int i = 0; i < n; i++) {
            double denom = Math.max(MAPE_EPS,
                    Math.max(scaleFloor, Math.abs(actual[i])));
            sum += Math.abs(actual[i] - forecast[i]) / denom;
            ok++;
        }
        return ok == 0 ? Double.NaN : sum / ok;
    }

    /** Mean of |x| over a non-null array (0 if empty). */
    private static double meanAbs(double[] x) {
        if (x == null || x.length == 0) return 0.0;
        double s = 0.0;
        for (double v : x) s += Math.abs(v);
        return s / x.length;
    }

    /** Symmetric MAPE in [0, 2]. NaN if no usable rows. */
    public static double smape(double[] actual, double[] forecast) {
        if (actual == null || forecast == null) return Double.NaN;
        int n = Math.min(actual.length, forecast.length);
        if (n == 0) return Double.NaN;
        double sum = 0.0; int ok = 0;
        for (int i = 0; i < n; i++) {
            double denom = Math.max(MAPE_EPS,
                    (Math.abs(actual[i]) + Math.abs(forecast[i])) / 2.0);
            sum += Math.abs(actual[i] - forecast[i]) / denom;
            ok++;
        }
        return ok == 0 ? Double.NaN : sum / ok;
    }

    /** Root Mean Squared Error (original units). NaN if no usable rows. */
    public static double rmse(double[] actual, double[] forecast) {
        if (actual == null || forecast == null) return Double.NaN;
        int n = Math.min(actual.length, forecast.length);
        if (n == 0) return Double.NaN;
        double s = 0.0;
        for (int i = 0; i < n; i++) {
            double e = actual[i] - forecast[i];
            s += e * e;
        }
        return Math.sqrt(s / n);
    }

    /**
     * Mean Absolute Scaled Error (Hyndman &amp; Koehler).
     *
     * <p>Scale = mean absolute change of the seasonal-naive baseline on the
     * <i>training</i> portion (here we use {@code actual} as the in-sample
     * reference series). MASE&lt;1 means the candidate beats the seasonal-naive
     * forecast; 1.0 ties it; &gt;1 loses to it.
     *
     * @param actual    reference series (acts as both training scale and test target)
     * @param forecast  one-step-ahead forecast aligned with {@code actual}
     * @param period    seasonal period (12 for monthly yearly seasonality);
     *                  falls back to a random-walk scale (period=1) when the
     *                  series is shorter than {@code 2*period}.
     */
    public static double mase(double[] actual, double[] forecast, int period) {
        if (actual == null || forecast == null) return Double.NaN;
        int n = Math.min(actual.length, forecast.length);
        if (n == 0) return Double.NaN;
        int p = period > 0 ? period : 1;
        if (actual.length < 2 * p) p = 1;
        double scale = naiveScale(actual, p);
        if (!(scale > 0.0)) return Double.NaN;
        double sum = 0.0;
        for (int i = 0; i < n; i++) sum += Math.abs(actual[i] - forecast[i]);
        return (sum / n) / scale;
    }

    /** Mean absolute first-difference (or seasonal difference) of the series. */
    private static double naiveScale(double[] series, int period) {
        if (series == null || series.length <= period) return Double.NaN;
        double sum = 0.0; int n = 0;
        for (int i = period; i < series.length; i++) {
            sum += Math.abs(series[i] - series[i - period]);
            n++;
        }
        return n == 0 ? Double.NaN : sum / n;
    }
}
