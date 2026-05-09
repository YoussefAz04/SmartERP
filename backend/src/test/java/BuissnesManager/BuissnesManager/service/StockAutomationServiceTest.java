package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.StockAlertDto;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StockAutomationService}. The service contains the
 * non-trivial reception/delivery/reservation/cancellation logic that mutates
 * stock counters and emits movements + notifications, so it gets dedicated
 * coverage even without spinning up a Spring context.
 */
class StockAutomationServiceTest {

    @Mock
    private StockRepository stockRepository;
    @Mock
    private StockMouvementRepo stockMouvementRepo;
    @Mock
    private LigneCommandeAchatRepository ligneCommandeAchatRepository;
    @Mock
    private LigneCommandeVenteRepository ligneCommandeVenteRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    @InjectMocks
    private StockAutomationService service;

    private Article article;
    private ArticleVente articleVente;
    private Stock stock;
    private Entreprise entreprise;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(enterpriseSecurityUtil.getCurrentUser()).thenReturn(Optional.empty());

        entreprise = new Entreprise();
        entreprise.setId(1L);

        article = new Article();
        article.setId(10L);
        article.setNom("Widget");
        article.setEntreprise(entreprise);

        articleVente = new ArticleVente();
        articleVente.setId(11L);
        articleVente.setArticleAchat(article);

        stock = new Stock();
        stock.setId(100L);
        stock.setArticleAchat(article);
        stock.setEntreprise(entreprise);
        stock.setQuantiteDisponible(BigDecimal.valueOf(50));
        stock.setQuantiteEntree(BigDecimal.valueOf(50));
        stock.setQuantiteReserve(BigDecimal.ZERO);
        stock.setQuantiteVendue(BigDecimal.ZERO);
        stock.setSeuilMinimum(BigDecimal.valueOf(5));
    }

    private LigneCommandeAchat ligneAchat(BigDecimal qty) {
        LigneCommandeAchat l = new LigneCommandeAchat();
        l.setQuantite(qty);
        l.setArticle(article);
        return l;
    }

    private LigneCommandeVente ligneVente(BigDecimal qty) {
        LigneCommandeVente l = new LigneCommandeVente();
        l.setQuantite(qty);
        l.setArticleVente(articleVente);
        return l;
    }

    @Test
    void incrementStockFromReceptionAddsToDisponibleAndEmitsEntreeMovement() {
        when(ligneCommandeAchatRepository.findByCommandeAchatId(1L))
                .thenReturn(List.of(ligneAchat(BigDecimal.valueOf(20))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        service.incrementStockFromReception(1L, "REF-001");

        assertEquals(0, BigDecimal.valueOf(70).compareTo(stock.getQuantiteDisponible()));
        assertEquals(0, BigDecimal.valueOf(70).compareTo(stock.getQuantiteEntree()));

        ArgumentCaptor<StockMouvement> captor = ArgumentCaptor.forClass(StockMouvement.class);
        verify(stockMouvementRepo).save(captor.capture());
        assertEquals(TypeMouvement.ENTREE, captor.getValue().getTypeMouvement());
        assertEquals(20, captor.getValue().getQuantite());
        assertEquals("REF-001", captor.getValue().getReferenceCommande());

        verify(notificationService).createStockAddedNotification(eq(stock), eq(article), eq(entreprise), eq(BigDecimal.valueOf(20)));
    }

    @Test
    void incrementStockSkipsZeroOrNegativeQuantities() {
        when(ligneCommandeAchatRepository.findByCommandeAchatId(1L))
                .thenReturn(List.of(ligneAchat(BigDecimal.ZERO), ligneAchat(BigDecimal.valueOf(-3))));

        service.incrementStockFromReception(1L, "REF-NEG");

        verify(stockRepository, never()).save(any());
        verify(stockMouvementRepo, never()).save(any());
    }

    @Test
    void decrementStockFromDeliveryConsumesReservationFirst() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(20));
        stock.setQuantiteReserve(BigDecimal.valueOf(15));
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(10))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        List<StockAlertDto> alerts = service.decrementStockFromDelivery(1L, "CV-1");

        // Reserve covers it fully, disponible is untouched.
        assertEquals(0, BigDecimal.valueOf(5).compareTo(stock.getQuantiteReserve()));
        assertEquals(0, BigDecimal.valueOf(20).compareTo(stock.getQuantiteDisponible()));
        assertEquals(0, BigDecimal.valueOf(10).compareTo(stock.getQuantiteVendue()));
        assertTrue(alerts.isEmpty(), "no alert when stock stays well above seuil");
    }

    @Test
    void decrementStockFromDeliverySpillsToDisponibleWhenReserveTooSmall() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(20));
        stock.setQuantiteReserve(BigDecimal.valueOf(3));
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(10))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        service.decrementStockFromDelivery(1L, "CV-2");

        // Reserve drained fully, remaining 7 taken from disponible (20 -> 13).
        assertEquals(0, BigDecimal.ZERO.compareTo(stock.getQuantiteReserve()));
        assertEquals(0, BigDecimal.valueOf(13).compareTo(stock.getQuantiteDisponible()));
    }

    @Test
    void decrementStockFromDeliveryRaisesRuptureAlertWhenStockHitsZero() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(10));
        stock.setQuantiteReserve(BigDecimal.ZERO);
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(10))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        List<StockAlertDto> alerts = service.decrementStockFromDelivery(1L, "CV-3");

        assertEquals(1, alerts.size());
        assertEquals("RUPTURE", alerts.get(0).getType());
        assertEquals(article.getId(), alerts.get(0).getArticleId());
    }

    @Test
    void decrementStockFromDeliveryRaisesFaibleStockAlertBelowSeuil() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(10));
        stock.setSeuilMinimum(BigDecimal.valueOf(8));
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(5))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        List<StockAlertDto> alerts = service.decrementStockFromDelivery(1L, "CV-4");

        assertEquals(1, alerts.size());
        assertEquals("FAIBLE_STOCK", alerts.get(0).getType());
    }

    @Test
    void decrementStockFromDeliveryThrowsWhenInsufficient() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(2));
        stock.setQuantiteReserve(BigDecimal.valueOf(1));
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(10))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        assertThrows(RuntimeException.class,
                () -> service.decrementStockFromDelivery(1L, "CV-OOM"));
        verify(stockRepository, never()).save(any());
    }

    @Test
    void reserverStockMovesQuantityFromDisponibleToReserve() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(50));
        stock.setQuantiteReserve(BigDecimal.valueOf(0));
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(12))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        service.reserverStockPourCommande(1L, "CV-RES");

        assertEquals(0, BigDecimal.valueOf(38).compareTo(stock.getQuantiteDisponible()));
        assertEquals(0, BigDecimal.valueOf(12).compareTo(stock.getQuantiteReserve()));

        ArgumentCaptor<StockMouvement> captor = ArgumentCaptor.forClass(StockMouvement.class);
        verify(stockMouvementRepo).save(captor.capture());
        assertEquals(TypeMouvement.RESERVATION, captor.getValue().getTypeMouvement());
    }

    @Test
    void reserverStockThrowsWhenNotEnoughDisponible() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(3));
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(10))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        assertThrows(RuntimeException.class,
                () -> service.reserverStockPourCommande(1L, "CV-RES-FAIL"));
    }

    @Test
    void annulerReservationStockReleasesUpToReservedAmount() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(30));
        stock.setQuantiteReserve(BigDecimal.valueOf(15));
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(20)))); // tries to release more than reserved
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        service.annulerReservationStock(1L, "CV-CANCEL");

        // Capped at the reserved amount (15) — disponible gets +15, reserve goes to 0.
        assertEquals(0, BigDecimal.ZERO.compareTo(stock.getQuantiteReserve()));
        assertEquals(0, BigDecimal.valueOf(45).compareTo(stock.getQuantiteDisponible()));
    }

    @Test
    void annulerStockDepuisReceptionDecrementsAndRecordsNegativeMovement() {
        stock.setQuantiteDisponible(BigDecimal.valueOf(50));
        stock.setQuantiteEntree(BigDecimal.valueOf(50));
        when(ligneCommandeAchatRepository.findByCommandeAchatId(1L))
                .thenReturn(List.of(ligneAchat(BigDecimal.valueOf(20))));
        when(stockRepository.findByArticleAchatId(article.getId()))
                .thenReturn(List.of(stock));

        service.annulerStockDepuisReception(1L, "CA-CANCEL");

        assertEquals(0, BigDecimal.valueOf(30).compareTo(stock.getQuantiteDisponible()));
        assertEquals(0, BigDecimal.valueOf(30).compareTo(stock.getQuantiteEntree()));

        ArgumentCaptor<StockMouvement> captor = ArgumentCaptor.forClass(StockMouvement.class);
        verify(stockMouvementRepo).save(captor.capture());
        assertEquals(TypeMouvement.SORTIE, captor.getValue().getTypeMouvement());
        assertTrue(captor.getValue().getQuantite() < 0,
                "cancellation movements must use a negative quantity to balance ledger");
    }

    @Test
    void incrementStockSkipsWhenNoLigneCommande() {
        when(ligneCommandeAchatRepository.findByCommandeAchatId(1L))
                .thenReturn(Collections.emptyList());

        service.incrementStockFromReception(1L, "REF-EMPTY");

        verify(stockRepository, never()).save(any());
        verify(stockMouvementRepo, never()).save(any());
    }

    @Test
    void decrementStockSkipsArticleWithNoStockRecord() {
        when(ligneCommandeVenteRepository.findByCommandeVenteId(1L))
                .thenReturn(List.of(ligneVente(BigDecimal.valueOf(5))));
        when(stockRepository.findByArticleAchatId(anyLong())).thenReturn(Collections.emptyList());

        List<StockAlertDto> alerts = service.decrementStockFromDelivery(1L, "CV-NO-STOCK");

        assertTrue(alerts.isEmpty());
        verify(stockRepository, never()).save(any());
    }
}
