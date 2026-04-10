import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ArticleVente } from '../../../model/article-vente.model';

@Injectable({
  providedIn: 'root'
})
export class ArticleVenteService {
  private baseUrl = `${environment.apiUrl}/articles-vente`;

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

  getAllArticlesVente(): Observable<ArticleVente[]> {
    return this.http.get<ArticleVente[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getArticleVenteById(id: number): Observable<ArticleVente> {
    return this.http.get<ArticleVente>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  getArticleVenteByClient(clientId: number): Observable<ArticleVente[]> {
    return this.http.get<ArticleVente[]>(`${this.baseUrl}/client/${clientId}`, { headers: this.getHeaders() });
  }

  addArticleVente(articleVente: ArticleVente, articleAchatId?: number, clientId?: number): Observable<ArticleVente> {
    let params = new HttpParams();
    if (articleAchatId) params = params.set('articleAchatId', articleAchatId.toString());
    if (clientId) params = params.set('clientId', clientId.toString());
    return this.http.post<ArticleVente>(`${this.baseUrl}/add`, articleVente, {
      headers: this.getHeaders(),
      params
    });
  }

  updateArticleVente(id: number, articleVente: ArticleVente): Observable<ArticleVente> {
    return this.http.put<ArticleVente>(`${this.baseUrl}/${id}`, articleVente, { headers: this.getHeaders() });
  }

  deleteArticleVente(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}
