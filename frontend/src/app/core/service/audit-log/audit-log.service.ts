import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface AuditLog {
  id: number;
  action: string;
  details: string;
  username: string;
  userId: number;
  entrepriseId: number;
  ipAddress: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {
  private baseUrl = `${environment.apiUrl}/api/audit-logs`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  getAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getAuditLogsByEntreprise(entrepriseId: number): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.baseUrl}/entreprise/${entrepriseId}`, { headers: this.getHeaders() });
  }
}
