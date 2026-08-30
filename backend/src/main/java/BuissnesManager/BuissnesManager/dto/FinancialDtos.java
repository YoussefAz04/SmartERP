package BuissnesManager.BuissnesManager.dto;

import BuissnesManager.BuissnesManager.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class FinancialDtos {
    private FinancialDtos() {
    }

    public record PaymentRequest(
            @NotNull FinancialDocumentType documentType,
            @NotNull Long invoiceId,
            @NotNull @Positive BigDecimal amount,
            @NotNull LocalDate paymentDate,
            @NotBlank @Size(max = 40) String paymentMethod,
            @Size(max = 80) String reference,
            @Size(max = 500) String notes) {
    }

    public record PaymentResponse(
            Long id, String reference, FinancialDocumentType documentType,
            Long invoiceId, String invoiceReference, BigDecimal amount,
            LocalDate paymentDate, String paymentMethod, PaymentStatus status,
            String notes, LocalDateTime createdAt) {
    }

    public record InvoiceBalance(
            FinancialDocumentType documentType, Long invoiceId, String invoiceReference,
            Long partyId, String partyName, LocalDate dueDate, BigDecimal grossAmount,
            BigDecimal creditedAmount, BigDecimal netAmount, BigDecimal paidAmount,
            BigDecimal balance, BigDecimal creditBalance, String status, boolean overdue) {
    }

    public record FinancialSummary(
            BigDecimal accountsReceivable, BigDecimal accountsPayable,
            BigDecimal overdueReceivable, BigDecimal overduePayable,
            long overdueCustomerInvoices, long overdueSupplierInvoices) {
    }

    public record ReturnRequest(
            @NotNull ReturnType returnType,
            @NotNull Long invoiceId,
            @NotNull LocalDate returnDate,
            @NotBlank @Size(max = 500) String reason,
            @NotEmpty List<@Valid ReturnLineRequest> lines) {
    }

    public record ReturnLineRequest(
            @NotNull Long orderLineId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal quantity) {
    }

    public record EligibleReturnLine(
            Long orderLineId, Long articleId, String articleName,
            BigDecimal invoicedQuantity, BigDecimal alreadyReturned,
            BigDecimal returnableQuantity, BigDecimal unitPriceHT, BigDecimal taxRate) {
    }

    public record ReturnLineResponse(
            Long id, Long orderLineId, Long articleId, String articleName,
            BigDecimal quantity, BigDecimal unitPriceHT, BigDecimal taxRate,
            BigDecimal amountHT, BigDecimal amountTotal) {
    }

    public record ReturnResponse(
            Long id, String reference, ReturnType returnType, ReturnStatus status,
            LocalDate returnDate, String reason, Long invoiceId, String invoiceReference,
            BigDecimal amountHT, BigDecimal amountTax, BigDecimal amountTotal,
            String creditNoteReference, List<ReturnLineResponse> lines,
            LocalDateTime createdAt) {
    }
}
