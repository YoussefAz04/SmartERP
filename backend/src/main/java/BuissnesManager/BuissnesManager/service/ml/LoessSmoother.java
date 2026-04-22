package BuissnesManager.BuissnesManager.service.ml;

/**
 * Minimal locally-weighted linear regression (LOESS / LOWESS) smoother. Used
 * as a drop-in replacement for the simple moving-average trend line in the
 * monthly-sales chart: unlike a moving average it doesn't lag the series by
 * {@code window/2} months and it tracks the local slope rather than the local
 * mean, which matters at the edges of a short 12-month series.
 *
 * <p>Implementation notes:
 * <ul>
 *   <li>Tricube kernel <code>w = (1 - (d/dmax)^3)^3</code> — the canonical LOESS
 *       weight function.</li>
 *   <li>Degree-1 local polynomial (local weighted <i>linear</i> regression),
 *       which is the classic LOWESS choice and avoids the numerical
 *       instabilities of higher-degree fits on small samples.</li>
 *   <li>Closed-form weighted normal equations on (1, x) — just a 2×2 solve
 *       per point, so a 12-month smoothing costs microseconds.</li>
 * </ul>
 *
 * <p>No Smile API is used directly because Smile 3.1.1 doesn't ship a LOESS
 * regressor; the maths is short enough to own.
 */
public final class LoessSmoother {

    private LoessSmoother() {}

    /**
     * Smooths {@code y} with a LOESS fit at every input abscissa
     * {@code x[i] = i}. The bandwidth is expressed as the <b>fraction</b> of
     * the series considered local; {@code 0.5} is a reasonable default for
     * monthly series.
     *
     * @param y    values to smooth (copied, not mutated)
     * @param frac bandwidth as a fraction of the series length in (0, 1]
     * @return a new array of the same length holding the smoothed values
     */
    public static double[] smooth(double[] y, double frac) {
        if (y == null || y.length == 0) return new double[0];
        int n = y.length;
        if (n == 1) return new double[]{y[0]};
        int band = Math.max(2, (int) Math.ceil(frac * n));
        band = Math.min(n, band);
        double[] out = new double[n];
        double[] dist = new double[n];

        for (int i = 0; i < n; i++) {
            // Distances from i to every other point.
            for (int j = 0; j < n; j++) dist[j] = Math.abs(j - i);
            // Pick the k-th nearest distance as the bandwidth denominator.
            double[] sortedDist = dist.clone();
            java.util.Arrays.sort(sortedDist);
            double dmax = Math.max(1e-9, sortedDist[band - 1]);

            // Weighted normal equations on [1, x]:
            //   S_w * [a, b]^T = [Σw*y, Σw*x*y]
            double sw = 0, swx = 0, swxx = 0, swy = 0, swxy = 0;
            for (int j = 0; j < n; j++) {
                double d = dist[j] / dmax;
                if (d >= 1.0) continue;
                double u = 1.0 - d * d * d;
                double w = u * u * u;          // tricube
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
                double a = (swxx * swy - swx * swxy) / det;     // intercept
                double b = (sw * swxy - swx * swy) / det;       // slope
                out[i] = a + b * i;
            }
        }
        return out;
    }
}
