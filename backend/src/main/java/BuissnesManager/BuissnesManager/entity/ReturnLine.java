package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "return_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPriceHT;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "amount_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountHT;

    @Column(name = "amount_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_document_id", nullable = false)
    @JsonIgnore
    private ReturnDocument returnDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_line_id")
    private LigneCommandeVente salesOrderLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_line_id")
    private LigneCommandeAchat purchaseOrderLine;
}
