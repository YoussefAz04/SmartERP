package BuissnesManager.BuissnesManager.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class SavSaleItemResponse {
    private Long orderId;
    private String orderReference;
    private LocalDate orderDate;
    private String orderStatus;
    private Long articleVenteId;
    private String articleName;
    private String sku;
    private BigDecimal quantity;
}
