import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  SavAgent, SavApprovalStatus, SavHistory, SavIntervention, SavInterventionRequest,
  SavSaleItem, SavStats, SavStatus, SavTicket, SavTicketRequest
} from '../../../model/sav-ticket.model';

@Injectable({
  providedIn: 'root'
})
export class SavService {
  private baseUrl = `${environment.apiUrl}/api/sav`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = this.getToken();
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  private getToken(): string | null {
    const item = localStorage.getItem('myLSkey');
    if (!item) return null;
    try {
      const user = JSON.parse(decodeURIComponent(escape(atob(item))));
      return user.token || null;
    } catch {
      return null;
    }
  }

  getTickets(): Observable<SavTicket[]> {
    return this.http.get<SavTicket[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getTicket(id: number): Observable<SavTicket> {
    return this.http.get<SavTicket>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  getStats(): Observable<SavStats> {
    return this.http.get<SavStats>(`${this.baseUrl}/stats`, { headers: this.getHeaders() });
  }

  getAgents(): Observable<SavAgent[]> {
    return this.http.get<SavAgent[]>(`${this.baseUrl}/agents`, { headers: this.getHeaders() });
  }

  getEligibleSales(clientId: number): Observable<SavSaleItem[]> {
    return this.http.get<SavSaleItem[]>(`${this.baseUrl}/eligible-sales`, {
      headers: this.getHeaders(), params: { clientId }
    });
  }

  getHistory(ticketId: number): Observable<SavHistory[]> {
    return this.http.get<SavHistory[]>(`${this.baseUrl}/${ticketId}/history`, { headers: this.getHeaders() });
  }

  addNote(ticketId: number, message: string): Observable<SavHistory> {
    return this.http.post<SavHistory>(
      `${this.baseUrl}/${ticketId}/notes`,
      { message },
      { headers: this.getHeaders() }
    );
  }

  getInterventions(ticketId: number): Observable<SavIntervention[]> {
    return this.http.get<SavIntervention[]>(`${this.baseUrl}/${ticketId}/interventions`, { headers: this.getHeaders() });
  }

  addIntervention(ticketId: number, request: SavInterventionRequest): Observable<SavIntervention> {
    return this.http.post<SavIntervention>(`${this.baseUrl}/${ticketId}/interventions`, request, { headers: this.getHeaders() });
  }

  deleteIntervention(ticketId: number, interventionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${ticketId}/interventions/${interventionId}`, { headers: this.getHeaders() });
  }

  updateApproval(ticketId: number, approvalStatus: SavApprovalStatus): Observable<SavTicket> {
    return this.http.patch<SavTicket>(
      `${this.baseUrl}/${ticketId}/approval`, { approvalStatus }, { headers: this.getHeaders() }
    );
  }

  createTicket(request: SavTicketRequest): Observable<SavTicket> {
    return this.http.post<SavTicket>(this.baseUrl, request, { headers: this.getHeaders() });
  }

  updateTicket(id: number, request: SavTicketRequest): Observable<SavTicket> {
    return this.http.put<SavTicket>(`${this.baseUrl}/${id}`, request, { headers: this.getHeaders() });
  }

  updateStatus(id: number, status: SavStatus): Observable<SavTicket> {
    return this.http.patch<SavTicket>(`${this.baseUrl}/${id}/status`, { status }, { headers: this.getHeaders() });
  }

  deleteTicket(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}
