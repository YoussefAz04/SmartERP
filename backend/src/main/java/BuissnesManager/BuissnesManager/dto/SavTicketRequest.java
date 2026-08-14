package BuissnesManager.BuissnesManager.dto;

import BuissnesManager.BuissnesManager.entity.SavPriority;
import BuissnesManager.BuissnesManager.entity.SavStatus;
import BuissnesManager.BuissnesManager.entity.SavType;
import BuissnesManager.BuissnesManager.entity.SavApprovalStatus;
import BuissnesManager.BuissnesManager.entity.SavResolutionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SavTicketRequest {
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
    private String contactChannel;
    private SavApprovalStatus approvalStatus;
    private SavResolutionType resolutionType;
    private LocalDate dueDate;
    private Long clientId;
    private Long articleVenteId;
    private Long commandeVenteId;
    private Long assignedToUserId;
}
