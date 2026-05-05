package BuissnesManager.BuissnesManager.service.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Robust outlier detection / winsorisation for monthly revenue series.
 *
 * <p>A single mis-keyed invoice (e.g. 1 000 000 TND instead of 1 000) destroys
 * both the trend and the seasonal indices of an otherwise well-behaved series.
 * The Hampel filter — a rolling-window variant of the median ± k·MAD rule —
 * is the standard fix: it's robust to up to 50 % contamination and degrades
 * gracefully when the series is too short.
 *
 * <p>The cleaner operates in <b>log-space</b> so a 10× spike is detected
 * irrespective of the level of the series. Months whose value is &lt;= 0 are
 * skipped (they're handled by the missing-vs-zero path elsewhere).
 */
public final class SeriesCleaner {

    /** Rolling window (in months) for the local median / MAD. */
    public static final int DEFAULT_WINDOW = 12;
    /** Hampel threshold in MAD units. 3.5 ≈ 99.95 % under normality. */
    public static final double DEFAULT_K = 3.5;
    /** Constant turning MAD into a σ estimator under normality. */
    private static final double MAD_TO_SIGMA = 1.4826;

    /** Detected anomaly bundle returned by {@link #clean(double[])}. */
    public static final class Anomaly {
        public final int index;
        public final double original;
        public final double replacement;
        public final double zScore;
        Anomaly(int index, double original, double replacement, double zScore) {
            this.index = index;
            this.original = original;
            this.replacement = replacement;
            this.zScore = zScore;
        }
    }

    /** Result bundle: cleaned series + list of detected anomalies. */
    public static final class Result {
        public final double[] cleaned;
        public final List<Anomaly> anomalies;
        Result(double[] cleaned, List<Anomaly> anomalies) {
            this.cleaned = cleaned;
            this.anomalies = anomalies;
        }
    }

    /** Outlier-handling policy controlled by {@code forecast.outlier.policy}. */
    public enum Policy {
        /** Replace outliers with the rolling median (default). */
        WINSORIZE,
        /** Mark outliers as missing (caller imputes). */
        REJECT,
        /** Pass-through: detect but don't replace. */
        RAW;

        public static Policy parse(String s) {
            if (s == null) return WINSORIZE;
            try { return Policy.valueOf(s.trim().toUpperCase(java.util.Locale.ROOT)); }
            catch (IllegalArgumentException ex) { return WINSORIZE; }
        }
    }

    private SeriesCleaner() {}

    /** Convenience: WINSORIZE with default window/threshold. */
    public static Result clean(double[] series) {
        return clean(series, Policy.WINSORIZE, DEFAULT_WINDOW, DEFAULT_K);
    }

    /**
     * Hampel filter on the log-transformed series.
     *
     * @param series   raw monthly values; null or short series are returned as-is
     * @param policy   {@link Policy#WINSORIZE} (replace), {@link Policy#REJECT}
     *                 (set to NaN so caller can impute), or {@link Policy#RAW}
     *                 (just detect)
     * @param window   rolling window size in months
     * @param k        threshold in MAD units
     */
    public static Result clean(double[] series, Policy policy, int window, double k) {
        if (series == null || series.length < 6) {
            return new Result(series == null ? null : series.clone(), Collections.emptyList());
        }
        double[] out = series.clone();
        List<Anomaly> anomalies = new ArrayList<>();
        int half = Math.max(2, window / 2);

        for (int i = 0; i < out.length; i++) {
            int lo = Math.max(0, i - half);
            int hi = Math.min(out.length, i + half + 1);
            // Build a window of strictly-positive observations so log() stays valid
            // and zero months don't dominate the median.
            List<Double> w = new ArrayList<>(hi - lo);
            for (int j = lo; j < hi; j++) {
                if (out[j] > 0) w.add(Math.log(out[j]));
            }
            if (w.size() < 5) continue; // not enough context to flag anything
            double median = median(w);
            List<Double> dev = new ArrayList<>(w.size());
            for (double v : w) dev.add(Math.abs(v - median));
            double mad = median(dev) * MAD_TO_SIGMA;
            if (!(mad > 0)) continue;

            if (out[i] <= 0) continue; // zeros handled by missing-vs-zero pipeline
            double logVal = Math.log(out[i]);
            double z = Math.abs(logVal - median) / mad;
            if (z > k) {
                double replacement = Math.exp(median);
                anomalies.add(new Anomaly(i, out[i], replacement, z));
                switch (policy) {
                    case WINSORIZE: out[i] = replacement; break;
                    case REJECT:    out[i] = Double.NaN; break;
                    case RAW:       /* no-op */ break;
                }
            }
        }
        return new Result(out, anomalies);
    }

    private static double median(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double[] arr = values.stream().mapToDouble(Double::doubleValue).toArray();
        Arrays.sort(arr);
        int n = arr.length;
        return n % 2 == 1 ? arr[n / 2] : 0.5 * (arr[n / 2 - 1] + arr[n / 2]);
    }
}
