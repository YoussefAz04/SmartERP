package BuissnesManager.BuissnesManager.dto;

import BuissnesManager.BuissnesManager.entity.SavPriority;
import BuissnesManager.BuissnesManager.entity.SavStatus;
import BuissnesManager.BuissnesManager.entity.SavType;
import BuissnesManager.BuissnesManager.entity.SavApprovalStatus;
import BuissnesManager.BuissnesManager.entity.SavResolutionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SavTicketResponse {
    private Long id;
    private String reference;
    private String subject;
    private String description;
    private String diagnostic;
    private String resolution;
    private SavStatus status;
    private SavPriority priority;
    private SavType type;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String serialNumber;
    private LocalDate purchaseDate;
    private Integer warrantyMonths;
    private LocalDate warrantyEndDate;
    private boolean underWarranty;
    private boolean overdue;
    private String contactChannel;
    private SavApprovalStatus approvalStatus;
    private SavResolutionType resolutionType;
    private LocalDateTime customerApprovedAt;
    private LocalDateTime productReceivedAt;
    private LocalDateTime productReturnedAt;
    private LocalDate dueDate;
    private LocalDateTime closedAt;
    private Long clientId;
    private String clientName;
    private Long articleVenteId;
    private String articleVenteName;
    private Long commandeVenteId;
    private String commandeVenteReference;
    private boolean saleVerified;
    private Long createdByUserId;
    private String createdByUsername;
    private Long assignedToUserId;
    private String assignedToUsername;
    private Long entrepriseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
