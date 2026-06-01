package BuissnesManager.BuissnesManager.service.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SeriesCleaner {

    
    public static final int DEFAULT_WINDOW = 12;
    
    public static final double DEFAULT_K = 3.5;
    
    private static final double MAD_TO_SIGMA = 1.4826;

    
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

    
    public static final class Result {
        public final double[] cleaned;
        public final List<Anomaly> anomalies;
        Result(double[] cleaned, List<Anomaly> anomalies) {
            this.cleaned = cleaned;
            this.anomalies = anomalies;
        }
    }

    
    public enum Policy {
        
        WINSORIZE,
        
        REJECT,
        
        RAW;

        public static Policy parse(String s) {
            if (s == null) return WINSORIZE;
            try { return Policy.valueOf(s.trim().toUpperCase(java.util.Locale.ROOT)); }
            catch (IllegalArgumentException ex) { return WINSORIZE; }
        }
    }

    private SeriesCleaner() {}

    
    public static Result clean(double[] series) {
        return clean(series, Policy.WINSORIZE, DEFAULT_WINDOW, DEFAULT_K);
    }



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
            
            
            List<Double> w = new ArrayList<>(hi - lo);
            for (int j = lo; j < hi; j++) {
                if (out[j] > 0) w.add(Math.log(out[j]));
            }
            if (w.size() < 5) continue; 
            double median = median(w);
            List<Double> dev = new ArrayList<>(w.size());
            for (double v : w) dev.add(Math.abs(v - median));
            double mad = median(dev) * MAD_TO_SIGMA;
            if (!(mad > 0)) continue;

            if (out[i] <= 0) continue; 
            double logVal = Math.log(out[i]);
            double z = Math.abs(logVal - median) / mad;
            if (z > k) {
                double replacement = Math.exp(median);
                anomalies.add(new Anomaly(i, out[i], replacement, z));
                switch (policy) {
                    case WINSORIZE: out[i] = replacement; break;
                    case REJECT:    out[i] = Double.NaN; break;
                    case RAW:        break;
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
