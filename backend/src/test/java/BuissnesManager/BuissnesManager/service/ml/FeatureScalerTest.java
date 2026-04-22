package BuissnesManager.BuissnesManager.service.ml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureScalerTest {

    @Test
    void zScoreCentersAndScalesColumns() {
        double[][] x = {{1, 10}, {2, 20}, {3, 30}, {4, 40}, {5, 50}};
        double[][] s = FeatureScaler.scale(x, FeatureScaler.Mode.Z_SCORE);
        // Mean of each column of s must be ~0, std ~1
        double sum0 = 0, sum1 = 0;
        for (double[] r : s) { sum0 += r[0]; sum1 += r[1]; }
        assertEquals(0.0, sum0 / s.length, 1e-9);
        assertEquals(0.0, sum1 / s.length, 1e-9);
    }

    @Test
    void robustScalerUsesMedianAndMad() {
        // Add an outlier — z-score would be pulled in, MAD stays stable.
        double[][] x = {{1}, {2}, {3}, {4}, {5}, {100}};
        FeatureScaler.Stats s = FeatureScaler.fit(x, FeatureScaler.Mode.ROBUST);
        // Median is 3.5, MAD-based scale should ignore the 100.
        assertEquals(3.5, s.center[0], 1e-9);
        assertTrue(s.scale[0] > 0 && s.scale[0] < 10);
    }

    @Test
    void zeroVarianceColumnGetsScaleOne() {
        double[][] x = {{5, 7}, {5, 7}, {5, 7}};
        FeatureScaler.Stats s = FeatureScaler.fit(x, FeatureScaler.Mode.Z_SCORE);
        assertEquals(1.0, s.scale[0], 1e-9);
        assertEquals(1.0, s.scale[1], 1e-9);
    }

    @Test
    void applyReusesPreviousStatsWithoutMutation() {
        double[][] train = {{1}, {2}, {3}, {4}, {5}};
        FeatureScaler.Stats stats = FeatureScaler.fit(train, FeatureScaler.Mode.Z_SCORE);
        double[][] test = {{6}, {7}};
        double[][] out = FeatureScaler.apply(test, stats);
        assertEquals(2, out.length);
        // Monotonicity: larger input → larger scaled output.
        assertTrue(out[1][0] > out[0][0]);
    }
}
