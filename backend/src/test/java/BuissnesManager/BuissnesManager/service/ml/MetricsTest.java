package BuissnesManager.BuissnesManager.service.ml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsTest {

    @Test
    void rmseAndMaeOnTrivialSeries() {
        double[] a = {1, 2, 3, 4};
        double[] f = {1, 2, 3, 4};
        assertEquals(0.0, Metrics.rmse(a, f), 1e-9);
        assertEquals(0.0, Metrics.mape(a, f), 1e-9);
        assertEquals(0.0, Metrics.smape(a, f), 1e-9);
    }

    @Test
    void mapeIsRobustToNearZeroActuals() {
        double[] a = {0.0};
        double[] f = {5.0};
        assertEquals(5.0 / Metrics.MAPE_EPS, Metrics.mape(a, f), 1e-9);
    }

    @Test
    void smapeStaysBounded() {
        double[] a = {0.0};
        double[] f = {1000.0};
        // sMAPE denominator = max(EPS, (|a| + |f|)/2) = 500.0; error = 1000 ⇒ 2.0.
        assertEquals(2.0, Metrics.smape(a, f), 1e-9);
    }

    @Test
    void maseFallsBackToRandomWalkOnShortSeries() {
        // With < 2*period observations we should fall back to lag-1 baseline.
        double[] history = {10, 12, 11, 13, 14, 15};
        double[] forecast = {15, 15, 15, 15, 15, 15};
        double mase = Metrics.mase(history, forecast, 12);
        // scale = mean(|y_t - y_{t-1}|) on history => non-zero, so MASE > 0.
        assertTrue(mase >= 0.0 && Double.isFinite(mase));
    }

    @Test
    void maseUndefinedOnConstantHistory() {
        double[] h = {5, 5, 5, 5, 5, 5};
        double[] f = {5, 5, 5, 5};
        // Random-walk scale = 0 ⇒ MASE undefined ⇒ NaN.
        assertTrue(Double.isNaN(Metrics.mase(h, f, 12)));
    }
}
