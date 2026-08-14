package BuissnesManager.BuissnesManager.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SavInterventionRequest {
    private String description;
    private Integer durationMinutes;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private Long technicianUserId;
}
