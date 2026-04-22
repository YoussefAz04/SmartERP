package BuissnesManager.BuissnesManager.service.ml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic unit tests for the Smile-backed {@link MlForecastEngine}. The
 * tests never talk to the database or Spring context, so they run anywhere.
 */
class MlForecastEngineTest {

    private final MlForecastEngine engine = new MlForecastEngine();

    @Test
    void canFitRequiresMinimumSamples() {
        assertFalse(engine.canFit(0));
        assertFalse(engine.canFit(MlForecastEngine.MIN_SAMPLES - 1));
        assertTrue(engine.canFit(MlForecastEngine.MIN_SAMPLES));
        assertTrue(engine.canFit(100));
    }

    @Test
    void throwsWhenHistoryTooShort() {
        double[] tooShort = new double[5];
        assertThrows(IllegalArgumentException.class,
                () -> engine.forecast(tooShort, 0, 6));
    }

    @Test
    void throwsOnNonPositiveHorizon() {
        double[] history = linearSeries(24);
        assertThrows(IllegalArgumentException.class,
                () -> engine.forecast(history, 0, 0));
    }

    @Test
    void fitsLinearlyGrowingSeriesAndProjectsForward() {
        // Perfect linear ramp: OLS should recover it almost perfectly and
        // project it forward (future value > last observation).
        double[] history = linearSeries(36);
        MlForecastEngine.MlForecastResult r = engine.forecast(history, 0, 6);

        assertNotNull(r);
        assertEquals(36, r.fittedHistorical.size());
        assertEquals(6, r.futurePoint.size());
        // R² on a near-linear series must be very high
        assertTrue(r.rSquared > 0.95,
                "OLS should fit a linear series near-perfectly; got R²=" + r.rSquared);
        // Residuals must be small relative to the series magnitude
        assertTrue(r.residualStd < history[history.length - 1] * 0.1);
        // The forecast should trend upward (not flat, not negative)
        double last = history[history.length - 1];
        double firstFuture = r.futurePoint.get(0);
        assertTrue(firstFuture >= last * 0.5,
                "forecast should not collapse; got " + firstFuture);
        for (double v : r.futurePoint) {
            assertTrue(v >= 0.0, "forecast must never go negative, got " + v);
        }
    }

    @Test
    void reproducesSeasonalPattern() {
        // 24 months with a clear 12-month sinusoidal pattern
        double[] history = new double[24];
        for (int t = 0; t < 24; t++) {
            history[t] = 1000.0 + 200.0 * Math.sin(2.0 * Math.PI * t / 12.0);
        }
        MlForecastEngine.MlForecastResult r = engine.forecast(history, 0, 12);

        assertEquals(12, r.futurePoint.size());
        // Season-high months (around t=3, 15, 27) should be higher than season-low
        // months (around t=9, 21) in the forecast.
        double high = r.futurePoint.get(2); // t=26 -> month index ~2
        double low = r.futurePoint.get(8);  // t=32 -> month index ~8
        assertTrue(high > low,
                "seasonal pattern should persist into the forecast: " + high + " vs " + low);
    }

    @Test
    void fittedValuesLengthMatchesHistoryLength() {
        double[] history = linearSeries(20);
        MlForecastEngine.MlForecastResult r = engine.forecast(history, 3, 6);
        assertEquals(history.length, r.fittedHistorical.size());
        for (Double v : r.fittedHistorical) {
            assertTrue(v >= 0.0);
        }
    }

    private static double[] linearSeries(int n) {
        double[] s = new double[n];
        for (int t = 0; t < n; t++) {
            s[t] = 100.0 + 10.0 * t;
        }
        return s;
    }
}
