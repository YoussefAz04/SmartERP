import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ArticleAchat } from '../../../model/article-achat.model';

@Injectable({
  providedIn: 'root'
})
export class ArticleAchatService {
  private baseUrl = `${environment.apiUrl}/articles-achat`;

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

  getAllArticlesAchat(): Observable<ArticleAchat[]> {
    return this.http.get<ArticleAchat[]>(this.baseUrl, { headers: this.getHeaders() });
  }

  getArticleAchatById(id: number): Observable<ArticleAchat> {
    return this.http.get<ArticleAchat>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }

  getArticleAchatByFournisseur(fournisseurId: number): Observable<ArticleAchat[]> {
    return this.http.get<ArticleAchat[]>(`${this.baseUrl}/fournisseur/${fournisseurId}`, { headers: this.getHeaders() });
  }

  getArticleAchatByCategorie(categorieId: number): Observable<ArticleAchat[]> {
    return this.http.get<ArticleAchat[]>(`${this.baseUrl}/categorie/${categorieId}`, { headers: this.getHeaders() });
  }

  addArticleAchat(articleAchat: ArticleAchat): Observable<ArticleAchat> {
    return this.http.post<ArticleAchat>(`${this.baseUrl}/add`, articleAchat, { headers: this.getHeaders() });
  }

  getCategoriesArticle(): Observable<any[]> {
    const categoriesUrl = `${environment.apiUrl}/api/articles/categories`;
    return this.http.get<any[]>(categoriesUrl, { headers: this.getHeaders() });
  }

  updateArticleAchat(id: number, articleAchat: ArticleAchat): Observable<ArticleAchat> {
    return this.http.put<ArticleAchat>(`${this.baseUrl}/${id}`, articleAchat, { headers: this.getHeaders() });
  }

  deleteArticleAchat(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() });
  }
}