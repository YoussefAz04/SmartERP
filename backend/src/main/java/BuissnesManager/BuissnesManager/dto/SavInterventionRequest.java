package BuissnesManager.BuissnesManager.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Getter
@Setter
public class SavInterventionRequest {
    @NotBlank(message = "Intervention description is required")
    private String description;

    @NotNull(message = "Intervention duration is required")
    @Positive(message = "Intervention duration must be positive")
    private Integer durationMinutes;

    @PositiveOrZero(message = "Labor cost cannot be negative")
    private BigDecimal laborCost;

    @PositiveOrZero(message = "Parts cost cannot be negative")
    private BigDecimal partsCost;
    private Long technicianUserId;
}
