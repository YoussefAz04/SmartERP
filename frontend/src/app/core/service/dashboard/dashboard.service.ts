import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface RecentClientSummary {
  id: number;
  nom: string;
  prenom?: string;
  email?: string;
  telephone?: string;
}

export interface RecentFournisseurSummary {
  id: number;
  nom: string;
  telephone?: string;
  ville?: string;
}

export interface RecentStockSummary {
  id: number;
  articleAchatId?: number;
  articleNom?: string;
  quantiteDisponible: number;
}

export interface RecentCommandeSummary {
  id: number;
  dateCommande?: string;
  montantTTC: number;
  statut?: string;
  clientNom?: string;
}

export interface DashboardHomeSummary {
  totalClients: number;
  totalFournisseurs: number;
  totalArticles: number;
  totalCommandes: number;
  recentClients: RecentClientSummary[];
  recentFournisseurs: RecentFournisseurSummary[];
  recentStocks: RecentStockSummary[];
  recentCommandes: RecentCommandeSummary[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private baseUrl = `${environment.apiUrl}/api/dashboard`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const item = localStorage.getItem('myLSkey');
    let token: string | null = null;
    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        token = user.token || null;
      } catch {
        token = null;
      }
    }
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  /**
   * Fetches counts + 5 most-recent rows of clients, fournisseurs, stocks and
   * commandes for the current enterprise in a single, server-aggregated and
   * cached call. Replaces the previous "fetch every list and slice 5" pattern
   * that made the admin dashboard slow on tenants with lots of data.
   */
  getHomeSummary(): Observable<DashboardHomeSummary> {
    return this.http.get<DashboardHomeSummary>(
      `${this.baseUrl}/home-summary`,
      { headers: this.getHeaders() }
    );
  }
}
