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
    return new HttpHeaders({ 'Content-Type': 'application/json' });
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
