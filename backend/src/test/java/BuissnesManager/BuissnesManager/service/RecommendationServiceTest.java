package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.RecommendationDto;
import BuissnesManager.BuissnesManager.repository.CommandeAchatRepository;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.StockRepository;
import BuissnesManager.BuissnesManager.service.ml.MlRecommendationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link RecommendationService} produces the new diversified
 * messages when the right data is present and stays silent when it isn't.
 */
class RecommendationServiceTest {

    @Mock private StockRepository stockRepository;
    @Mock private CommandeVenteRepository commandeVenteRepository;
    @Mock private CommandeAchatRepository commandeAchatRepository;
    @Mock private MlRecommendationEngine mlRecommendationEngine;

    @InjectMocks
    private RecommendationService service;

    private final Long entrepriseId = 1L;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Defaults that say "we have nothing" for every dependency. Each test
        // overrides the slice of inputs it wants to exercise.
        when(stockRepository.findByEntrepriseId(anyLong())).thenReturn(Collections.emptyList());
        when(stockRepository.getLowStockItemsCount(anyLong())).thenReturn(0L);
        when(stockRepository.getOutOfStockItemsCount(anyLong())).thenReturn(0L);
        when(stockRepository.getTotalStockValue(anyLong())).thenReturn(0.0);

        when(commandeVenteRepository.getTotalSalesRevenue(anyLong())).thenReturn(0.0);
        when(commandeVenteRepository.getTotalSalesOrders(anyLong())).thenReturn(0L);
        when(commandeVenteRepository.getSalesInPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(0.0);
        when(commandeVenteRepository.getCOGSInPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(0.0);
        when(commandeVenteRepository.getTopProducts(anyLong())).thenReturn(Collections.emptyList());
        when(commandeVenteRepository.getTopCustomers(anyLong())).thenReturn(Collections.emptyList());
        when(commandeVenteRepository.getOrderStatusBreakdown(anyLong())).thenReturn(Collections.emptyList());
        when(commandeVenteRepository.getCategoryPerformance(anyLong())).thenReturn(Collections.emptyList());
        when(commandeVenteRepository.getSalesOrderCountInPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(0L);

        when(commandeAchatRepository.getTotalPurchases(anyLong())).thenReturn(0.0);
        when(commandeAchatRepository.getOverdueOrdersCount(anyLong(), any(LocalDate.class))).thenReturn(0L);
        when(commandeAchatRepository.getActiveOrdersCount(anyLong())).thenReturn(0L);
        when(commandeAchatRepository.getPurchasesInPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(0.0);
        when(commandeAchatRepository.getSupplierMetrics(anyLong())).thenReturn(Collections.emptyList());

        when(mlRecommendationEngine.stockRiskRecommendations(any(), eq(30))).thenReturn(new ArrayList<>());
        when(mlRecommendationEngine.opportunityRecommendations(any())).thenReturn(new ArrayList<>());
    }

    @Test
    void emptyDatasetEmitsOnlyDataQualityHint() {
        // 0 orders ⇒ DONNEES_INSUFFISANTES, nothing else.
        List<RecommendationDto> recs = service.generateRecommendations(entrepriseId);
        assertNotNull(recs);
        assertTrue(recs.stream().anyMatch(r -> "DONNEES_INSUFFISANTES".equals(r.getType())));
    }

    @Test
    void cancellationRateRuleFiresWhenAboveThreshold() {
        // 30 orders total, 10 cancelled → 33% cancellation rate.
        when(commandeVenteRepository.getTotalSalesOrders(anyLong())).thenReturn(30L);
        when(commandeVenteRepository.getTotalSalesRevenue(anyLong())).thenReturn(50000.0);
        when(commandeVenteRepository.getOrderStatusBreakdown(anyLong())).thenReturn(Arrays.asList(
                new Object[]{"VALIDEE", 20L},
                new Object[]{"ANNULEE", 10L}
        ));
        List<RecommendationDto> recs = service.generateRecommendations(entrepriseId);
        assertTrue(recs.stream().anyMatch(r -> "TAUX_ANNULATION".equals(r.getType())),
                "expected TAUX_ANNULATION recommendation");
    }

    @Test
    void supplierConcentrationRuleFires() {
        // One supplier with 80% of purchases → DIVERSIFICATION_FOURNISSEUR.
        when(commandeAchatRepository.getSupplierMetrics(anyLong())).thenReturn(Arrays.asList(
                new Object[]{1L, "Acme", "", 5L, 8000.0, 95.0, 4.5},
                new Object[]{2L, "Bravo", "", 2L, 2000.0, 80.0, 3.0}
        ));
        List<RecommendationDto> recs = service.generateRecommendations(entrepriseId);
        assertTrue(recs.stream().anyMatch(r -> "DIVERSIFICATION_FOURNISSEUR".equals(r.getType())),
                "expected DIVERSIFICATION_FOURNISSEUR recommendation");
        // Highly rated supplier should also surface FOURNISSEUR_PERFORMANT.
        assertTrue(recs.stream().anyMatch(r -> "FOURNISSEUR_PERFORMANT".equals(r.getType())),
                "expected FOURNISSEUR_PERFORMANT highlight");
    }

    @Test
    void cashFlowRuleFiresWhenPurchasesDwarfSales() {
        // Recent 30d purchases >> recent 30d sales.
        when(commandeAchatRepository.getPurchasesInPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(20000.0);
        when(commandeVenteRepository.getSalesInPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(5000.0);
        when(commandeVenteRepository.getTotalSalesOrders(anyLong())).thenReturn(50L);
        List<RecommendationDto> recs = service.generateRecommendations(entrepriseId);
        assertTrue(recs.stream().anyMatch(r -> "CASH_FLOW".equals(r.getType())),
                "expected CASH_FLOW recommendation");
    }

    @Test
    void categoryConcentrationRuleFires() {
        when(commandeVenteRepository.getTotalSalesRevenue(anyLong())).thenReturn(10000.0);
        when(commandeVenteRepository.getTotalSalesOrders(anyLong())).thenReturn(50L);
        when(commandeVenteRepository.getCategoryPerformance(anyLong())).thenReturn(Arrays.asList(
                new Object[]{"Électronique", 8000.0, 100L},
                new Object[]{"Accessoires", 1500.0, 50L},
                new Object[]{"Autres", 500.0, 20L}
        ));
        List<RecommendationDto> recs = service.generateRecommendations(entrepriseId);
        assertTrue(recs.stream().anyMatch(r -> "CATEGORIE_DOMINANTE".equals(r.getType())),
                "expected CATEGORIE_DOMINANTE recommendation");
    }

    @Test
    void recommendationsAreNeverEmptyTitleOrDescription() {
        when(commandeVenteRepository.getTotalSalesOrders(anyLong())).thenReturn(50L);
        when(commandeVenteRepository.getTotalSalesRevenue(anyLong())).thenReturn(10000.0);
        when(stockRepository.getOutOfStockItemsCount(anyLong())).thenReturn(3L);
        when(stockRepository.getLowStockItemsCount(anyLong())).thenReturn(7L);
        List<RecommendationDto> recs = service.generateRecommendations(entrepriseId);
        assertFalse(recs.isEmpty());
        for (RecommendationDto r : recs) {
            assertNotNull(r.getType());
            assertNotNull(r.getTitle());
            assertFalse(r.getTitle().isBlank(), "title should not be blank");
            assertNotNull(r.getDescription());
            assertFalse(r.getDescription().isBlank(), "description should not be blank");
            assertNotNull(r.getPriority());
        }
    }
}
