package BuissnesManager.BuissnesManager.dto;

import BuissnesManager.BuissnesManager.entity.SavHistoryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SavHistoryResponse {
    private Long id;
    private SavHistoryType type;
    private String message;
    private Long createdByUserId;
    private String createdByUsername;
    private LocalDateTime createdAt;
}
