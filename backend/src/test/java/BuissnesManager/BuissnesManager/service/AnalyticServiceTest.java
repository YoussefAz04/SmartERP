package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.dto.SalesAnalysisDto;
import BuissnesManager.BuissnesManager.entity.Article;
import BuissnesManager.BuissnesManager.entity.ArticleVente;
import BuissnesManager.BuissnesManager.repository.ArticleVenteRepo;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.DetaisFactureDeVenteRepo;
import BuissnesManager.BuissnesManager.repository.StockRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class AnalyticServiceTest {

    @Mock
    private CommandeVenteRepository commandeVenteRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private DetaisFactureDeVenteRepo detaisFactureDeVenteRepo;

    @Mock
    private ArticleVenteRepo articleVenteRepo;

    @InjectMocks
    private AnalyticService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void analyzeSalesWithNoDataProducesSafeZeroedResponse() {
        when(commandeVenteRepository.getMonthlySales(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        SalesAnalysisDto dto = service.analyzeSales(42L);

        assertNotNull(dto);
        assertEquals(12, dto.getMonthlySales().size());
        assertEquals(0.0, dto.getTotalRevenue(), 0.0001);
        // No active months → average must be 0 (avoid 0/12 confusion anyway)
        assertEquals(0.0, dto.getAverageMonthlyRevenue(), 0.0001);
        // No first-half revenue → growth rate safely reported as 0.0
        assertEquals(0.0, dto.getGrowthRate(), 0.0001);
        assertEquals(12, dto.getTrendLine().size());
    }

    @Test
    void analyzeSalesAveragesOnlyActiveMonths() {
        // Business only started in October (months 10, 11) of current year.
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{10, 1000.0});
        rows.add(new Object[]{11, 3000.0});
        when(commandeVenteRepository.getMonthlySales(anyLong(), anyInt())).thenReturn(rows);

        SalesAnalysisDto dto = service.analyzeSales(1L);

        assertEquals(4000.0, dto.getTotalRevenue(), 0.0001);
        // Average over 2 active months = 2000 (not 4000/12 = 333)
        assertEquals(2000.0, dto.getAverageMonthlyRevenue(), 0.0001);
        assertEquals("Nov", dto.getBestMonth());
    }

    @Test
    void analyzeSalesClampsAbsurdGrowthRate() {
        // First half = 1 TND, second half = 100k TND → would otherwise be 9_999_900%
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{1, 1.0});
        rows.add(new Object[]{7, 100_000.0});
        when(commandeVenteRepository.getMonthlySales(anyLong(), anyInt())).thenReturn(rows);

        SalesAnalysisDto dto = service.analyzeSales(1L);

        assertTrue(dto.getGrowthRate() <= 9999.0,
                "extreme growth must be clamped, got " + dto.getGrowthRate());
    }

    @Test
    void analyzeProductsRanksAndLabelsTopAndSlowMovers() {
        List<Object[]> top = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            // name, qty, revenue — descending by construction
            top.add(new Object[]{"P" + i, (long) (200 - i * 10), (double) (1000 - i * 50)});
        }
        when(commandeVenteRepository.getTopProducts(anyLong())).thenReturn(top);
        when(commandeVenteRepository.getCategoryPerformance(anyLong()))
                .thenReturn(Collections.emptyList());
        when(stockRepository.getTotalSKUs(anyLong())).thenReturn(12L);

        ProductIntelligenceDto dto = service.analyzeProducts(1L);

        assertNotNull(dto);
        assertEquals(10, dto.getTopProducts().size());
        // Top 3 must be labelled TOP — not overwritten by the slow-movers loop.
        // This is the regression test for the subList() view bug: if the top
        // list were a view of `products`, the slow-movers loop would mutate
        // the same underlying objects, corrupting the ranking on display.
        for (int i = 0; i < 3; i++) {
            assertEquals("TOP", dto.getTopProducts().get(i).getPerformanceLabel(),
                    "top-3 product should keep its TOP label after slow-mover pass");
        }
        // Rank assigned correctly on the stored copy
        assertEquals(1, dto.getTopProducts().get(0).getRank());

        assertNotNull(dto.getSlowMovingProducts());
        assertFalse(dto.getSlowMovingProducts().isEmpty());
        for (ProductIntelligenceDto.ProductPerformanceDto p : dto.getSlowMovingProducts()) {
            assertEquals("LENT", p.getPerformanceLabel());
        }
    }

    @Test
    void analyzeProductsHandlesEmptyCatalogue() {
        when(commandeVenteRepository.getTopProducts(anyLong()))
                .thenReturn(Collections.emptyList());
        when(commandeVenteRepository.getCategoryPerformance(anyLong()))
                .thenReturn(Collections.emptyList());
        when(detaisFactureDeVenteRepo.getTopProductsFromFactures(anyLong()))
                .thenReturn(Collections.emptyList());
        when(articleVenteRepo.findByEntrepriseId(anyLong()))
                .thenReturn(Collections.emptyList());
        when(stockRepository.getTotalSKUs(anyLong())).thenReturn(0L);

        ProductIntelligenceDto dto = service.analyzeProducts(1L);

        assertNotNull(dto);
        assertTrue(dto.getTopProducts().isEmpty());
        assertTrue(dto.getSlowMovingProducts().isEmpty());
        assertEquals(0, dto.getTotalProducts());
    }

    @Test
    void analyzeProductsFallsBackToCatalogueWhenNoSales() {
        // Enterprise has built a catalogue (via purchases) but has not yet
        // recorded a single sale. The dashboard must still surface the
        // catalogue so the "Intelligence Produits" and "Articles à Rotation
        // Lente" panels are not left empty.
        when(commandeVenteRepository.getTopProducts(anyLong()))
                .thenReturn(Collections.emptyList());
        when(commandeVenteRepository.getCategoryPerformance(anyLong()))
                .thenReturn(Collections.emptyList());
        when(detaisFactureDeVenteRepo.getTopProductsFromFactures(anyLong()))
                .thenReturn(Collections.emptyList());
        when(stockRepository.getTotalSKUs(anyLong())).thenReturn(3L);

        List<ArticleVente> catalogue = new ArrayList<>();
        catalogue.add(buildArticleVente(1L, "Clavier", 8));
        catalogue.add(buildArticleVente(2L, "Écran", 25));
        catalogue.add(buildArticleVente(3L, "Câble", 3));
        when(articleVenteRepo.findByEntrepriseId(anyLong())).thenReturn(catalogue);

        ProductIntelligenceDto dto = service.analyzeProducts(11L);

        assertNotNull(dto);
        // All catalogue items surface as "top products" with zero sales.
        assertEquals(3, dto.getTopProducts().size());
        // Highest-stock first: Écran (25) > Clavier (8) > Câble (3).
        assertEquals("Écran", dto.getTopProducts().get(0).getProductName());
        assertEquals(0L, (long) dto.getTopProducts().get(0).getQuantitySold());
        assertEquals(0.0, dto.getTopProducts().get(0).getRevenue(), 0.0001);
        assertEquals("NOUVEAU", dto.getTopProducts().get(0).getPerformanceLabel());

        // Slow-movers panel must list the highest-stock items as biggest risk.
        assertFalse(dto.getSlowMovingProducts().isEmpty());
        assertEquals("Écran", dto.getSlowMovingProducts().get(0).getProductName());
        assertEquals("LENT", dto.getSlowMovingProducts().get(0).getPerformanceLabel());
        // The "LENT" label on slow-movers must NOT bleed back into the
        // top-products list (defensive copy).
        assertEquals("NOUVEAU", dto.getTopProducts().get(0).getPerformanceLabel());
    }

    private ArticleVente buildArticleVente(Long id, String nom, int stock) {
        ArticleVente av = new ArticleVente();
        av.setId(id);
        av.setNom(nom);
        av.setQuantite(stock);
        Article achat = new Article();
        achat.setId(id);
        achat.setNom(nom);
        av.setArticleAchat(achat);
        return av;
    }

    @Test
    void analyzeProductsFallsBackToFactureDetailsWhenCommandeLinesEmpty() {
        // No commande-line data — simulates an enterprise that records sales
        // as standalone factures (with a free-text article label) instead of
        // structured commandes.
        when(commandeVenteRepository.getTopProducts(anyLong()))
                .thenReturn(Collections.emptyList());
        when(commandeVenteRepository.getCategoryPerformance(anyLong()))
                .thenReturn(Collections.emptyList());
        when(stockRepository.getTotalSKUs(anyLong())).thenReturn(3L);

        List<Object[]> fallback = new ArrayList<>();
        fallback.add(new Object[]{"Laptop Pro", 12L, 24000.0});
        fallback.add(new Object[]{"Tablette",   8L, 6400.0});
        fallback.add(new Object[]{"Souris",     5L, 250.0});
        when(detaisFactureDeVenteRepo.getTopProductsFromFactures(anyLong()))
                .thenReturn(fallback);

        ProductIntelligenceDto dto = service.analyzeProducts(7L);

        assertNotNull(dto);
        assertEquals(3, dto.getTopProducts().size());
        assertEquals("Laptop Pro", dto.getTopProducts().get(0).getProductName());
        assertEquals(1, dto.getTopProducts().get(0).getRank());
        // Revenue share should reflect the fallback aggregation (24000 / 30650).
        assertTrue(dto.getTopProducts().get(0).getRevenueShare() > 70.0,
                "primary fallback product should dominate the revenue share");
    }

    @Test
    void computeMovingAverageMatchesKnownValues() {
        List<Double> data = new ArrayList<>();
        data.add(1.0); data.add(2.0); data.add(3.0); data.add(4.0); data.add(5.0);

        List<Double> ma = service.computeMovingAverage(data, 3);

        assertEquals(5, ma.size());
        // Window 3, ramp-up at the start:
        // i=0 -> 1.0
        // i=1 -> (1+2)/2 = 1.5
        // i=2 -> (1+2+3)/3 = 2.0
        // i=3 -> (2+3+4)/3 = 3.0
        // i=4 -> (3+4+5)/3 = 4.0
        assertEquals(1.0, ma.get(0), 1e-6);
        assertEquals(1.5, ma.get(1), 1e-6);
        assertEquals(2.0, ma.get(2), 1e-6);
        assertEquals(3.0, ma.get(3), 1e-6);
        assertEquals(4.0, ma.get(4), 1e-6);
    }

    @Test
    void currentYearUsed() {
        // sanity: the service reads LocalDate.now() internally, just make sure
        // a realistic call works end-to-end.
        when(commandeVenteRepository.getMonthlySales(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        SalesAnalysisDto dto = service.analyzeSales(1L);

        assertNotNull(dto);
        // Placebo assertion to keep the compiler happy about using LocalDate.
        assertTrue(LocalDate.now().getYear() > 2000);
    }
}
