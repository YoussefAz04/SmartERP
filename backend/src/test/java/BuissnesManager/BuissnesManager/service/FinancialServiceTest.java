package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.FinancialDtos.PaymentRequest;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FinancialServiceTest {
    @Mock private PaymentRepository paymentRepository;
    @Mock private CreditNoteRepository creditNoteRepository;
    @Mock private FactureDeVenteRepo salesInvoiceRepository;
    @Mock private FactureAchatRepo purchaseInvoiceRepository;
    @Mock private EnterpriseSecurityUtil enterpriseSecurityUtil;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;

    private FinancialService service;
    private FactureDeVente invoice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new FinancialService(paymentRepository, creditNoteRepository,
                salesInvoiceRepository, purchaseInvoiceRepository, enterpriseSecurityUtil,
                auditLogService, notificationService);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        Client client = new Client();
        client.setId(20L);
        client.setNom("Acme");
        client.setPrenom("Store");
        invoice = new FactureDeVente();
        invoice.setId(10L);
        invoice.setReferance("FV-0010");
        invoice.setMontantTTC(100.0);
        invoice.setStatut("EN_ATTENTE");
        invoice.setDateEcheance(LocalDate.now().plusDays(10));
        invoice.setEntreprise(entreprise);
        invoice.setClient(client);

        when(enterpriseSecurityUtil.getContextEnterpriseId()).thenReturn(1L);
        when(enterpriseSecurityUtil.getCurrentUser()).thenReturn(Optional.empty());
        when(salesInvoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
        when(salesInvoiceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(invoice));
        when(creditNoteRepository.sumIssuedBySalesInvoice(10L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(50L);
            return payment;
        });
    }

    @Test
    void partialPaymentUpdatesPaidAmountBalanceAndStatus() {
        when(paymentRepository.sumConfirmedBySalesInvoice(10L))
                .thenReturn(BigDecimal.ZERO, new BigDecimal("40.00"));

        service.registerPayment(new PaymentRequest(FinancialDocumentType.SALES, 10L,
                new BigDecimal("40.00"), LocalDate.now(), "bank transfer", null, null));

        assertEquals(new BigDecimal("40.00"), invoice.getMontantPaye());
        assertEquals(new BigDecimal("60.00"), invoice.getSoldeRestant());
        assertEquals("PARTIELLEMENT_PAYEE", invoice.getStatut());
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals("BANK TRANSFER", captor.getValue().getPaymentMethod());
        assertEquals(PaymentStatus.CONFIRMED, captor.getValue().getStatus());
    }

    @Test
    void paymentCannotExceedOutstandingBalance() {
        when(paymentRepository.sumConfirmedBySalesInvoice(10L)).thenReturn(new BigDecimal("80.00"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.registerPayment(new PaymentRequest(FinancialDocumentType.SALES, 10L,
                        new BigDecimal("25.00"), LocalDate.now(), "cash", null, null)));

        assertTrue(error.getMessage().contains("outstanding balance"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void issuedCreditNoteReducesTheInvoiceBalance() {
        when(creditNoteRepository.sumIssuedBySalesInvoice(10L)).thenReturn(new BigDecimal("25.00"));
        when(paymentRepository.sumConfirmedBySalesInvoice(10L)).thenReturn(new BigDecimal("20.00"));

        var balance = service.getBalance(FinancialDocumentType.SALES, 10L);

        assertEquals(new BigDecimal("75.00"), balance.netAmount());
        assertEquals(new BigDecimal("55.00"), balance.balance());
        assertEquals(new BigDecimal("25.00"), balance.creditedAmount());
    }
}
