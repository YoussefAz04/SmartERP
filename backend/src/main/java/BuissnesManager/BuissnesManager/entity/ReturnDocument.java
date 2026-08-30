package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", nullable = false, length = 30)
    private ReturnType returnType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "amount_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountHT;

    @Column(name = "amount_tax", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountTax;

    @Column(name = "amount_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id")
    @JsonIgnoreProperties({"commandeVente", "client", "entreprise", "detaisFactureDeVente"})
    private FactureDeVente salesInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id")
    @JsonIgnoreProperties({"commandeAchat", "fournisseur", "entreprise", "lignes"})
    private FactureAchat purchaseInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @OneToMany(mappedBy = "returnDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnLine> lines = new ArrayList<>();

    @OneToOne(mappedBy = "returnDocument", fetch = FetchType.LAZY)
    private CreditNote creditNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
