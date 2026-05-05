package BuissnesManager.BuissnesManager.service.ml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoxCoxTest {

    @Test
    void roundTripWithLogTransformPreservesPositiveValues() {
        BoxCox.Fit f = BoxCox.fit(new double[]{
                100, 110, 121, 133, 146, 161, 177, 195, 215, 236, 260, 286
        });
        // We only assert round-trip stability — the chosen λ depends on the grid.
        for (double y : new double[]{50.0, 100.0, 1000.0}) {
            assertEquals(y, f.inverse(f.forward(y)), 1e-3);
        }
    }

    @Test
    void fitPicksLogForExponentialGrowth() {
        double[] series = new double[24];
        for (int i = 0; i < 24; i++) series[i] = Math.pow(1.1, i) * 100.0;
        BoxCox.Fit f = BoxCox.fit(series);
        assertTrue(Math.abs(f.lambda) <= 0.4,
                "Expected λ near 0 for exponential growth, got " + f.lambda);
    }

    @Test
    void fitDegradesGracefullyOnTooFewObservations() {
        BoxCox.Fit f = BoxCox.fit(new double[]{0, 5});
        assertEquals(1.0, f.lambda, 1e-9);
    }

    @Test
    void inverseIsNonNegative() {
        BoxCox.Fit f = BoxCox.fit(new double[]{1, 2, 3, 4, 5, 6});
        // Inverse must never produce a negative value (sales floor).
        for (double z : new double[]{-100, 0, 100, 1000}) {
            assertTrue(f.inverse(z) >= 0.0, "negative output for z=" + z);
        }
    }
}

