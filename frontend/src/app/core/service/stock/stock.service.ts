import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Stock, StockDto } from '../../../model/stock.model';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private baseUrl = `${environment.apiUrl}/api/stocks`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  getAllStocks(): Observable<Stock[]> {
    return this.http.get<Stock[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getStockById(id: number): Observable<Stock> {
    return this.http.get<Stock>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  getStocksByArticleAchat(articleAchatId: number): Observable<Stock[]> {
    return this.http.get<Stock[]>(`${this.baseUrl}/article-achat/${articleAchatId}`, { headers: this.getHeaders() });
  }

  createStock(stock: StockDto): Observable<Stock> {
    return this.http.post<Stock>(this.baseUrl, stock, { headers: this.getHeaders() });
  }

  updateStock(id: number, stock: StockDto): Observable<Stock> {
    return this.http.put<Stock>(`${this.baseUrl}/${id}`, stock, { headers: this.getHeaders() });
  }

  deleteStock(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}
