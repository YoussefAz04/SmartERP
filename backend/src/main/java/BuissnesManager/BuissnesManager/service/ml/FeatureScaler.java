package BuissnesManager.BuissnesManager.service.ml;


public final class FeatureScaler {

    private FeatureScaler() {}

    
    public enum Mode { Z_SCORE, ROBUST }

    
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

    
    public static double[][] scale(double[][] x, Mode mode) {
        Stats s = fit(x, mode);
        return apply(x, s);
    }

    
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

    
    public static double median(double[] v) {
        if (v == null || v.length == 0) return 0.0;
        double[] copy = v.clone();
        java.util.Arrays.sort(copy);
        int n = copy.length;
        return n % 2 == 1 ? copy[n / 2] : 0.5 * (copy[n / 2 - 1] + copy[n / 2]);
    }
}
