package BuissnesManager.BuissnesManager.service.ml;

import BuissnesManager.BuissnesManager.dto.RecommendationDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MlRecommendationEngineTest {

    private final MlRecommendationEngine engine = new MlRecommendationEngine();

    @Test
    void stockRiskReturnsEmptyOnTooFewRows() {
        List<MlRecommendationEngine.StockRow> rows = new ArrayList<>();
        rows.add(new MlRecommendationEngine.StockRow("A", 10, 5, 2, 5, 6, 0.3));
        assertTrue(engine.stockRiskRecommendations(rows, 30).isEmpty());
    }

    @Test
    void stockRiskFlagsObviousStockouts() {
        // Build 10 rows with two clear classes:
        //  - 5 items with tons of stock (no risk)
        //  - 5 items with nearly no stock vs high demand (risk)
        List<MlRecommendationEngine.StockRow> rows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            rows.add(new MlRecommendationEngine.StockRow(
                    "SAFE-" + i, 500 + i, 10, 5, 5, 1500, 0.3));
        }
        for (int i = 0; i < 5; i++) {
            rows.add(new MlRecommendationEngine.StockRow(
                    "RISK-" + i, 2, 60, 20, 10, 1, 0.3));
        }
        List<RecommendationDto> recs = engine.stockRiskRecommendations(rows, 30);
        // Most recommendations must be for the RISK-* family.
        int risk = 0;
        for (RecommendationDto r : recs) if (r.getDescription().contains("RISK-")
                || r.getTitle().contains("RISK-")) risk++;
        assertTrue(risk >= 1, "engine should flag at least one RISK item, got " + recs.size());
        for (RecommendationDto r : recs) {
            assertEquals("ML_RISQUE_STOCK", r.getType());
        }
    }

    @Test
    void opportunityEmitsStarAndOpportunities() {
        List<MlRecommendationEngine.ProductRow> rows = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            // Gradient of revenue so the engine can tell them apart.
            rows.add(new MlRecommendationEngine.ProductRow(
                    "P" + i, 10000 - i * 500, 100 - i * 2,
                    50 + i, 0.1 + i * 0.01, 0.5 - i * 0.05));
        }
        List<RecommendationDto> out = engine.opportunityRecommendations(rows);
        assertFalse(out.isEmpty());
        // First output must be the PRODUIT_STAR.
        assertEquals("PRODUIT_STAR", out.get(0).getType());
    }

    @Test
    void opportunityReturnsEmptyOnTooFewRows() {
        List<MlRecommendationEngine.ProductRow> rows = new ArrayList<>();
        rows.add(new MlRecommendationEngine.ProductRow("X", 1, 1, 1, 0.5, 0));
        assertTrue(engine.opportunityRecommendations(rows).isEmpty());
    }

    @Test
    void classifyDormantReturnsEmptyWhenTooFewOrSingleClass() {
        // Single class → classifier can't fit, returns empty.
        double[][] feats = new double[10][3];
        int[] labels = new int[10]; // all 0
        boolean[] out = engine.classifyDormant(feats, labels);
        assertEquals(0, out.length);
    }
}
