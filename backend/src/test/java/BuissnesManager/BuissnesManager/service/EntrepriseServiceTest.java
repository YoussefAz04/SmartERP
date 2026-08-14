package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link EntrepriseService} focused on the soft-delete pair
 * ({@link EntrepriseService#disableEntreprise(Long)} /
 *  {@link EntrepriseService#enableEntreprise(Long)}). These flows mark the
 * entreprise inactive and propagate the change to all attached users — a
 * silent regression here means orphaned active users on a closed company,
 * which is exactly the kind of bug the suite must catch.
 */
class EntrepriseServiceTest {

    @Mock private EntrepriseRepo entrepriseRepository;
    @Mock private UserRepo userRepo;
    @Mock private CaracteristiqueFactureEntrepriseRepo caracteristiqueFactureEntrepriseRepo;
    @Mock private PredictionLogRepository predictionLogRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private StockMouvementRepo stockMouvementRepo;
    @Mock private LigneFactureAchatRepository ligneFactureAchatRepository;
    @Mock private LigneFactureVenteRepository ligneFactureVenteRepository;
    @Mock private DetaisFactureAchatRepo detaisFactureAchatRepo;
    @Mock private DetaisFactureDeVenteRepo detaisFactureDeVenteRepo;
    @Mock private FactureAchatRepo factureAchatRepo;
    @Mock private FactureDeVenteRepo factureDeVenteRepo;
    @Mock private FactureVenteRepository factureVenteRepository;
    @Mock private BonReceptionRepository bonReceptionRepository;
    @Mock private BonLivraisonRepository bonLivraisonRepository;
    @Mock private LigneCommandeAchatRepository ligneCommandeAchatRepository;
    @Mock private LigneCommandeVenteRepository ligneCommandeVenteRepository;
    @Mock private CommandeAchatRepository commandeAchatRepository;
    @Mock private CommandeVenteRepository commandeVenteRepository;
    @Mock private ArticleVenteRepo articleVenteRepo;
    @Mock private StockRepository stockRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private PermissionRepo permissionRepo;
    @Mock private RoleRepo roleRepo;
    @Mock private FournisseurRepo fournisseurRepo;
    @Mock private ClientRepo clientRepo;
    @Mock private CategorieArticleRepository categorieArticleRepository;
    @Mock private MesureRepository mesureRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private EntrepriseService service;

    private Entreprise entreprise;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        entreprise = new Entreprise();
        entreprise.setId(42L);
        entreprise.setActive(true);
    }

    @Test
    void disableEntrepriseMarksEntrepriseInactive() {
        when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(entreprise));
        when(userRepo.findByEntrepriseId(42L)).thenReturn(List.of());

        service.disableEntreprise(42L);

        assertFalse(entreprise.isActive(), "active flag must be flipped to false");
        verify(entrepriseRepository).save(entreprise);
    }

    @Test
    void disableEntrepriseDisablesAllAttachedUsers() {
        User u1 = new User();
        u1.setId(1L);
        u1.setEnabled(true);
        User u2 = new User();
        u2.setId(2L);
        u2.setEnabled(true);

        when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(entreprise));
        when(userRepo.findByEntrepriseId(42L)).thenReturn(List.of(u1, u2));

        service.disableEntreprise(42L);

        assertFalse(u1.isEnabled());
        assertFalse(u2.isEnabled());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRepo).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void disableEntrepriseThrowsWhenEntrepriseNotFound() {
        when(entrepriseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmptyResultDataAccessException.class,
                () -> service.disableEntreprise(99L));
        verify(entrepriseRepository, never()).save(any());
        verify(userRepo, never()).saveAll(any());
    }

    @Test
    void enableEntrepriseRestoresEntrepriseAndUsers() {
        entreprise.setActive(false);
        User u1 = new User();
        u1.setId(1L);
        u1.setEnabled(false);

        when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(entreprise));
        when(userRepo.findByEntrepriseId(42L)).thenReturn(List.of(u1));

        service.enableEntreprise(42L);

        assertTrue(entreprise.isActive());
        assertTrue(u1.isEnabled());
        verify(entrepriseRepository).save(entreprise);
        verify(userRepo).saveAll(anyList());
    }

    @Test
    void enableEntrepriseThrowsWhenEntrepriseNotFound() {
        when(entrepriseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmptyResultDataAccessException.class,
                () -> service.enableEntreprise(99L));
    }

    @Test
    void disableThenEnableRoundTripsActiveFlagsCleanly() {
        User u = new User();
        u.setId(1L);
        u.setEnabled(true);

        when(entrepriseRepository.findById(42L)).thenReturn(Optional.of(entreprise));
        when(userRepo.findByEntrepriseId(42L)).thenReturn(List.of(u));

        service.disableEntreprise(42L);
        assertFalse(entreprise.isActive());
        assertFalse(u.isEnabled());

        service.enableEntreprise(42L);
        assertTrue(entreprise.isActive());
        assertTrue(u.isEnabled());

        verify(entrepriseRepository, times(2)).save(entreprise);
        verify(userRepo, times(2)).saveAll(anyList());
    }
}
