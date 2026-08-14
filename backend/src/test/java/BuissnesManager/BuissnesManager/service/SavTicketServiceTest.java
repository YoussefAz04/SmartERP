package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.SavTicketRequest;
import BuissnesManager.BuissnesManager.dto.SavTicketResponse;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.SavApprovalStatus;
import BuissnesManager.BuissnesManager.entity.SavPriority;
import BuissnesManager.BuissnesManager.entity.SavStatus;
import BuissnesManager.BuissnesManager.entity.SavTicket;
import BuissnesManager.BuissnesManager.entity.SavType;
import BuissnesManager.BuissnesManager.repository.ArticleVenteRepo;
import BuissnesManager.BuissnesManager.repository.ClientRepo;
import BuissnesManager.BuissnesManager.repository.CommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.LigneCommandeVenteRepository;
import BuissnesManager.BuissnesManager.repository.SavInterventionRepository;
import BuissnesManager.BuissnesManager.repository.SavTicketHistoryRepository;
import BuissnesManager.BuissnesManager.repository.SavTicketRepository;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavTicketServiceTest {

    private static final long ENTERPRISE_ID = 10L;

    @Mock private SavTicketRepository savTicketRepository;
    @Mock private SavTicketHistoryRepository savTicketHistoryRepository;
    @Mock private SavInterventionRepository savInterventionRepository;
    @Mock private EntrepriseRepo entrepriseRepo;
    @Mock private ClientRepo clientRepo;
    @Mock private ArticleVenteRepo articleVenteRepo;
    @Mock private CommandeVenteRepository commandeVenteRepository;
    @Mock private LigneCommandeVenteRepository ligneCommandeVenteRepository;
    @Mock private UserRepo userRepo;
    @Mock private EnterpriseSecurityUtil enterpriseSecurityUtil;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private SavTicketService service;

    private Entreprise entreprise;

    @BeforeEach
    void setUp() {
        entreprise = new Entreprise();
        entreprise.setId(ENTERPRISE_ID);
        when(enterpriseSecurityUtil.getContextEnterpriseId()).thenReturn(ENTERPRISE_ID);
        lenient().when(savTicketHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createTicketIgnoresClientManagedWorkflowAndCostFields() {
        SavTicketRequest request = baseRequest();
        request.setStatus(SavStatus.FERME);
        request.setActualCost(new BigDecimal("999.99"));
        request.setApprovalStatus(SavApprovalStatus.ACCEPTE);

        when(entrepriseRepo.findById(ENTERPRISE_ID)).thenReturn(Optional.of(entreprise));
        when(savTicketRepository.save(any())).thenAnswer(invocation -> {
            SavTicket ticket = invocation.getArgument(0);
            ticket.setId(1L);
            return ticket;
        });

        SavTicketResponse response = service.createTicket(request);

        assertEquals(SavStatus.OUVERT, response.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getActualCost()));
        assertEquals(SavApprovalStatus.NON_REQUIS, response.getApprovalStatus());
    }

    @Test
    void updateTicketPreservesActualCostAndApprovalDecision() {
        SavTicket ticket = existingTicket(SavStatus.EN_REPARATION);
        ticket.setDiagnostic("Power supply replaced");
        ticket.setEstimatedCost(new BigDecimal("120.00"));
        ticket.setActualCost(new BigDecimal("45.00"));
        ticket.setApprovalStatus(SavApprovalStatus.ACCEPTE);

        SavTicketRequest request = baseRequest();
        request.setStatus(SavStatus.EN_REPARATION);
        request.setType(SavType.REPARATION);
        request.setDiagnostic("Power supply replaced");
        request.setEstimatedCost(new BigDecimal("120.00"));
        request.setActualCost(new BigDecimal("900.00"));
        request.setApprovalStatus(SavApprovalStatus.REFUSE);

        when(savTicketRepository.findByIdAndEntrepriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.of(ticket));
        when(savTicketRepository.save(ticket)).thenReturn(ticket);

        SavTicketResponse response = service.updateTicket(1L, request);

        assertEquals(0, new BigDecimal("45.00").compareTo(response.getActualCost()));
        assertEquals(SavApprovalStatus.ACCEPTE, response.getApprovalStatus());
    }

    @Test
    void customerCannotApproveRepairAfterDiagnosisWasRemoved() {
        SavTicket ticket = existingTicket(SavStatus.EN_ATTENTE_ACCORD);
        ticket.setEstimatedCost(new BigDecimal("120.00"));
        ticket.setApprovalStatus(SavApprovalStatus.EN_ATTENTE);
        when(savTicketRepository.findByIdAndEntrepriseId(1L, ENTERPRISE_ID)).thenReturn(Optional.of(ticket));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.updateApproval(1L, SavApprovalStatus.ACCEPTE));

        assertEquals("Diagnostic is required before repair", error.getMessage());
        verify(savTicketRepository, never()).save(any());
    }

    private SavTicketRequest baseRequest() {
        SavTicketRequest request = new SavTicketRequest();
        request.setSubject("Laptop does not start");
        request.setDescription("Customer reports a power issue");
        request.setPriority(SavPriority.NORMALE);
        request.setType(SavType.SUPPORT);
        return request;
    }

    private SavTicket existingTicket(SavStatus status) {
        SavTicket ticket = new SavTicket();
        ticket.setId(1L);
        ticket.setReference("SAV-TEST-001");
        ticket.setSubject("Laptop does not start");
        ticket.setDescription("Customer reports a power issue");
        ticket.setStatus(status);
        ticket.setPriority(SavPriority.NORMALE);
        ticket.setType(SavType.REPARATION);
        ticket.setEntreprise(entreprise);
        return ticket;
    }
}
