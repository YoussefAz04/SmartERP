package BuissnesManager.BuissnesManager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BudgetPerformanceDto {
    private Long budgetId;
    private String nom;
    private Double tauxConsommation;
    private Double tauxTemps;
    private boolean depassement;
}
