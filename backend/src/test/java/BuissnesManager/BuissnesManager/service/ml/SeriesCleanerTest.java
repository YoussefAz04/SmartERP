package BuissnesManager.BuissnesManager.service.ml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeriesCleanerTest {

    /** Returns a noisy-but-trended seasonal series of {@code n} months. */
    private static double[] season(int n) {
        double[] s = new double[n];
        for (int i = 0; i < n; i++) {
            s[i] = 1000.0 + 30.0 * i + 200.0 * Math.sin(2 * Math.PI * i / 12.0)
                    + 25.0 * Math.cos(i); // small noise so the rolling MAD is non-zero
        }
        return s;
    }

    @Test
    void winsorizesObviousOutlier() {
        double[] series = season(30);
        double pre = series[15];
        series[15] = 100_000.0; // ~50× the typical level

        SeriesCleaner.Result r = SeriesCleaner.clean(series, SeriesCleaner.Policy.WINSORIZE,
                SeriesCleaner.DEFAULT_WINDOW, SeriesCleaner.DEFAULT_K);
        assertFalse(r.anomalies.isEmpty(), "should detect the planted spike");
        assertTrue(r.cleaned[15] < 10_000.0,
                "spike should have been replaced; got " + r.cleaned[15]);
        assertTrue(Math.abs(r.cleaned[15] - pre) < pre, "replacement is in the right ballpark");
    }

    @Test
    void rejectMarksOutlierAsNaN() {
        double[] series = season(30);
        series[10] = 80_000.0;
        SeriesCleaner.Result r = SeriesCleaner.clean(series, SeriesCleaner.Policy.REJECT,
                SeriesCleaner.DEFAULT_WINDOW, SeriesCleaner.DEFAULT_K);
        assertFalse(r.anomalies.isEmpty());
        assertTrue(Double.isNaN(r.cleaned[10]));
    }

    @Test
    void rawPolicyDetectsButLeavesValuesIntact() {
        double[] series = season(30);
        series[20] = 80_000.0;
        SeriesCleaner.Result r = SeriesCleaner.clean(series, SeriesCleaner.Policy.RAW,
                SeriesCleaner.DEFAULT_WINDOW, SeriesCleaner.DEFAULT_K);
        // RAW still reports anomalies but does not modify the series.
        assertFalse(r.anomalies.isEmpty());
        assertEquals(80_000.0, r.cleaned[20], 1e-9);
    }

    @Test
    void shortSeriesIsReturnedUnmodified() {
        double[] series = {1, 2, 3};
        SeriesCleaner.Result r = SeriesCleaner.clean(series);
        assertArrayEquals(series, r.cleaned);
        assertTrue(r.anomalies.isEmpty());
    }
}

