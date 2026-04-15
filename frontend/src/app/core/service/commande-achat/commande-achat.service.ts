import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CommandeAchat } from '../../../model/commande-achat.model';

@Injectable({
  providedIn: 'root'
})
export class CommandeAchatService {
  private baseUrl = `${environment.apiUrl}/api/commandes-achat`;

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
    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        return user.token || null;
      } catch (error) {
        return null;
      }
    }
    return null;
  }

  getAllCommandes(): Observable<CommandeAchat[]> {
    return this.http.get<CommandeAchat[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getCommandeById(id: number): Observable<CommandeAchat> {
    return this.http.get<CommandeAchat>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  getCommandesByEntreprise(entrepriseId: number): Observable<CommandeAchat[]> {
    return this.http.get<CommandeAchat[]>(`${this.baseUrl}/entreprise/${entrepriseId}`, { headers: this.getHeaders() });
  }

  createCommande(payload: any): Observable<any> {
  return this.http.post<any>(`${this.baseUrl}/add`, payload, { headers: this.getHeaders() });
  // ↑ ADD /add HERE
}

  updateCommande(id: number, commande: CommandeAchat): Observable<CommandeAchat> {
    return this.http.put<CommandeAchat>(`${this.baseUrl}/${id}`, commande, { headers: this.getHeaders() });
  }

  validerCommande(id: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}/valider`, {}, { headers: this.getHeaders() });
  }

  recevoirCommande(id: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}/recevoir`, {}, { headers: this.getHeaders() });
  }

  changeStatutToEnAttente(id: number): Observable<CommandeAchat> {
    return this.http.patch<CommandeAchat>(`${this.baseUrl}/${id}/statut?statut=EN_ATTENTE`, {}, { headers: this.getHeaders() });
  }

  annulerCommande(id: number): Observable<CommandeAchat> {
    return this.http.put<CommandeAchat>(`${this.baseUrl}/${id}/annuler`, {}, { headers: this.getHeaders() });
  }

  deleteCommande(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}
