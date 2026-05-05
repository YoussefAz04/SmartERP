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
    void mapeUsesSeriesRelativeFloorForNearZeroActuals() {
        // Revenue-like series: mean ≈ 5000 EUR. A single zero-actual point with a
        // wildly extrapolated forecast of 50 000 EUR must NOT divide by the 1 EUR
        // absolute floor — that produced cross-validated MAPE values like 996 504 %.
        // With the series-relative floor (1 % of mean = 50 EUR) the per-point ratio
        // is bounded to a reasonable order of magnitude.
        double[] actual   = {5000, 5000, 5000, 5000, 5000, 5000, 5000, 0.0};
        double[] forecast = {5000, 5000, 5000, 5000, 5000, 5000, 5000, 50_000};
        double mape = Metrics.mape(actual, forecast);
        // With the old 1 EUR floor this would be 50_000 / 8 = 6250 (= 625 000 %).
        // With the 1 %-of-scale floor (50 EUR) the worst point contributes
        // 50_000 / 50 = 1000, averaged over 8 points => 125 (= 12 500 %).
        assertTrue(mape < 200.0,
                "MAPE should be bounded by the series-relative floor, got " + mape);
        assertTrue(mape > 100.0,
                "MAPE should still reflect the bad forecast, got " + mape);
    }

    @Test
    void mapeOnHealthyActualsIgnoresRelativeFloor() {
        // When every |actual_i| stays above 1 % of the series mean, behaviour is
        // identical to the textbook MAPE — no smoothing leaks into normal cases.
        double[] a = {100, 110, 120, 130};
        double[] f = {110, 110, 110, 110};
        double expected = (10.0/100 + 0.0/110 + 10.0/120 + 20.0/130) / 4.0;
        assertEquals(expected, Metrics.mape(a, f), 1e-9);
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
