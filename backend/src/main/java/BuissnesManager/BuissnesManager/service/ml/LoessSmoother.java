package BuissnesManager.BuissnesManager.service.ml;

public final class LoessSmoother {

    private LoessSmoother() {}

    


    public static double[] smooth(double[] y, double frac) {
        if (y == null || y.length == 0) return new double[0];
        int n = y.length;
        if (n == 1) return new double[]{y[0]};
        int band = Math.max(2, (int) Math.ceil(frac * n));
        band = Math.min(n, band);
        double[] out = new double[n];
        double[] dist = new double[n];

        for (int i = 0; i < n; i++) {
            
            for (int j = 0; j < n; j++) dist[j] = Math.abs(j - i);
            
            double[] sortedDist = dist.clone();
            java.util.Arrays.sort(sortedDist);
            double dmax = Math.max(1e-9, sortedDist[band - 1]);

            
            
            double sw = 0, swx = 0, swxx = 0, swy = 0, swxy = 0;
            for (int j = 0; j < n; j++) {
                double d = dist[j] / dmax;
                if (d >= 1.0) continue;
                double u = 1.0 - d * d * d;
                double w = u * u * u;          
                sw += w;
                swx += w * j;
                swxx += w * j * j;
                swy += w * y[j];
                swxy += w * j * y[j];
            }
            double det = sw * swxx - swx * swx;
            if (Math.abs(det) < 1e-12) {
                out[i] = sw > 0 ? swy / sw : y[i];
            } else {
                double a = (swxx * swy - swx * swxy) / det;     
                double b = (sw * swxy - swx * swy) / det;       
                out[i] = a + b * i;
            }
        }
        return out;
    }
}
