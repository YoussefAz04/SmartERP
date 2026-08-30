import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { catchError, forkJoin, of } from 'rxjs';
import { FinanceService } from '../../core/service/finance/finance.service';
import {
  EligibleReturnLine, FinancialDocumentType, FinancialSummary,
  InvoiceBalance, PaymentRecord, ReturnRecord, ReturnType
} from '../../model/finance.model';

interface ReturnLineInput extends EligibleReturnLine {
  selected: boolean;
  quantity: number;
}

@Component({
  selector: 'app-finance',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './finance.component.html',
  styleUrl: './finance.component.scss'
})
export class FinanceComponent implements OnInit {
  activeTab: 'overview' | 'payments' | 'returns' | 'statements' = 'overview';
  isLoading = true;
  isSaving = false;
  errorMessage = '';
  successMessage = '';

  summary: FinancialSummary = {
    accountsReceivable: 0, accountsPayable: 0,
    overdueReceivable: 0, overduePayable: 0,
    overdueCustomerInvoices: 0, overdueSupplierInvoices: 0
  };
  salesInvoices: InvoiceBalance[] = [];
  purchaseInvoices: InvoiceBalance[] = [];
  payments: PaymentRecord[] = [];
  returns: ReturnRecord[] = [];

  paymentType: FinancialDocumentType = 'SALES';
  paymentForm = this.emptyPaymentForm();
  dueDate = '';

  returnType: ReturnType = 'CUSTOMER_RETURN';
  returnForm = this.emptyReturnForm();
  returnLines: ReturnLineInput[] = [];

  statementType: FinancialDocumentType = 'SALES';
  statementSearch = '';

  readonly paymentMethods = ['CASH', 'BANK_TRANSFER', 'CARD', 'CHEQUE', 'OTHER'];

  constructor(private financeService: FinanceService, private translate: TranslateService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';
    forkJoin({
      summary: this.financeService.getSummary(),
      sales: this.financeService.getInvoices('SALES'),
      purchases: this.financeService.getInvoices('PURCHASE'),
      payments: this.financeService.getPayments(),
      returns: this.financeService.getReturns()
    }).pipe(catchError(() => {
      this.errorMessage = 'finance.messages.loadError';
      return of(null);
    })).subscribe(result => {
      if (result) {
        this.summary = result.summary;
        this.salesInvoices = result.sales;
        this.purchaseInvoices = result.purchases;
        this.payments = result.payments;
        this.returns = result.returns;
        this.financeService.scanOverdue().subscribe();
      }
      this.isLoading = false;
    });
  }

  get paymentInvoices(): InvoiceBalance[] {
    return (this.paymentType === 'SALES' ? this.salesInvoices : this.purchaseInvoices)
      .filter(invoice => invoice.balance > 0);
  }

  get returnInvoices(): InvoiceBalance[] {
    return this.returnType === 'CUSTOMER_RETURN' ? this.salesInvoices : this.purchaseInvoices;
  }

  get statementInvoices(): InvoiceBalance[] {
    const query = this.statementSearch.trim().toLowerCase();
    return (this.statementType === 'SALES' ? this.salesInvoices : this.purchaseInvoices)
      .filter(invoice => !query || invoice.partyName.toLowerCase().includes(query)
        || invoice.invoiceReference.toLowerCase().includes(query));
  }

  get selectedPaymentInvoice(): InvoiceBalance | undefined {
    return this.paymentInvoices.find(invoice => invoice.invoiceId === Number(this.paymentForm.invoiceId));
  }

  exportStatementCsv(): void {
    const headers = [
      'invoice', 'party', 'dueDate', 'gross', 'credits', 'paid', 'balance', 'status'
    ].map(key => this.translate.instant(`finance.table.${key}`));
    const rows = this.statementInvoices.map(invoice => [
      invoice.invoiceReference,
      invoice.partyName,
      invoice.dueDate || '',
      invoice.grossAmount.toFixed(2),
      invoice.creditedAmount.toFixed(2),
      invoice.paidAmount.toFixed(2),
      invoice.balance.toFixed(2),
      this.translate.instant(invoice.overdue
        ? 'finance.status.overdue'
        : `finance.status.${invoice.status}`)
    ]);
    const csv = [headers, ...rows]
      .map(row => row.map(value => this.csvCell(value)).join(';'))
      .join('\r\n');
    const blob = new Blob(['\uFEFF', csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${this.translate.instant(
      this.statementType === 'SALES'
        ? 'finance.statement.customerFileName'
        : 'finance.statement.supplierFileName'
    )}-${this.today()}.csv`;
    link.click();
    URL.revokeObjectURL(url);
    this.successMessage = 'finance.messages.statementExported';
  }

  onPaymentTypeChange(): void {
    this.paymentForm = this.emptyPaymentForm();
    this.dueDate = '';
  }

  onPaymentInvoiceChange(): void {
    const invoice = this.selectedPaymentInvoice;
    this.paymentForm.amount = invoice?.balance || 0;
    this.dueDate = invoice?.dueDate || '';
  }

  savePayment(): void {
    const invoice = this.selectedPaymentInvoice;
    if (!invoice || this.paymentForm.amount <= 0 || this.paymentForm.amount > invoice.balance) {
      this.errorMessage = 'finance.messages.invalidPayment';
      return;
    }
    this.isSaving = true;
    this.financeService.registerPayment({
      documentType: this.paymentType,
      invoiceId: invoice.invoiceId,
      amount: this.paymentForm.amount,
      paymentDate: this.paymentForm.paymentDate,
      paymentMethod: this.paymentForm.paymentMethod,
      notes: this.paymentForm.notes
    }).subscribe({
      next: () => this.finishSave('finance.messages.paymentSaved'),
      error: error => this.failSave(error)
    });
  }

  saveDueDate(): void {
    const invoice = this.selectedPaymentInvoice;
    if (!invoice || !this.dueDate) return;
    this.isSaving = true;
    this.financeService.updateDueDate(this.paymentType, invoice.invoiceId, this.dueDate).subscribe({
      next: () => this.finishSave('finance.messages.dueDateSaved'),
      error: error => this.failSave(error)
    });
  }

  cancelPayment(payment: PaymentRecord): void {
    if (!confirm(this.translate.instant('finance.confirm.cancelPayment'))) return;
    this.financeService.cancelPayment(payment.id).subscribe({
      next: () => this.finishSave('finance.messages.paymentCancelled'),
      error: error => this.failSave(error)
    });
  }

  onReturnTypeChange(): void {
    this.returnForm = this.emptyReturnForm();
    this.returnLines = [];
  }

  onReturnInvoiceChange(): void {
    this.returnLines = [];
    if (!this.returnForm.invoiceId) return;
    this.financeService.getEligibleLines(this.returnType, Number(this.returnForm.invoiceId)).subscribe({
      next: lines => this.returnLines = lines.map(line => ({ ...line, selected: false, quantity: 1 })),
      error: error => this.failSave(error)
    });
  }

  createReturn(): void {
    const lines = this.returnLines.filter(line => line.selected && line.quantity > 0)
      .map(line => ({ orderLineId: line.orderLineId, quantity: line.quantity }));
    if (!this.returnForm.invoiceId || !this.returnForm.reason.trim() || lines.length === 0) {
      this.errorMessage = 'finance.messages.invalidReturn';
      return;
    }
    this.isSaving = true;
    this.financeService.createReturn({
      returnType: this.returnType,
      invoiceId: Number(this.returnForm.invoiceId),
      returnDate: this.returnForm.returnDate,
      reason: this.returnForm.reason,
      lines
    }).subscribe({
      next: () => this.finishSave('finance.messages.returnDraftSaved'),
      error: error => this.failSave(error)
    });
  }

  validateReturn(item: ReturnRecord): void {
    if (!confirm(this.translate.instant('finance.confirm.validateReturn'))) return;
    this.isSaving = true;
    this.financeService.validateReturn(item.id).subscribe({
      next: () => this.finishSave('finance.messages.returnValidated'),
      error: error => this.failSave(error)
    });
  }

  cancelReturn(item: ReturnRecord): void {
    if (!confirm(this.translate.instant('finance.confirm.cancelReturn'))) return;
    this.isSaving = true;
    this.financeService.cancelReturn(item.id).subscribe({
      next: () => this.finishSave('finance.messages.returnCancelled'),
      error: error => this.failSave(error)
    });
  }

  private finishSave(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    this.paymentForm = this.emptyPaymentForm();
    this.returnForm = this.emptyReturnForm();
    this.returnLines = [];
    this.isSaving = false;
    this.loadData();
  }

  private failSave(error: any): void {
    this.errorMessage = error?.error?.message || 'finance.messages.saveError';
    this.isSaving = false;
  }

  private emptyPaymentForm() {
    return { invoiceId: null as number | null, amount: 0, paymentDate: this.today(), paymentMethod: 'BANK_TRANSFER', notes: '' };
  }

  private emptyReturnForm() {
    return { invoiceId: null as number | null, returnDate: this.today(), reason: '' };
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private csvCell(value: string | number): string {
    const text = String(value ?? '');
    return /[;"\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
  }
}
