package BuissnesManager.BuissnesManager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SavStatsDto {
    private long total;
    private long open;
    private long inProgress;
    private long waitingCustomer;
    private long resolved;
    private long closed;
    private long cancelled;
    private long overdue;
    private long unassigned;
    private BigDecimal actualCostTotal;
}
