import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, forkJoin } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

export interface SearchResult {
  id: number;
  nom: string;
  type: 'Client' | 'Article' | 'Commande';
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

  searchGlobal(query: string): Observable<SearchResult[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<SearchResult[]>(`${this.apiUrl}/api/search`, { params }).pipe(
      catchError(() => {
        const lowerQuery = query.toLowerCase();
        const clients$ = this.http.get<any[]>(`${this.apiUrl}/clients/getAllClients`).pipe(
          map(clients => clients
            .filter(c => c.nom && c.nom.toLowerCase().includes(lowerQuery))
            .map(c => ({ id: c.id, nom: c.nom, type: 'Client' as const, detail: c.email }))),
          catchError(() => of([] as SearchResult[]))
        );
        const articles$ = this.http.get<any[]>(`${this.apiUrl}/articles/getAll`).pipe(
          map(articles => articles
            .filter(a => a.nom && a.nom.toLowerCase().includes(lowerQuery))
            .map(a => ({ id: a.id, nom: a.nom, type: 'Article' as const, detail: a.reference }))),
          catchError(() => of([] as SearchResult[]))
        );
        return forkJoin([clients$, articles$]).pipe(
          map(([clients, articles]) => [...clients, ...articles]),
          catchError(() => of([] as SearchResult[]))
        );
      })
    );
  }

  // ✅ UPDATED: Accept entrepriseId as parameter
  getNotifications(entrepriseId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/api/notifications/${entrepriseId}`).pipe(
      catchError((error) => {
        console.error('❌ Error fetching notifications:', error);
        return of([] as Notification[]);
      })
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