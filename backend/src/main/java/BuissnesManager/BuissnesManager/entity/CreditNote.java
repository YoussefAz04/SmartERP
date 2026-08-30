package BuissnesManager.BuissnesManager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_note")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private FinancialDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditNoteStatus status;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "amount_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountHT;

    @Column(name = "amount_tax", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountTax;

    @Column(name = "amount_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountTotal;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_document_id", nullable = false, unique = true)
    private ReturnDocument returnDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
