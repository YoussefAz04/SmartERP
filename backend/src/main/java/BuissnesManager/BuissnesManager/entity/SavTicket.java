package BuissnesManager.BuissnesManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sav_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String reference;

    @NotBlank(message = "Subject is required")
    @Column(nullable = false, length = 180)
    private String subject;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String diagnostic;

    @Column(length = 2000)
    private String resolution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SavStatus status = SavStatus.OUVERT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SavPriority priority = SavPriority.NORMALE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SavType type = SavType.SUPPORT;

    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedCost;

    @Column(precision = 15, scale = 2)
    private BigDecimal actualCost;

    @Column(length = 100)
    private String serialNumber;

    private LocalDate purchaseDate;

    private Integer warrantyMonths;

    @Column(length = 30)
    private String contactChannel;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SavApprovalStatus approvalStatus = SavApprovalStatus.NON_REQUIS;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private SavResolutionType resolutionType;

    private LocalDateTime customerApprovedAt;

    private LocalDateTime productReceivedAt;

    private LocalDateTime productReturnedAt;

    private LocalDate dueDate;

    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "factureDeVentes", "entreprises", "entreprise"})
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "article_vente_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "client", "stock", "articleAchat"})
    private ArticleVente articleVente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commande_vente_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lignes", "entreprise", "client"})
    private CommandeVente commandeVente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles", "entreprise"})
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles", "entreprise"})
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    @NotNull(message = "Enterprise is required")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Entreprise entreprise;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
