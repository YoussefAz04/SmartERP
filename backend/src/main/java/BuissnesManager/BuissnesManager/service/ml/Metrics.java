package BuissnesManager.BuissnesManager.service.ml;


public final class Metrics {

    
    public static final double MAPE_EPS = 1.0;

   
    public static final double MAPE_REL_EPS = 0.01;

    private Metrics() {}

    
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

   
    private static double meanAbs(double[] x) {
        if (x == null || x.length == 0) return 0.0;
        double s = 0.0;
        for (double v : x) s += Math.abs(v);
        return s / x.length;
    }

    
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
