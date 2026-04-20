package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the revamped {@link ForecastService}. Do not load a Spring
 * context — the goal is to validate the algorithm in isolation so the suite can
 * run on machines that don't have the production SQL Server reachable.
 */
class ForecastServiceTest {

    @Mock
    private CommandeVenteRepository commandeVenteRepository;

    @InjectMocks
    private ForecastService service;

    private int currentYear;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currentYear = LocalDate.now().getYear();
    }

    /* Helper: build a mock JPQL response row [monthNumber, totalTTC]. */
    private static Object[] row(int month, double amount) {
        return new Object[]{month, amount};
    }

    @Test
    void emptyDataReturnsWellFormedDtoWithZeroedPredictions() {
        when(commandeVenteRepository.getMonthlySales(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        ForecastDto dto = service.generateForecast(1L);

        assertNotNull(dto);
        assertNotNull(dto.getForecastPoints());
        // 6 future points at least (plus optional historical points up to today)
        assertTrue(dto.getForecastPoints().size() >= 6);
        assertEquals(0.0, dto.getPredictedNextMonth(), 0.001);
        assertEquals(0.0, dto.getPredictedNext3Months(), 0.001);
        assertEquals(0.0, dto.getPredictedNext6Months(), 0.001);
        assertTrue(dto.getConfidenceScore() >= 0.0);
        assertTrue(dto.getConfidenceScore() <= 95.0);
        assertNotNull(dto.getMethodology());
        // Seasonality list must always have 12 entries for a calendar grid.
        assertEquals(12, dto.getSeasonality().size());
    }

    @Test
    void noPriorYearWithGrowingCurrentYearProducesPositiveForecast() {
        int monthsSoFar = LocalDate.now().getMonthValue();
        List<Object[]> currentYearRows = new ArrayList<>();
        for (int m = 1; m <= Math.min(6, monthsSoFar); m++) {
            currentYearRows.add(row(m, 1000.0 * m));
        }
        when(commandeVenteRepository.getMonthlySales(eq(7L), eq(currentYear)))
                .thenReturn(currentYearRows);
        when(commandeVenteRepository.getMonthlySales(eq(7L), eq(currentYear - 1)))
                .thenReturn(Collections.emptyList());

        ForecastDto dto = service.generateForecast(7L);

        assertNotNull(dto.getPredictedNextMonth());
        assertTrue(dto.getPredictedNextMonth() > 0.0,
                "forecast must be positive on a growing series");
        for (ForecastDto.ForecastPointDto p : dto.getForecastPoints()) {
            if (p.getActual() == null) {
                assertNotNull(p.getForecast());
                assertTrue(p.getForecast() >= 0.0);
                assertTrue(p.getLowerBound() >= 0.0);
                assertTrue(p.getUpperBound() >= p.getForecast() - 1e-6);
            }
        }
        assertTrue(dto.getMethodology().toLowerCase().contains("holt"));
    }

    @Test
    void withPriorYearAppliesMultiplicativeSeasonality() {
        // Previous year: strong seasonality — monotonically increasing
        List<Object[]> prevYearRows = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            prevYearRows.add(row(m, 1000.0 + (m * 400.0))); // 1400..5800
        }
        int monthsSoFar = LocalDate.now().getMonthValue();
        List<Object[]> currentYearRows = new ArrayList<>();
        for (int m = 1; m <= Math.min(3, monthsSoFar); m++) {
            currentYearRows.add(row(m, 2000.0));
        }
        when(commandeVenteRepository.getMonthlySales(eq(9L), eq(currentYear)))
                .thenReturn(currentYearRows);
        when(commandeVenteRepository.getMonthlySales(eq(9L), eq(currentYear - 1)))
                .thenReturn(prevYearRows);

        ForecastDto dto = service.generateForecast(9L);

        assertNotNull(dto);
        assertTrue(dto.getMethodology().toLowerCase().contains("saisonn"));
        // December is well above the prior-year average -> HAUTE
        ForecastDto.SeasonalityDto dec = dto.getSeasonality().get(11);
        assertEquals("Déc", dec.getMonth());
        assertEquals("HAUTE", dec.getTrend());
        // January is well below average -> BASSE
        assertEquals("BASSE", dto.getSeasonality().get(0).getTrend());
    }

    @Test
    void confidenceScoreStaysInRangeOnFlatHistory() {
        List<Object[]> flat = new ArrayList<>();
        for (int m = 1; m <= 12; m++) flat.add(row(m, 500.0));
        when(commandeVenteRepository.getMonthlySales(anyLong(), anyInt())).thenReturn(flat);

        ForecastDto dto = service.generateForecast(1L);

        assertTrue(dto.getConfidenceScore() >= 0.0);
        assertTrue(dto.getConfidenceScore() <= 95.0);
    }

    @Test
    void malformedRowsAreIgnoredInsteadOfCrashing() {
        List<Object[]> weird = new ArrayList<>();
        weird.add(new Object[]{null, 100.0});      // null month -> skipped
        weird.add(new Object[]{3, null});          // null amount -> 0
        weird.add(row(5, 400.0));                  // valid
        when(commandeVenteRepository.getMonthlySales(anyLong(), anyInt())).thenReturn(weird);

        ForecastDto dto = service.generateForecast(1L);

        assertNotNull(dto);
        assertFalse(dto.getForecastPoints().isEmpty());
    }

    @Test
    void predictedSummariesMatchSumOfFutureForecastPoints() {
        int monthsSoFar = LocalDate.now().getMonthValue();
        List<Object[]> currentYearRows = new ArrayList<>();
        for (int m = 1; m <= monthsSoFar; m++) {
            currentYearRows.add(row(m, 200.0 + m * 50.0));
        }
        when(commandeVenteRepository.getMonthlySales(anyLong(), eq(currentYear)))
                .thenReturn(currentYearRows);
        when(commandeVenteRepository.getMonthlySales(anyLong(), eq(currentYear - 1)))
                .thenReturn(Collections.emptyList());

        ForecastDto dto = service.generateForecast(1L);

        double sum3 = 0.0, sum6 = 0.0;
        int future = 0;
        for (ForecastDto.ForecastPointDto p : dto.getForecastPoints()) {
            if (p.getActual() == null && p.getForecast() != null) {
                if (future < 3) sum3 += p.getForecast();
                if (future < 6) sum6 += p.getForecast();
                future++;
            }
        }
        // Rounded to 2 decimals in the DTO; allow a little slack for display rounding.
        assertEquals(Math.round(sum3 * 100.0) / 100.0, dto.getPredictedNext3Months(), 0.1);
        assertEquals(Math.round(sum6 * 100.0) / 100.0, dto.getPredictedNext6Months(), 0.1);
    }
}
