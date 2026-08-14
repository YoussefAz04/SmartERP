package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.SavStatsDto;
import BuissnesManager.BuissnesManager.dto.SavTicketRequest;
import BuissnesManager.BuissnesManager.dto.SavTicketResponse;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.math.BigDecimal;
import BuissnesManager.BuissnesManager.dto.SavAgentResponse;
import BuissnesManager.BuissnesManager.dto.SavHistoryResponse;
import BuissnesManager.BuissnesManager.dto.SavInterventionRequest;
import BuissnesManager.BuissnesManager.dto.SavInterventionResponse;
import BuissnesManager.BuissnesManager.dto.SavSaleItemResponse;

@Service
public class SavTicketService {

    @Autowired
    private SavTicketRepository savTicketRepository;

    @Autowired
    private SavTicketHistoryRepository savTicketHistoryRepository;

    @Autowired
    private SavInterventionRepository savInterventionRepository;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private ArticleVenteRepo articleVenteRepo;

    @Autowired
    private CommandeVenteRepository commandeVenteRepository;

    @Autowired
    private LigneCommandeVenteRepository ligneCommandeVenteRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    @Autowired
    private AuditLogService auditLogService;

    public List<SavTicketResponse> getAllTickets() {
        Long entrepriseId = requireEnterpriseId();
        return savTicketRepository.findByEntrepriseIdOrderByCreatedAtDesc(entrepriseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SavTicketResponse getTicket(Long id) {
        return toResponse(findTicketForCurrentEnterprise(id));
    }

    public SavStatsDto getStats() {
        Long entrepriseId = requireEnterpriseId();
        List<SavStatus> terminalStatuses = terminalStatuses();
        return new SavStatsDto(
                savTicketRepository.countByEntrepriseId(entrepriseId),
                savTicketRepository.countByEntrepriseIdAndStatus(entrepriseId, SavStatus.OUVERT),
                savTicketRepository.countByEntrepriseIdAndStatusIn(entrepriseId, activeProcessingStatuses()),
                savTicketRepository.countByEntrepriseIdAndStatus(entrepriseId, SavStatus.EN_ATTENTE_CLIENT),
                savTicketRepository.countByEntrepriseIdAndStatus(entrepriseId, SavStatus.RESOLU),
                savTicketRepository.countByEntrepriseIdAndStatus(entrepriseId, SavStatus.FERME),
                savTicketRepository.countByEntrepriseIdAndStatus(entrepriseId, SavStatus.ANNULE),
                savTicketRepository.countByEntrepriseIdAndDueDateBeforeAndStatusNotIn(
                        entrepriseId, LocalDate.now(), terminalStatuses),
                savTicketRepository.countByEntrepriseIdAndAssignedToIsNullAndStatusNotIn(
                        entrepriseId, terminalStatuses),
                savTicketRepository.sumActualCostByEntrepriseId(entrepriseId)
        );
    }

    public List<SavAgentResponse> getAgents() {
        return userRepo.findByEntrepriseId(requireEnterpriseId()).stream()
                .filter(User::isEnabled)
                .map(user -> new SavAgentResponse(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SavSaleItemResponse> getEligibleSales(Long clientId) {
        Long entrepriseId = requireEnterpriseId();
        resolveClient(clientId, entrepriseId);
        return commandeVenteRepository.findByClientId(clientId).stream()
                .filter(order -> order.getEntreprise() != null && entrepriseId.equals(order.getEntreprise().getId()))
                .filter(this::isEligibleSale)
                .flatMap(order -> ligneCommandeVenteRepository.findByCommandeVenteId(order.getId()).stream()
                        .filter(line -> line.getArticleVente() != null)
                        .map(line -> SavSaleItemResponse.builder()
                                .orderId(order.getId())
                                .orderReference(order.getReference())
                                .orderDate(order.getDateCommande())
                                .orderStatus(order.getStatut())
                                .articleVenteId(line.getArticleVente().getId())
                                .articleName(line.getArticleVente().getNom())
                                .sku(line.getArticleVente().getSku())
                                .quantity(line.getQuantite())
                                .build()))
                .toList();
    }

    public List<SavInterventionResponse> getInterventions(Long ticketId) {
        findTicketForCurrentEnterprise(ticketId);
        return savInterventionRepository.findByTicketIdOrderByPerformedAtDesc(ticketId).stream()
                .map(this::toInterventionResponse)
                .toList();
    }

    @Transactional
    public SavInterventionResponse addIntervention(Long ticketId, SavInterventionRequest request) {
        SavTicket ticket = findTicketForCurrentEnterprise(ticketId);
        if (ticket.getStatus() != SavStatus.DIAGNOSTIC && ticket.getStatus() != SavStatus.EN_REPARATION
                && ticket.getStatus() != SavStatus.EN_COURS) {
            throw new IllegalArgumentException("Interventions can only be logged during diagnosis or repair");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Intervention description is required");
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Intervention duration must be positive");
        }
        validateNonNegative(request.getLaborCost(), "Labor cost");
        validateNonNegative(request.getPartsCost(), "Parts cost");

        SavIntervention intervention = new SavIntervention();
        intervention.setTicket(ticket);
        intervention.setDescription(request.getDescription().trim());
        intervention.setDurationMinutes(request.getDurationMinutes());
        intervention.setLaborCost(zeroIfNull(request.getLaborCost()));
        intervention.setPartsCost(zeroIfNull(request.getPartsCost()));
        intervention.setTechnician(request.getTechnicianUserId() != null
                ? resolveUser(request.getTechnicianUserId(), requireEnterpriseId())
                : ticket.getAssignedTo() != null ? ticket.getAssignedTo() : enterpriseSecurityUtil.getCurrentUser().orElse(null));

        SavIntervention saved = savInterventionRepository.save(intervention);
        recalculateActualCost(ticket);
        recordHistory(ticket, SavHistoryType.INTERVENTION,
                "Intervention logged: " + saved.getDurationMinutes() + " min - " + saved.getDescription());
        auditLogService.log("SAV_INTERVENTION_ADDED", "Intervention added to " + ticket.getReference());
        return toInterventionResponse(saved);
    }

    @Transactional
    public void deleteIntervention(Long ticketId, Long interventionId) {
        SavTicket ticket = findTicketForCurrentEnterprise(ticketId);
        SavIntervention intervention = savInterventionRepository.findByIdAndTicketId(interventionId, ticketId)
                .orElseThrow(() -> new RuntimeException("SAV intervention not found: " + interventionId));
        savInterventionRepository.delete(intervention);
        savInterventionRepository.flush();
        recalculateActualCost(ticket);
        recordHistory(ticket, SavHistoryType.INTERVENTION, "Intervention removed");
        auditLogService.log("SAV_INTERVENTION_DELETED", "Intervention removed from " + ticket.getReference());
    }

    @Transactional
    public SavTicketResponse updateApproval(Long ticketId, SavApprovalStatus approvalStatus) {
        if (approvalStatus != SavApprovalStatus.ACCEPTE && approvalStatus != SavApprovalStatus.REFUSE) {
            throw new IllegalArgumentException("Customer approval must be accepted or refused");
        }
        SavTicket ticket = findTicketForCurrentEnterprise(ticketId);
        if (ticket.getStatus() != SavStatus.EN_ATTENTE_ACCORD) {
            throw new IllegalArgumentException("Ticket is not waiting for customer approval");
        }
        ticket.setApprovalStatus(approvalStatus);
        ticket.setCustomerApprovedAt(LocalDateTime.now());
        SavStatus previousStatus = ticket.getStatus();
        SavStatus targetStatus = approvalStatus == SavApprovalStatus.ACCEPTE
                ? SavStatus.EN_REPARATION
                : SavStatus.ANNULE;
        validateTransition(previousStatus, targetStatus);
        prepareAndValidateStage(ticket, previousStatus, targetStatus);
        ticket.setStatus(targetStatus);
        updateClosedAt(ticket, previousStatus);
        SavTicket saved = savTicketRepository.save(ticket);
        recordHistory(saved, SavHistoryType.APPROVAL,
                "Customer " + (approvalStatus == SavApprovalStatus.ACCEPTE ? "accepted" : "refused") + " the estimate");
        recordHistory(saved, SavHistoryType.STATUS_CHANGED,
                "Status changed from " + previousStatus + " to " + saved.getStatus());
        auditLogService.log("SAV_CUSTOMER_DECISION",
                "Customer decision for " + saved.getReference() + ": " + approvalStatus);
        return toResponse(saved);
    }

    public List<SavHistoryResponse> getHistory(Long ticketId) {
        findTicketForCurrentEnterprise(ticketId);
        return savTicketHistoryRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public SavHistoryResponse addNote(Long ticketId, String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Note is required");
        }
        SavTicket ticket = findTicketForCurrentEnterprise(ticketId);
        SavTicketHistory history = recordHistory(ticket, SavHistoryType.NOTE, message.trim());
        auditLogService.log("SAV_TICKET_NOTE_ADDED",
                "Internal note added to SAV ticket: ref=" + ticket.getReference());
        return toHistoryResponse(history);
    }

    @Transactional
    public SavTicketResponse createTicket(SavTicketRequest request) {
        Long entrepriseId = requireEnterpriseId();
        Entreprise entreprise = entrepriseRepo.findById(entrepriseId)
                .orElseThrow(() -> new RuntimeException("Entreprise not found: " + entrepriseId));

        SavTicket ticket = new SavTicket();
        ticket.setReference(generateReference());
        ticket.setEntreprise(entreprise);
        ticket.setCreatedBy(enterpriseSecurityUtil.getCurrentUser().orElse(null));
        applyRequest(ticket, request, entrepriseId, true);

        SavTicket saved = savTicketRepository.save(ticket);
        recordHistory(saved, SavHistoryType.CREATED, "Ticket created");
        if (saved.getCommandeVente() != null) {
            recordHistory(saved, SavHistoryType.SALE_VERIFIED,
                    "Original sale verified: " + saved.getCommandeVente().getReference());
        }
        auditLogService.log("SAV_TICKET_CREATED",
                "Ticket SAV cree: ref=" + saved.getReference()
                        + ", sujet=" + saved.getSubject()
                        + ", statut=" + saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public SavTicketResponse updateTicket(Long id, SavTicketRequest request) {
        Long entrepriseId = requireEnterpriseId();
        SavTicket ticket = findTicketForCurrentEnterprise(id);
        SavStatus previousStatus = ticket.getStatus();
        Long previousAssigneeId = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null;
        applyRequest(ticket, request, entrepriseId, false);
        validateTransition(previousStatus, ticket.getStatus());
        prepareAndValidateStage(ticket, previousStatus, ticket.getStatus());
        updateClosedAt(ticket, previousStatus);

        SavTicket saved = savTicketRepository.save(ticket);
        if (previousStatus != saved.getStatus()) {
            recordHistory(saved, SavHistoryType.STATUS_CHANGED,
                    "Status changed from " + previousStatus + " to " + saved.getStatus());
        }
        Long newAssigneeId = saved.getAssignedTo() != null ? saved.getAssignedTo().getId() : null;
        if (!Objects.equals(previousAssigneeId, newAssigneeId)) {
            String assignee = saved.getAssignedTo() != null ? saved.getAssignedTo().getUsername() : "Unassigned";
            recordHistory(saved, SavHistoryType.ASSIGNED, "Ticket assigned to " + assignee);
        }
        recordHistory(saved, SavHistoryType.UPDATED, "Ticket information updated");
        auditLogService.log("SAV_TICKET_UPDATED",
                "Ticket SAV modifie: ref=" + saved.getReference()
                        + ", statut=" + previousStatus + " -> " + saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public SavTicketResponse updateStatus(Long id, SavStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        SavTicket ticket = findTicketForCurrentEnterprise(id);
        SavStatus previousStatus = ticket.getStatus();
        if (previousStatus == status) {
            return toResponse(ticket);
        }
        validateTransition(previousStatus, status);
        prepareAndValidateStage(ticket, previousStatus, status);
        ticket.setStatus(status);
        updateClosedAt(ticket, previousStatus);

        SavTicket saved = savTicketRepository.save(ticket);
        recordHistory(saved, SavHistoryType.STATUS_CHANGED,
                "Status changed from " + previousStatus + " to " + saved.getStatus());
        auditLogService.log("SAV_TICKET_STATUS_CHANGED",
                "Statut ticket SAV change: ref=" + saved.getReference()
                        + ", " + previousStatus + " -> " + saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public void deleteTicket(Long id) {
        SavTicket ticket = findTicketForCurrentEnterprise(id);
        savInterventionRepository.deleteByTicketId(id);
        savTicketHistoryRepository.deleteByTicketId(id);
        savTicketRepository.delete(ticket);
        auditLogService.log("SAV_TICKET_DELETED",
                "Ticket SAV supprime: ref=" + ticket.getReference() + ", id=" + id);
    }

    private void applyRequest(SavTicket ticket, SavTicketRequest request, Long entrepriseId, boolean creating) {
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        ticket.setSubject(request.getSubject().trim());
        ticket.setDescription(request.getDescription());
        ticket.setDiagnostic(request.getDiagnostic());
        ticket.setResolution(request.getResolution());
        ticket.setStatus(creating
                ? SavStatus.OUVERT
                : request.getStatus() != null ? request.getStatus() : defaultStatus(ticket));
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : SavPriority.NORMALE);
        ticket.setType(request.getType() != null ? request.getType() : SavType.SUPPORT);
        ticket.setEstimatedCost(request.getEstimatedCost());
        if (creating) {
            ticket.setActualCost(BigDecimal.ZERO);
        }
        ticket.setSerialNumber(clean(request.getSerialNumber()));
        ticket.setPurchaseDate(request.getPurchaseDate());
        ticket.setWarrantyMonths(request.getWarrantyMonths());
        ticket.setContactChannel(clean(request.getContactChannel()));
        if (creating) {
            ticket.setApprovalStatus(SavApprovalStatus.NON_REQUIS);
        } else if (ticket.getApprovalStatus() == null) {
            ticket.setApprovalStatus(SavApprovalStatus.NON_REQUIS);
        }
        ticket.setResolutionType(request.getResolutionType());
        ticket.setDueDate(request.getDueDate() != null ? request.getDueDate() : defaultDueDate(ticket));

        if (ticket.getEstimatedCost() != null && ticket.getEstimatedCost().signum() < 0) {
            throw new IllegalArgumentException("Costs cannot be negative");
        }
        if (ticket.getWarrantyMonths() != null && ticket.getWarrantyMonths() < 0) {
            throw new IllegalArgumentException("Warranty duration cannot be negative");
        }

        ticket.setClient(resolveClient(request.getClientId(), entrepriseId));
        ticket.setArticleVente(resolveArticleVente(request.getArticleVenteId(), entrepriseId));
        ticket.setCommandeVente(resolveSale(request.getCommandeVenteId(), ticket.getClient(), ticket.getArticleVente(), entrepriseId));
        ticket.setAssignedTo(resolveUser(request.getAssignedToUserId(), entrepriseId));

        if (ticket.getCommandeVente() != null && ticket.getPurchaseDate() == null) {
            ticket.setPurchaseDate(ticket.getCommandeVente().getDateCommande());
        }
        if (creating && requiresVerifiedSale(ticket.getType()) && ticket.getCommandeVente() == null) {
            throw new IllegalArgumentException("A verified original sale is required for warranty, return or exchange requests");
        }
    }

    private SavStatus defaultStatus(SavTicket ticket) {
        return ticket.getStatus() != null ? ticket.getStatus() : SavStatus.OUVERT;
    }

    private LocalDate defaultDueDate(SavTicket ticket) {
        if (ticket.getDueDate() != null) {
            return ticket.getDueDate();
        }
        return LocalDate.now().plusDays(switch (ticket.getPriority()) {
            case URGENTE -> 1;
            case HAUTE -> 2;
            case NORMALE -> 5;
            case BASSE -> 10;
        });
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateTransition(SavStatus from, SavStatus to) {
        if (from == null || to == null || from == to) {
            return;
        }
        Set<SavStatus> allowed = switch (from) {
            case OUVERT -> Set.of(SavStatus.QUALIFICATION, SavStatus.ANNULE);
            case QUALIFICATION -> Set.of(SavStatus.EN_ATTENTE_PRODUIT, SavStatus.DIAGNOSTIC,
                    SavStatus.EN_ATTENTE_CLIENT, SavStatus.ANNULE);
            case EN_ATTENTE_PRODUIT -> Set.of(SavStatus.DIAGNOSTIC, SavStatus.ANNULE);
            case DIAGNOSTIC -> Set.of(SavStatus.EN_ATTENTE_ACCORD, SavStatus.EN_REPARATION,
                    SavStatus.EN_ATTENTE_CLIENT, SavStatus.RESOLU, SavStatus.ANNULE);
            case EN_ATTENTE_ACCORD -> Set.of(SavStatus.EN_REPARATION, SavStatus.ANNULE);
            case EN_REPARATION -> Set.of(SavStatus.PRET_RESTITUTION, SavStatus.EN_ATTENTE_CLIENT, SavStatus.ANNULE);
            case PRET_RESTITUTION -> Set.of(SavStatus.RESOLU, SavStatus.EN_REPARATION);
            case EN_COURS -> Set.of(SavStatus.DIAGNOSTIC, SavStatus.EN_REPARATION,
                    SavStatus.EN_ATTENTE_CLIENT, SavStatus.RESOLU, SavStatus.ANNULE);
            case EN_ATTENTE_CLIENT -> Set.of(SavStatus.QUALIFICATION, SavStatus.DIAGNOSTIC,
                    SavStatus.EN_REPARATION, SavStatus.RESOLU, SavStatus.ANNULE);
            case RESOLU -> Set.of(SavStatus.FERME, SavStatus.DIAGNOSTIC);
            case FERME -> Set.of(SavStatus.DIAGNOSTIC);
            case ANNULE -> Set.of(SavStatus.OUVERT);
        };
        if (!allowed.contains(to)) {
            throw new IllegalArgumentException("Invalid SAV transition: " + from + " -> " + to);
        }
    }

    private List<SavStatus> terminalStatuses() {
        return List.of(SavStatus.RESOLU, SavStatus.FERME, SavStatus.ANNULE);
    }

    private List<SavStatus> activeProcessingStatuses() {
        return List.of(SavStatus.QUALIFICATION, SavStatus.EN_ATTENTE_PRODUIT, SavStatus.DIAGNOSTIC,
                SavStatus.EN_ATTENTE_ACCORD, SavStatus.EN_REPARATION, SavStatus.PRET_RESTITUTION,
                SavStatus.EN_COURS);
    }

    private void prepareAndValidateStage(SavTicket ticket, SavStatus from, SavStatus target) {
        if (target == null) {
            return;
        }
        if (target == SavStatus.QUALIFICATION && (ticket.getClient() == null || isBlank(ticket.getDescription()))) {
            throw new IllegalArgumentException("Customer and request description are required for qualification");
        }
        if (target == SavStatus.DIAGNOSTIC) {
            if (ticket.getAssignedTo() == null) {
                throw new IllegalArgumentException("A technician must be assigned before diagnosis");
            }
            if (from == SavStatus.EN_ATTENTE_PRODUIT) {
                ticket.setProductReceivedAt(LocalDateTime.now());
            }
        }
        if (target == SavStatus.EN_ATTENTE_ACCORD) {
            if (isBlank(ticket.getDiagnostic()) || ticket.getEstimatedCost() == null) {
                throw new IllegalArgumentException("Diagnostic and estimate are required before customer approval");
            }
            ticket.setApprovalStatus(SavApprovalStatus.EN_ATTENTE);
        }
        if (target == SavStatus.EN_REPARATION) {
            if (isBlank(ticket.getDiagnostic())) {
                throw new IllegalArgumentException("Diagnostic is required before repair");
            }
            boolean approvalRequired = !isUnderWarranty(ticket) && ticket.getEstimatedCost() != null
                    && ticket.getEstimatedCost().compareTo(BigDecimal.ZERO) > 0;
            if (approvalRequired && ticket.getApprovalStatus() != SavApprovalStatus.ACCEPTE) {
                throw new IllegalArgumentException("Customer approval is required before repair");
            }
            if (!approvalRequired && ticket.getApprovalStatus() != SavApprovalStatus.ACCEPTE) {
                ticket.setApprovalStatus(SavApprovalStatus.NON_REQUIS);
            }
        }
        if (target == SavStatus.PRET_RESTITUTION) {
            if (isBlank(ticket.getResolution()) || ticket.getResolutionType() == null) {
                throw new IllegalArgumentException("Resolution details and outcome are required before restitution");
            }
            if (ticket.getType() == SavType.REPARATION && savInterventionRepository.countByTicketId(ticket.getId()) == 0) {
                throw new IllegalArgumentException("At least one intervention must be logged for a repair");
            }
        }
        if (target == SavStatus.RESOLU && from != SavStatus.PRET_RESTITUTION
                && (isBlank(ticket.getResolution()) || ticket.getResolutionType() == null)) {
            throw new IllegalArgumentException("Resolution details and outcome are required before resolving the ticket");
        }
        if (target == SavStatus.RESOLU && from == SavStatus.PRET_RESTITUTION) {
            ticket.setProductReturnedAt(LocalDateTime.now());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean requiresVerifiedSale(SavType type) {
        return type == SavType.GARANTIE || type == SavType.RETOUR || type == SavType.ECHANGE;
    }

    private CommandeVente resolveSale(Long orderId, Client client, ArticleVente article, Long entrepriseId) {
        if (orderId == null) {
            return null;
        }
        if (client == null || article == null) {
            throw new IllegalArgumentException("Customer and sold product are required to verify a sale");
        }
        CommandeVente order = commandeVenteRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sales order not found: " + orderId));
        if (order.getEntreprise() == null || !entrepriseId.equals(order.getEntreprise().getId())) {
            throw new RuntimeException("Sales order does not belong to current enterprise");
        }
        if (order.getClient() == null || !client.getId().equals(order.getClient().getId())) {
            throw new IllegalArgumentException("Sales order does not belong to the selected customer");
        }
        if (!isEligibleSale(order)) {
            throw new IllegalArgumentException("Sales order is not confirmed, delivered or invoiced");
        }
        boolean productWasSold = ligneCommandeVenteRepository.findByCommandeVenteId(orderId).stream()
                .anyMatch(line -> line.getArticleVente() != null && article.getId().equals(line.getArticleVente().getId()));
        if (!productWasSold) {
            throw new IllegalArgumentException("Selected product is not part of the original sales order");
        }
        return order;
    }

    private boolean isEligibleSale(CommandeVente order) {
        CommandeStatut status = CommandeStatut.fromString(order.getStatut());
        return status != null && status.isRevenueQualifying();
    }

    private void validateNonNegative(BigDecimal value, String label) {
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException(label + " cannot be negative");
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private void recalculateActualCost(SavTicket ticket) {
        BigDecimal total = savInterventionRepository.findByTicketIdOrderByPerformedAtDesc(ticket.getId()).stream()
                .map(item -> zeroIfNull(item.getLaborCost()).add(zeroIfNull(item.getPartsCost())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ticket.setActualCost(total);
        savTicketRepository.save(ticket);
    }

    private SavTicketHistory recordHistory(SavTicket ticket, SavHistoryType type, String message) {
        SavTicketHistory history = new SavTicketHistory();
        history.setTicket(ticket);
        history.setType(type);
        history.setMessage(message);
        history.setCreatedBy(enterpriseSecurityUtil.getCurrentUser().orElse(null));
        return savTicketHistoryRepository.save(history);
    }

    private Client resolveClient(Long clientId, Long entrepriseId) {
        if (clientId == null) {
            return null;
        }
        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));
        if (client.getEntreprise() == null || !entrepriseId.equals(client.getEntreprise().getId())) {
            throw new RuntimeException("Client does not belong to current enterprise");
        }
        return client;
    }

    private ArticleVente resolveArticleVente(Long articleVenteId, Long entrepriseId) {
        if (articleVenteId == null) {
            return null;
        }
        ArticleVente articleVente = articleVenteRepo.findById(articleVenteId)
                .orElseThrow(() -> new RuntimeException("Article vente not found: " + articleVenteId));
        Article articleAchat = articleVente.getArticleAchat();
        if (articleAchat == null || articleAchat.getEntreprise() == null
                || !entrepriseId.equals(articleAchat.getEntreprise().getId())) {
            throw new RuntimeException("Article vente does not belong to current enterprise");
        }
        return articleVente;
    }

    private User resolveUser(Long userId, Long entrepriseId) {
        if (userId == null) {
            return null;
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (user.getEntreprise() == null || !entrepriseId.equals(user.getEntreprise().getId())) {
            throw new RuntimeException("Assigned user does not belong to current enterprise");
        }
        return user;
    }

    private void updateClosedAt(SavTicket ticket, SavStatus previousStatus) {
        boolean isClosing = terminalStatuses().contains(ticket.getStatus());
        boolean wasClosing = terminalStatuses().contains(previousStatus);
        if (isClosing && !wasClosing) {
            ticket.setClosedAt(LocalDateTime.now());
        } else if (!isClosing) {
            ticket.setClosedAt(null);
        }
    }

    private SavTicket findTicketForCurrentEnterprise(Long id) {
        Long entrepriseId = requireEnterpriseId();
        return savTicketRepository.findByIdAndEntrepriseId(id, entrepriseId)
                .orElseThrow(() -> new RuntimeException("SAV ticket not found: " + id));
    }

    private Long requireEnterpriseId() {
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
        if (entrepriseId == null) {
            entrepriseId = enterpriseSecurityUtil.getCurrentUser()
                    .map(User::getEntreprise)
                    .map(Entreprise::getId)
                    .orElse(null);
        }
        if (entrepriseId == null) {
            throw new RuntimeException("Enterprise context required");
        }
        return entrepriseId;
    }

    private String generateReference() {
        return "SAV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private SavTicketResponse toResponse(SavTicket ticket) {
        Client client = ticket.getClient();
        ArticleVente articleVente = ticket.getArticleVente();
        CommandeVente commandeVente = ticket.getCommandeVente();
        User createdBy = ticket.getCreatedBy();
        User assignedTo = ticket.getAssignedTo();
        return SavTicketResponse.builder()
                .id(ticket.getId())
                .reference(ticket.getReference())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .diagnostic(ticket.getDiagnostic())
                .resolution(ticket.getResolution())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .type(ticket.getType())
                .estimatedCost(ticket.getEstimatedCost())
                .actualCost(ticket.getActualCost())
                .serialNumber(ticket.getSerialNumber())
                .purchaseDate(ticket.getPurchaseDate())
                .warrantyMonths(ticket.getWarrantyMonths())
                .warrantyEndDate(warrantyEndDate(ticket))
                .underWarranty(isUnderWarranty(ticket))
                .overdue(isOverdue(ticket))
                .contactChannel(ticket.getContactChannel())
                .approvalStatus(ticket.getApprovalStatus())
                .resolutionType(ticket.getResolutionType())
                .customerApprovedAt(ticket.getCustomerApprovedAt())
                .productReceivedAt(ticket.getProductReceivedAt())
                .productReturnedAt(ticket.getProductReturnedAt())
                .dueDate(ticket.getDueDate())
                .closedAt(ticket.getClosedAt())
                .clientId(client != null ? client.getId() : null)
                .clientName(client != null ? (client.getPrenom() + " " + client.getNom()).trim() : null)
                .articleVenteId(articleVente != null ? articleVente.getId() : null)
                .articleVenteName(articleVente != null ? articleVente.getNom() : null)
                .commandeVenteId(commandeVente != null ? commandeVente.getId() : null)
                .commandeVenteReference(commandeVente != null ? commandeVente.getReference() : null)
                .saleVerified(commandeVente != null)
                .createdByUserId(createdBy != null ? createdBy.getId() : null)
                .createdByUsername(createdBy != null ? createdBy.getUsername() : null)
                .assignedToUserId(assignedTo != null ? assignedTo.getId() : null)
                .assignedToUsername(assignedTo != null ? assignedTo.getUsername() : null)
                .entrepriseId(ticket.getEntreprise() != null ? ticket.getEntreprise().getId() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private SavInterventionResponse toInterventionResponse(SavIntervention intervention) {
        User technician = intervention.getTechnician();
        BigDecimal labor = zeroIfNull(intervention.getLaborCost());
        BigDecimal parts = zeroIfNull(intervention.getPartsCost());
        return SavInterventionResponse.builder()
                .id(intervention.getId())
                .description(intervention.getDescription())
                .durationMinutes(intervention.getDurationMinutes())
                .laborCost(labor)
                .partsCost(parts)
                .totalCost(labor.add(parts))
                .technicianUserId(technician != null ? technician.getId() : null)
                .technicianUsername(technician != null ? technician.getUsername() : null)
                .performedAt(intervention.getPerformedAt())
                .build();
    }

    private LocalDate warrantyEndDate(SavTicket ticket) {
        if (ticket.getPurchaseDate() == null || ticket.getWarrantyMonths() == null) {
            return null;
        }
        return ticket.getPurchaseDate().plusMonths(ticket.getWarrantyMonths());
    }

    private boolean isUnderWarranty(SavTicket ticket) {
        LocalDate endDate = warrantyEndDate(ticket);
        return endDate != null && !LocalDate.now().isAfter(endDate);
    }

    private boolean isOverdue(SavTicket ticket) {
        return ticket.getDueDate() != null
                && ticket.getDueDate().isBefore(LocalDate.now())
                && !terminalStatuses().contains(ticket.getStatus());
    }

    private SavHistoryResponse toHistoryResponse(SavTicketHistory history) {
        User createdBy = history.getCreatedBy();
        return SavHistoryResponse.builder()
                .id(history.getId())
                .type(history.getType())
                .message(history.getMessage())
                .createdByUserId(createdBy != null ? createdBy.getId() : null)
                .createdByUsername(createdBy != null ? createdBy.getUsername() : null)
                .createdAt(history.getCreatedAt())
                .build();
    }
}
