package BuissnesManager.BuissnesManager.service.ml;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplierRatingEngineTest {

    private final SupplierRatingEngine engine = new SupplierRatingEngine();

    @Test
    void fallsBackToBaselineOnSmallSample() {
        List<SupplierRatingEngine.Input> in = new ArrayList<>();
        in.add(new SupplierRatingEngine.Input(10, 10000, 0.9, 0.05));
        in.add(new SupplierRatingEngine.Input(5, 4000, 0.6, 0.10));
        List<SupplierRatingEngine.Output> out = engine.rate(in);
        assertEquals(2, out.size());
        assertEquals("Baseline", out.get(0).modelName);
        assertTrue(out.get(0).ratingStars >= 1.0 && out.get(0).ratingStars <= 5.0);
    }

    @Test
    void rewardsOverPerformersRelativeToPeers() {
        List<SupplierRatingEngine.Input> in = new ArrayList<>();
        // Peer group: high-volume suppliers typically deliver around 85%.
        in.add(new SupplierRatingEngine.Input(50, 100000, 0.85, 0.05));
        in.add(new SupplierRatingEngine.Input(45, 90000, 0.83, 0.05));
        in.add(new SupplierRatingEngine.Input(55, 110000, 0.84, 0.06));
        in.add(new SupplierRatingEngine.Input(40, 85000, 0.86, 0.04));
        // Over-performer: same scale but 98% delivery.
        in.add(new SupplierRatingEngine.Input(48, 95000, 0.98, 0.02));
        // Under-performer: same scale but 70%.
        in.add(new SupplierRatingEngine.Input(50, 100000, 0.70, 0.10));
        List<SupplierRatingEngine.Output> out = engine.rate(in);
        assertEquals(6, out.size());
        assertEquals("OLS peer-group", out.get(0).modelName);
        double overStars = out.get(4).ratingStars;
        double underStars = out.get(5).ratingStars;
        assertTrue(overStars > underStars,
                "over-performer must rank higher than under-performer, got "
                        + overStars + " vs " + underStars);
        assertTrue(overStars >= 3.0, "over-performer should earn an above-average rating, got " + overStars);
    }

    @Test
    void ratingIsBoundedInTheStarsRange() {
        List<SupplierRatingEngine.Input> in = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            in.add(new SupplierRatingEngine.Input(10 + i, 1000 + i * 500, 0.5 + i * 0.04, 0.05));
        }
        for (SupplierRatingEngine.Output o : engine.rate(in)) {
            assertTrue(o.ratingStars >= 1.0 && o.ratingStars <= 5.0,
                    "rating out of range: " + o.ratingStars);
        }
    }
}
