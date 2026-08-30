import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CommandeVente } from '../../../model/commande-vente.model';

@Injectable({
  providedIn: 'root'
})
export class CommandeVenteService {
  private baseUrl = `${environment.apiUrl}/api/commandes-vente`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  getAllCommandes(): Observable<CommandeVente[]> {
    return this.http.get<CommandeVente[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getCommandeById(id: number): Observable<CommandeVente> {
    return this.http.get<CommandeVente>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  getCommandesByEntreprise(entrepriseId: number): Observable<CommandeVente[]> {
    return this.http.get<CommandeVente[]>(`${this.baseUrl}/entreprise/${entrepriseId}`, { headers: this.getHeaders() });
  }

  createCommande(commande: CommandeVente): Observable<CommandeVente> {
  return this.http.post<CommandeVente>(`${this.baseUrl}/add`, commande, { headers: this.getHeaders() });
  // ✅ Now: POST /api/commandes-vente/add (matches backend)
}

  updateCommande(id: number, commande: CommandeVente): Observable<CommandeVente> {
    return this.http.put<CommandeVente>(`${this.baseUrl}/${id}`, commande, { headers: this.getHeaders() });
  }

  validerCommande(id: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}/valider`, {}, { headers: this.getHeaders() });
  }

  livrerCommande(id: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}/livrer`, {}, { headers: this.getHeaders() });
  }

  annulerCommande(id: number): Observable<CommandeVente> {
    return this.http.put<CommandeVente>(`${this.baseUrl}/${id}/annuler`, {}, { headers: this.getHeaders() });
  }

  deleteCommande(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}
