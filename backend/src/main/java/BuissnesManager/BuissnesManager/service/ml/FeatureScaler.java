package BuissnesManager.BuissnesManager.service.ml;

/**
 * Small utility for standardising feature matrices before feeding them to
 * distance- or regularisation-sensitive learners (K-Means, Ridge, ElasticNet,
 * Logistic Regression, Isolation Forest …).
 *
 * <p>Two scaling modes are supported:
 * <ul>
 *   <li><b>Z-score</b> — subtract column mean, divide by sample standard
 *       deviation. Standard choice when the feature distribution is
 *       approximately symmetric / normal.</li>
 *   <li><b>Robust (median + MAD)</b> — subtract the column median and divide
 *       by its median absolute deviation scaled by 1.4826 (the robust-Gaussian
 *       consistency constant). Much less sensitive to outliers, which matters
 *       for business data where a handful of huge orders or zero-stock rows
 *       would otherwise dominate a z-score.</li>
 * </ul>
 *
 * <p>All scalers replace a zero or non-finite scale with {@code 1.0} so the
 * output is never {@code NaN}/{@code Infinity}. This keeps downstream Smile
 * learners happy — several of them throw on any non-finite feature.
 */
public final class FeatureScaler {

    private FeatureScaler() {}

    /** Mode selector for {@link #scale(double[][], Mode)}. */
    public enum Mode { Z_SCORE, ROBUST }

    /** Statistics captured per column during {@link #fit(double[][], Mode)}. */
    public static final class Stats {
        public final double[] center;
        public final double[] scale;
        public final Mode mode;
        public Stats(double[] center, double[] scale, Mode mode) {
            this.center = center;
            this.scale = scale;
            this.mode = mode;
        }
    }

    /** Standardises {@code x} in place and returns the matrix. */
    public static double[][] scale(double[][] x, Mode mode) {
        Stats s = fit(x, mode);
        return apply(x, s);
    }

    /** Computes per-column centring/scaling statistics without mutating {@code x}. */
    public static Stats fit(double[][] x, Mode mode) {
        int n = x.length;
        if (n == 0) return new Stats(new double[0], new double[0], mode);
        int p = x[0].length;
        double[] center = new double[p];
        double[] scale = new double[p];
        double[] col = new double[n];
        for (int j = 0; j < p; j++) {
            for (int i = 0; i < n; i++) col[i] = x[i][j];
            if (mode == Mode.Z_SCORE) {
                double[] ms = meanStd(col);
                center[j] = ms[0];
                scale[j] = ms[1] > 0.0 && Double.isFinite(ms[1]) ? ms[1] : 1.0;
            } else {
                double med = median(col.clone());
                double[] dev = new double[n];
                for (int i = 0; i < n; i++) dev[i] = Math.abs(col[i] - med);
                double mad = median(dev) * 1.4826;
                center[j] = med;
                scale[j] = mad > 0.0 && Double.isFinite(mad) ? mad : 1.0;
            }
        }
        return new Stats(center, scale, mode);
    }

    /** Applies previously-fitted statistics to a new matrix (returns new array). */
    public static double[][] apply(double[][] x, Stats s) {
        int n = x.length;
        if (n == 0) return x;
        int p = x[0].length;
        double[][] out = new double[n][p];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                double v = (x[i][j] - s.center[j]) / s.scale[j];
                out[i][j] = Double.isFinite(v) ? v : 0.0;
            }
        }
        return out;
    }

    /** Returns [mean, sample stdDev] for a non-empty array; safe on length &lt; 2. */
    public static double[] meanStd(double[] v) {
        if (v == null || v.length == 0) return new double[]{0.0, 0.0};
        double sum = 0.0;
        for (double x : v) sum += x;
        double mean = sum / v.length;
        if (v.length < 2) return new double[]{mean, 0.0};
        double sq = 0.0;
        for (double x : v) { double d = x - mean; sq += d * d; }
        return new double[]{mean, Math.sqrt(sq / (v.length - 1))};
    }

    /** Median of {@code v}; mutates the input array (sorts it in place). */
    public static double median(double[] v) {
        if (v == null || v.length == 0) return 0.0;
        double[] copy = v.clone();
        java.util.Arrays.sort(copy);
        int n = copy.length;
        return n % 2 == 1 ? copy[n / 2] : 0.5 * (copy[n / 2 - 1] + copy[n / 2]);
    }
}
