package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.FinancialDtos.*;
import BuissnesManager.BuissnesManager.entity.FinancialDocumentType;
import BuissnesManager.BuissnesManager.service.FinancialService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
public class FinancialController {
    private final FinancialService financialService;

    public FinancialController(FinancialService financialService) {
        this.financialService = financialService;
    }

    @GetMapping("/summary")
    public FinancialSummary summary() {
        return financialService.getSummary();
    }

    @GetMapping("/invoices")
    public List<InvoiceBalance> invoices(@RequestParam FinancialDocumentType type,
                                         @RequestParam(required = false) Long partyId,
                                         @RequestParam(defaultValue = "false") boolean outstandingOnly) {
        return financialService.getInvoices(type, partyId, outstandingOnly);
    }

    @PutMapping("/invoices/{type}/{invoiceId}/due-date")
    public InvoiceBalance updateDueDate(@PathVariable FinancialDocumentType type,
                                        @PathVariable Long invoiceId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        return financialService.updateDueDate(type, invoiceId, dueDate);
    }

    @GetMapping("/payments")
    public List<PaymentResponse> payments() {
        return financialService.getPayments();
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> registerPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(financialService.registerPayment(request));
    }

    @PostMapping("/payments/{id}/cancel")
    public PaymentResponse cancelPayment(@PathVariable Long id) {
        return financialService.cancelPayment(id);
    }

    @PostMapping("/notifications/scan-overdue")
    public Map<String, Integer> scanOverdue() {
        return Map.of("created", financialService.createOverdueNotifications());
    }
}
