package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.FinancialDtos.*;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class FinancialService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final PaymentRepository paymentRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final FactureDeVenteRepo salesInvoiceRepository;
    private final FactureAchatRepo purchaseInvoiceRepository;
    private final EnterpriseSecurityUtil enterpriseSecurityUtil;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public FinancialService(PaymentRepository paymentRepository,
                            CreditNoteRepository creditNoteRepository,
                            FactureDeVenteRepo salesInvoiceRepository,
                            FactureAchatRepo purchaseInvoiceRepository,
                            EnterpriseSecurityUtil enterpriseSecurityUtil,
                            AuditLogService auditLogService,
                            NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.enterpriseSecurityUtil = enterpriseSecurityUtil;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentResponse registerPayment(PaymentRequest request) {
        Long enterpriseId = requireEnterpriseId();
        FactureDeVente salesInvoice = null;
        FactureAchat purchaseInvoice = null;
        InvoiceBalance balance;
        if (request.documentType() == FinancialDocumentType.SALES) {
            salesInvoice = requireSalesInvoiceForUpdate(request.invoiceId(), enterpriseId);
            balance = salesBalance(salesInvoice);
        } else {
            purchaseInvoice = requirePurchaseInvoiceForUpdate(request.invoiceId(), enterpriseId);
            balance = purchaseBalance(purchaseInvoice);
        }
        BigDecimal amount = money(request.amount());
        if (amount.compareTo(balance.balance()) > 0) {
            throw new IllegalArgumentException("Payment exceeds the outstanding balance of " + balance.balance());
        }

        Payment payment = new Payment();
        payment.setReference(uniqueReference(request.reference(), request.documentType()));
        payment.setDocumentType(request.documentType());
        payment.setAmount(amount);
        payment.setPaymentDate(request.paymentDate());
        payment.setPaymentMethod(request.paymentMethod().trim().toUpperCase(Locale.ROOT));
        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setNotes(trimToNull(request.notes()));
        payment.setCreatedBy(enterpriseSecurityUtil.getCurrentUser().orElse(null));

        if (request.documentType() == FinancialDocumentType.SALES) {
            payment.setSalesInvoice(salesInvoice);
            payment.setEntreprise(salesInvoice.getEntreprise());
        } else {
            payment.setPurchaseInvoice(purchaseInvoice);
            payment.setEntreprise(purchaseInvoice.getEntreprise());
        }

        Payment saved = paymentRepository.save(payment);
        recalculateInvoice(request.documentType(), request.invoiceId());
        auditLogService.log("PAYMENT_RECORDED", "Payment recorded: " + saved.getReference()
                + ", amount=" + saved.getAmount() + ", invoice=" + balance.invoiceReference());
        return toPaymentResponse(saved);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Long enterpriseId = requireEnterpriseId();
        Payment payment = paymentRepository.findById(paymentId)
                .filter(p -> p.getEntreprise().getId().equals(enterpriseId))
                .orElseThrow(() -> new NoSuchElementException("Payment not found"));
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Payment is already cancelled");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        Payment saved = paymentRepository.save(payment);
        Long invoiceId = payment.getDocumentType() == FinancialDocumentType.SALES
                ? payment.getSalesInvoice().getId() : payment.getPurchaseInvoice().getId();
        recalculateInvoice(payment.getDocumentType(), invoiceId);
        auditLogService.log("PAYMENT_CANCELLED", "Payment cancelled: " + saved.getReference());
        return toPaymentResponse(saved);
    }

    @Transactional
    public InvoiceBalance updateDueDate(FinancialDocumentType type, Long invoiceId, LocalDate dueDate) {
        Long enterpriseId = requireEnterpriseId();
        if (type == FinancialDocumentType.SALES) {
            FactureDeVente invoice = requireSalesInvoice(invoiceId, enterpriseId);
            invoice.setDateEcheance(dueDate);
            salesInvoiceRepository.save(invoice);
        } else {
            FactureAchat invoice = requirePurchaseInvoice(invoiceId, enterpriseId);
            invoice.setDateEcheance(dueDate);
            purchaseInvoiceRepository.save(invoice);
        }
        return getBalance(type, invoiceId);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments() {
        return paymentRepository.findByEntrepriseIdOrderByPaymentDateDescIdDesc(requireEnterpriseId())
                .stream().map(this::toPaymentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceBalance> getInvoices(FinancialDocumentType type, Long partyId, boolean outstandingOnly) {
        return getInvoices(requireEnterpriseId(), type, partyId, outstandingOnly);
    }

    private List<InvoiceBalance> getInvoices(Long enterpriseId, FinancialDocumentType type,
                                             Long partyId, boolean outstandingOnly) {
        List<InvoiceBalance> balances;
        if (type == FinancialDocumentType.SALES) {
            balances = salesInvoiceRepository.findByEntrepriseId(enterpriseId).stream()
                    .map(this::salesBalance).toList();
        } else {
            balances = purchaseInvoiceRepository.findByEntrepriseId(enterpriseId).stream()
                    .map(this::purchaseBalance).toList();
        }
        return balances.stream()
                .filter(b -> partyId == null || Objects.equals(b.partyId(), partyId))
                .filter(b -> !outstandingOnly || b.balance().compareTo(ZERO) > 0)
                .sorted(Comparator.comparing(InvoiceBalance::dueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public FinancialSummary getSummary() {
        List<InvoiceBalance> sales = getInvoices(FinancialDocumentType.SALES, null, false);
        List<InvoiceBalance> purchases = getInvoices(FinancialDocumentType.PURCHASE, null, false);
        return new FinancialSummary(
                sumBalances(sales, false), sumBalances(purchases, false),
                sumBalances(sales, true), sumBalances(purchases, true),
                sales.stream().filter(InvoiceBalance::overdue).count(),
                purchases.stream().filter(InvoiceBalance::overdue).count());
    }

    @Transactional
    public int createOverdueNotifications() {
        return createOverdueNotificationsForEnterprise(requireEnterpriseId());
    }

    @Transactional
    public int createOverdueNotificationsForEnterprise(Long enterpriseId) {
        Objects.requireNonNull(enterpriseId, "Enterprise id is required");
        int created = 0;
        for (InvoiceBalance balance : getInvoices(enterpriseId, FinancialDocumentType.SALES, null, true)) {
            if (balance.overdue() && notificationService.createOverduePaymentNotification(balance, enterpriseId)) {
                created++;
            }
        }
        for (InvoiceBalance balance : getInvoices(enterpriseId, FinancialDocumentType.PURCHASE, null, true)) {
            if (balance.overdue() && notificationService.createOverduePaymentNotification(balance, enterpriseId)) {
                created++;
            }
        }
        return created;
    }

    @Transactional
    public InvoiceBalance recalculateInvoice(FinancialDocumentType type, Long invoiceId) {
        Long enterpriseId = requireEnterpriseId();
        if (type == FinancialDocumentType.SALES) {
            FactureDeVente invoice = requireSalesInvoice(invoiceId, enterpriseId);
            InvoiceBalance balance = salesBalance(invoice);
            invoice.setMontantPaye(balance.paidAmount());
            invoice.setSoldeRestant(balance.balance());
            invoice.setStatut(statusFor(balance));
            salesInvoiceRepository.save(invoice);
            return balance;
        }
        FactureAchat invoice = requirePurchaseInvoice(invoiceId, enterpriseId);
        InvoiceBalance balance = purchaseBalance(invoice);
        invoice.setMontantPaye(balance.paidAmount());
        invoice.setSoldeRestant(balance.balance());
        invoice.setStatut(statusFor(balance));
        purchaseInvoiceRepository.save(invoice);
        return balance;
    }

    @Transactional(readOnly = true)
    public InvoiceBalance getBalance(FinancialDocumentType type, Long invoiceId) {
        Long enterpriseId = requireEnterpriseId();
        return type == FinancialDocumentType.SALES
                ? salesBalance(requireSalesInvoice(invoiceId, enterpriseId))
                : purchaseBalance(requirePurchaseInvoice(invoiceId, enterpriseId));
    }

    private InvoiceBalance salesBalance(FactureDeVente invoice) {
        BigDecimal gross = money(invoice.getMontantTTC());
        BigDecimal credited = money(creditNoteRepository.sumIssuedBySalesInvoice(invoice.getId())).min(gross);
        BigDecimal net = gross.subtract(credited).max(ZERO);
        BigDecimal paid = effectivePaid(invoice.getMontantPaye(), invoice.getStatut(),
                paymentRepository.sumConfirmedBySalesInvoice(invoice.getId()), net);
        return balance(FinancialDocumentType.SALES, invoice.getId(), invoice.getReferance(),
                invoice.getClient() != null ? invoice.getClient().getId() : null,
                invoice.getClient() != null ? partyName(invoice.getClient().getNom(), invoice.getClient().getPrenom()) : "-",
                invoice.getDateEcheance(), gross, credited, paid, invoice.getStatut());
    }

    private InvoiceBalance purchaseBalance(FactureAchat invoice) {
        BigDecimal gross = money(invoice.getMontantTTC());
        BigDecimal credited = money(creditNoteRepository.sumIssuedByPurchaseInvoice(invoice.getId())).min(gross);
        BigDecimal net = gross.subtract(credited).max(ZERO);
        BigDecimal paid = effectivePaid(invoice.getMontantPaye(), invoice.getStatut(),
                paymentRepository.sumConfirmedByPurchaseInvoice(invoice.getId()), net);
        return balance(FinancialDocumentType.PURCHASE, invoice.getId(), invoice.getReferance(),
                invoice.getFournisseur() != null ? invoice.getFournisseur().getId() : null,
                invoice.getFournisseur() != null ? partyName(invoice.getFournisseur().getNom(), invoice.getFournisseur().getPrenom()) : "-",
                invoice.getDateEcheance(), gross, credited, paid, invoice.getStatut());
    }

    private InvoiceBalance balance(FinancialDocumentType type, Long id, String reference,
                                   Long partyId, String partyName, LocalDate dueDate,
                                   BigDecimal gross, BigDecimal credited, BigDecimal paid, String status) {
        BigDecimal net = gross.subtract(credited).max(ZERO);
        BigDecimal balance = net.subtract(paid).max(ZERO);
        BigDecimal creditBalance = paid.subtract(net).max(ZERO);
        boolean overdue = dueDate != null && dueDate.isBefore(LocalDate.now()) && balance.compareTo(ZERO) > 0;
        return new InvoiceBalance(type, id, reference, partyId, partyName, dueDate, gross,
                credited, net, paid, balance, creditBalance, status, overdue);
    }

    private BigDecimal effectivePaid(BigDecimal storedPaid, String status, BigDecimal recorded, BigDecimal net) {
        BigDecimal payments = money(recorded);
        if (payments.compareTo(ZERO) > 0 || storedPaid != null) {
            return payments;
        }
        return "PAYEE".equals(status) ? net : ZERO;
    }

    private String statusFor(InvoiceBalance balance) {
        if (balance.netAmount().compareTo(ZERO) == 0) return "CREDITEE";
        if (balance.balance().compareTo(ZERO) == 0) return "PAYEE";
        if (balance.paidAmount().compareTo(ZERO) > 0 || balance.creditedAmount().compareTo(ZERO) > 0) {
            return "PARTIELLEMENT_PAYEE";
        }
        return "EN_ATTENTE";
    }

    private FactureDeVente requireSalesInvoice(Long id, Long enterpriseId) {
        return salesInvoiceRepository.findById(id)
                .filter(f -> f.getEntreprise() != null && enterpriseId.equals(f.getEntreprise().getId()))
                .orElseThrow(() -> new NoSuchElementException("Sales invoice not found"));
    }

    private FactureDeVente requireSalesInvoiceForUpdate(Long id, Long enterpriseId) {
        return salesInvoiceRepository.findByIdForUpdate(id)
                .filter(f -> f.getEntreprise() != null && enterpriseId.equals(f.getEntreprise().getId()))
                .orElseThrow(() -> new NoSuchElementException("Sales invoice not found"));
    }

    private FactureAchat requirePurchaseInvoice(Long id, Long enterpriseId) {
        return purchaseInvoiceRepository.findById(id)
                .filter(f -> f.getEntreprise() != null && enterpriseId.equals(f.getEntreprise().getId()))
                .orElseThrow(() -> new NoSuchElementException("Purchase invoice not found"));
    }

    private FactureAchat requirePurchaseInvoiceForUpdate(Long id, Long enterpriseId) {
        return purchaseInvoiceRepository.findByIdForUpdate(id)
                .filter(f -> f.getEntreprise() != null && enterpriseId.equals(f.getEntreprise().getId()))
                .orElseThrow(() -> new NoSuchElementException("Purchase invoice not found"));
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        Long invoiceId = payment.getDocumentType() == FinancialDocumentType.SALES
                ? payment.getSalesInvoice().getId() : payment.getPurchaseInvoice().getId();
        String invoiceReference = payment.getDocumentType() == FinancialDocumentType.SALES
                ? payment.getSalesInvoice().getReferance() : payment.getPurchaseInvoice().getReferance();
        return new PaymentResponse(payment.getId(), payment.getReference(), payment.getDocumentType(),
                invoiceId, invoiceReference, payment.getAmount(), payment.getPaymentDate(),
                payment.getPaymentMethod(), payment.getStatus(), payment.getNotes(), payment.getCreatedAt());
    }

    private String uniqueReference(String requested, FinancialDocumentType type) {
        if (requested != null && !requested.isBlank()) return requested.trim();
        return "PAY-" + (type == FinancialDocumentType.SALES ? "S-" : "P-")
                + LocalDate.now().toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private BigDecimal sumBalances(List<InvoiceBalance> balances, boolean overdueOnly) {
        return balances.stream().filter(b -> !overdueOnly || b.overdue())
                .map(InvoiceBalance::balance).reduce(ZERO, BigDecimal::add);
    }

    private Long requireEnterpriseId() {
        Long id = enterpriseSecurityUtil.getContextEnterpriseId();
        if (id == null) throw new IllegalStateException("Enterprise context required");
        return id;
    }

    private BigDecimal money(Double value) {
        return value == null ? ZERO : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String partyName(String name, String surname) {
        return ((name == null ? "" : name) + " " + (surname == null ? "" : surname)).trim();
    }
}
