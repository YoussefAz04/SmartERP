import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  EligibleReturnLine, FinancialDocumentType, FinancialSummary,
  InvoiceBalance, PaymentRecord, ReturnRecord, ReturnType
} from '../../../model/finance.model';

@Injectable({ providedIn: 'root' })
export class FinanceService {
  private readonly financeUrl = `${environment.apiUrl}/api/finance`;
  private readonly returnsUrl = `${environment.apiUrl}/api/returns`;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<FinancialSummary> {
    return this.http.get<FinancialSummary>(`${this.financeUrl}/summary`);
  }

  getInvoices(type: FinancialDocumentType, outstandingOnly = false, partyId?: number): Observable<InvoiceBalance[]> {
    let params = new HttpParams().set('type', type).set('outstandingOnly', outstandingOnly);
    if (partyId != null) params = params.set('partyId', partyId);
    return this.http.get<InvoiceBalance[]>(`${this.financeUrl}/invoices`, { params });
  }

  updateDueDate(type: FinancialDocumentType, invoiceId: number, dueDate: string): Observable<InvoiceBalance> {
    return this.http.put<InvoiceBalance>(`${this.financeUrl}/invoices/${type}/${invoiceId}/due-date`, null,
      { params: { dueDate } });
  }

  getPayments(): Observable<PaymentRecord[]> {
    return this.http.get<PaymentRecord[]>(`${this.financeUrl}/payments`);
  }

  registerPayment(request: object): Observable<PaymentRecord> {
    return this.http.post<PaymentRecord>(`${this.financeUrl}/payments`, request);
  }

  cancelPayment(id: number): Observable<PaymentRecord> {
    return this.http.post<PaymentRecord>(`${this.financeUrl}/payments/${id}/cancel`, {});
  }

  scanOverdue(): Observable<{ created: number }> {
    return this.http.post<{ created: number }>(`${this.financeUrl}/notifications/scan-overdue`, {});
  }

  getReturns(): Observable<ReturnRecord[]> {
    return this.http.get<ReturnRecord[]>(this.returnsUrl);
  }

  getEligibleLines(type: ReturnType, invoiceId: number): Observable<EligibleReturnLine[]> {
    return this.http.get<EligibleReturnLine[]>(`${this.returnsUrl}/eligible-lines`,
      { params: { type, invoiceId } });
  }

  createReturn(request: object): Observable<ReturnRecord> {
    return this.http.post<ReturnRecord>(this.returnsUrl, request);
  }

  validateReturn(id: number): Observable<ReturnRecord> {
    return this.http.post<ReturnRecord>(`${this.returnsUrl}/${id}/validate`, {});
  }

  cancelReturn(id: number): Observable<ReturnRecord> {
    return this.http.post<ReturnRecord>(`${this.returnsUrl}/${id}/cancel`, {});
  }
}
