import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Sav, SavIntervention } from '../../../model/sav.model';

@Injectable({ providedIn: 'root' })
export class SavService {
  private baseUrl = `${environment.apiUrl}/api/sav`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    const token = this.getToken();
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

  getAll(): Observable<Sav[]> {
    return this.http.get<Sav[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getByEntreprise(entrepriseId: number): Observable<Sav[]> {
    return this.http.get<Sav[]>(`${this.baseUrl}/entreprise/${entrepriseId}`, {
      headers: this.getHeaders(),
    });
  }

  getByClient(clientId: number): Observable<Sav[]> {
    return this.http.get<Sav[]>(`${this.baseUrl}/client/${clientId}`, {
      headers: this.getHeaders(),
    });
  }

  getById(id: number): Observable<Sav> {
    return this.http.get<Sav>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  create(payload: Sav): Observable<Sav> {
    return this.http.post<Sav>(`${this.baseUrl}/add`, payload, { headers: this.getHeaders() });
  }

  update(id: number, payload: Sav): Observable<Sav> {
    return this.http.put<Sav>(`${this.baseUrl}/${id}`, payload, { headers: this.getHeaders() });
  }

  delete(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  changeStatut(id: number, statut: string, commentaire?: string): Observable<Sav> {
    return this.http.put<Sav>(
      `${this.baseUrl}/${id}/statut`,
      { statut, commentaire: commentaire || '' },
      { headers: this.getHeaders() }
    );
  }

  getInterventions(id: number): Observable<SavIntervention[]> {
    return this.http.get<SavIntervention[]>(`${this.baseUrl}/${id}/interventions`, {
      headers: this.getHeaders(),
    });
  }

  addIntervention(id: number, body: SavIntervention): Observable<SavIntervention> {
    return this.http.post<SavIntervention>(`${this.baseUrl}/${id}/interventions`, body, {
      headers: this.getHeaders(),
    });
  }

  stats(entrepriseId?: number): Observable<Record<string, unknown>> {
    const url = entrepriseId
      ? `${this.baseUrl}/stats?entrepriseId=${entrepriseId}`
      : `${this.baseUrl}/stats`;
    return this.http.get<Record<string, unknown>>(url, { headers: this.getHeaders() });
  }
}
