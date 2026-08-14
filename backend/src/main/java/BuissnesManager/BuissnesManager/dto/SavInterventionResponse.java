package BuissnesManager.BuissnesManager.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SavInterventionResponse {
    private Long id;
    private String description;
    private Integer durationMinutes;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private BigDecimal totalCost;
    private Long technicianUserId;
    private String technicianUsername;
    private LocalDateTime performedAt;
}
