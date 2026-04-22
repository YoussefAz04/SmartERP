package BuissnesManager.BuissnesManager.service.ml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoessSmootherTest {

    @Test
    void smoothsLinearSeriesCloseToIdentity() {
        // Perfect linear ramp: local linear fit should reproduce it.
        double[] y = new double[24];
        for (int i = 0; i < 24; i++) y[i] = 10.0 + 5.0 * i;
        double[] s = LoessSmoother.smooth(y, 0.5);
        for (int i = 0; i < 24; i++) {
            assertEquals(y[i], s[i], 1.0);
        }
    }

    @Test
    void smoothsNoisyLinearSeriesReducingVariance() {
        double[] y = new double[40];
        double[] clean = new double[40];
        java.util.Random rng = new java.util.Random(123L);
        for (int i = 0; i < 40; i++) {
            clean[i] = 100.0 + 2.0 * i;
            y[i] = clean[i] + rng.nextGaussian() * 30.0;
        }
        double[] s = LoessSmoother.smooth(y, 0.4);
        double rawVar = variance(diff(y, clean));
        double smVar = variance(diff(s, clean));
        assertTrue(smVar < rawVar,
                "LOESS must reduce error variance against the truth, got " + smVar + " vs " + rawVar);
    }

    @Test
    void handlesSingletonAndEmpty() {
        assertEquals(0, LoessSmoother.smooth(new double[0], 0.5).length);
        double[] one = LoessSmoother.smooth(new double[]{42.0}, 0.5);
        assertEquals(1, one.length);
        assertEquals(42.0, one[0], 1e-9);
    }

    private static double[] diff(double[] a, double[] b) {
        double[] d = new double[a.length];
        for (int i = 0; i < a.length; i++) d[i] = a[i] - b[i];
        return d;
    }

    private static double variance(double[] x) {
        double m = 0;
        for (double v : x) m += v;
        m /= x.length;
        double s = 0;
        for (double v : x) { double d = v - m; s += d * d; }
        return s / Math.max(1, x.length - 1);
    }
}
