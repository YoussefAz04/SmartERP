package BuissnesManager.BuissnesManager.service.ml;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.YearMonth;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalendarFeatureServiceTest {

    private CalendarFeatureService svc() {
        CalendarFeatureService s = new CalendarFeatureService();
        ReflectionTestUtils.setField(s, "configuredCountry", "TN");
        return s;
    }

    @Test
    void featureNamesAreStable() {
        // Stable contract — these names are persisted in PredictionLog and
        // surfaced in the methodology panel; do not reorder without migration.
        assertEquals(4, CalendarFeatureService.FEATURE_NAMES.length);
        assertEquals("is_ramadan", CalendarFeatureService.FEATURE_NAMES[1]);
        assertEquals("is_year_end", CalendarFeatureService.FEATURE_NAMES[3]);
    }

    @Test
    void endOfQuarterAndYearEndAreOneHot() {
        CalendarFeatureService s = svc();
        double[] dec = s.features(YearMonth.of(2024, 12), null);
        assertEquals(1.0, dec[2], 1e-9, "December is end-of-quarter");
        assertEquals(1.0, dec[3], 1e-9, "December is year-end");
        double[] feb = s.features(YearMonth.of(2024, 2), null);
        assertEquals(0.0, feb[2], 1e-9);
        assertEquals(0.0, feb[3], 1e-9);
    }

    @Test
    void ramadanFractionForKnownYearIsPositive() {
        CalendarFeatureService s = svc();
        // Ramadan 2024 starts ~11 March.
        double[] mar = s.features(YearMonth.of(2024, 3), null);
        assertTrue(mar[1] > 0.0, "March 2024 should overlap Ramadan, got " + mar[1]);
        double[] feb = s.features(YearMonth.of(2024, 2), null);
        assertEquals(0.0, feb[1], 1e-9);
    }

    @Test
    void glossaryDocumentsEveryFeature() {
        Map<String, String> gloss = CalendarFeatureService.glossary();
        for (String name : CalendarFeatureService.FEATURE_NAMES) {
            assertTrue(gloss.containsKey(name), "missing glossary entry for " + name);
        }
    }
}

