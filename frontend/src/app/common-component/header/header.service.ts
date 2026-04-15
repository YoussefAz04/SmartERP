import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, forkJoin } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

export interface SearchResult {
  id: number;
  nom: string;
  type: 'Client' | 'Article' | 'Fournisseur' | 'Commande';
  detail?: string;
}

export interface Notification {
  id: number;
  message: string;
  statut: 'NOUVEAU' | 'VU' | 'IGNORE';
  createdAt: string;
  type?: string;
}

@Injectable({
  providedIn: 'root'
})
export class HeaderService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  private getEntrepriseId(): number | null {
    const item = localStorage.getItem('myLSkey');
    if (!item) return null;
    try {
      const user = JSON.parse(decodeURIComponent(escape(atob(item))));
      return user.entrepriseId || null;
    } catch {
      return null;
    }
  }

  searchGlobal(query: string): Observable<SearchResult[]> {
    const lowerQuery = query.toLowerCase();
    const entrepriseId = this.getEntrepriseId();

    // Use enterprise-scoped endpoints when available
    const clientsUrl = entrepriseId
      ? `${this.apiUrl}/clients/byEntreprise/${entrepriseId}`
      : `${this.apiUrl}/clients/getAllClients`;
    const articlesUrl = entrepriseId
      ? `${this.apiUrl}/api/articles/entreprise/${entrepriseId}`
      : `${this.apiUrl}/api/articles`;
    const fournisseursUrl = entrepriseId
      ? `${this.apiUrl}/fournisseurs/byEntreprise/${entrepriseId}`
      : `${this.apiUrl}/fournisseurs/getAllFournisseurs`;

    const clients$ = this.http.get<any[]>(clientsUrl).pipe(
      map(clients => (clients || [])
        .filter(c => c.nom && c.nom.toLowerCase().includes(lowerQuery))
        .slice(0, 5)
        .map(c => ({ id: c.id, nom: c.nom, type: 'Client' as const, detail: c.email }))),
      catchError(() => of([] as SearchResult[]))
    );
    const articles$ = this.http.get<any[]>(articlesUrl).pipe(
      map(articles => (articles || [])
        .filter(a => (a.nom && a.nom.toLowerCase().includes(lowerQuery)) ||
                     (a.reference && a.reference.toLowerCase().includes(lowerQuery)))
        .slice(0, 5)
        .map(a => ({ id: a.id, nom: a.nom || a.reference, type: 'Article' as const, detail: a.reference }))),
      catchError(() => of([] as SearchResult[]))
    );
    const fournisseurs$ = this.http.get<any[]>(fournisseursUrl).pipe(
      map(fournisseurs => (fournisseurs || [])
        .filter(f => f.nom && f.nom.toLowerCase().includes(lowerQuery))
        .slice(0, 5)
        .map(f => ({ id: f.id, nom: f.nom, type: 'Fournisseur' as const, detail: f.email }))),
      catchError(() => of([] as SearchResult[]))
    );

    return forkJoin([clients$, articles$, fournisseurs$]).pipe(
      map(([clients, articles, fournisseurs]) => [...clients, ...articles, ...fournisseurs]),
      catchError(() => of([] as SearchResult[]))
    );
  }

  getNotifications(entrepriseId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/api/notifications/${entrepriseId}`).pipe(
      catchError(() => of([] as Notification[]))
    );
  }

  markNotificationRead(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/api/notifications/${id}/mark-viewed`, {}).pipe(
      catchError(() => of(null))
    );
  }

  markAllNotificationsRead(entrepriseId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/api/notifications/markAll/${entrepriseId}`, {}).pipe(
      catchError(() => of(null))
    );
  }

  getUserProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/users/me`).pipe(
      catchError(() => of(null))
    );
  }
}