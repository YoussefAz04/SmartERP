import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StockMouvement, StockMouvementDto } from '../../../model/stock-mouvement.model';

@Injectable({
  providedIn: 'root'
})
export class StockMouvementService {
  private baseUrl = `${environment.apiUrl}/api/stock-mouvement`;

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

  getAllMovements(): Observable<StockMouvement[]> {
    return this.http.get<StockMouvement[]>(`${this.baseUrl}/all`, { headers: this.getHeaders() });
  }

  getMovementById(id: number): Observable<StockMouvement> {
    return this.http.get<StockMouvement>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  getMovementsByStock(stockId: number): Observable<StockMouvement[]> {
    return this.http.get<StockMouvement[]>(`${this.baseUrl}/stock/${stockId}`, { headers: this.getHeaders() });
  }

  getMovementsByType(type: string): Observable<StockMouvement[]> {
    return this.http.get<StockMouvement[]>(`${this.baseUrl}/type/${type}`, { headers: this.getHeaders() });
  }

  getMovementsByUser(utilisateurId: number): Observable<StockMouvement[]> {
    return this.http.get<StockMouvement[]>(`${this.baseUrl}/user/${utilisateurId}`, { headers: this.getHeaders() });
  }

  createMovement(dto: StockMouvementDto): Observable<StockMouvement> {
    return this.http.post<StockMouvement>(this.baseUrl, dto, { headers: this.getHeaders() });
  }

  deleteMovement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}
