/* eslint-disable @typescript-eslint/no-explicit-any */
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, of, Subject } from 'rxjs';
import { catchError, takeUntil } from 'rxjs/operators';
import { ClientService } from 'src/app/core/service/client/client.service';
import { FournisseurService } from 'src/app/core/service/fournisseur/fournisseur.service';
import { ArticleAchatService } from 'src/app/core/service/article-achat/article-achat.service';
import { StockService } from 'src/app/core/service/stock/stock.service';
import { CommandeVenteService } from 'src/app/core/service/commande-vente/commande-vente.service';
import { Client } from 'src/app/model/client.model';
import { Fournisseur } from 'src/app/model/fournisseur.model';
import { Stock } from 'src/app/model/stock.model';
import { CommandeVente } from 'src/app/model/commande-vente.model';

export interface DashboardStats {
  totalClients: number;
  totalFournisseurs: number;
  totalArticles: number;
  totalCommandes: number;
}

export interface RecentArticleView {
  id: number;
  name: string;
  quantity: number;
  stockStatus: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  stockPercent: number;
}

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss',
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private readonly LOW_STOCK_THRESHOLD = 10;
  private readonly destroy$ = new Subject<void>();

  public userName = 'Administrateur';
  public entrepriseName = '';
  public entrepriseId: number | null = null;
  public today: Date = new Date();

  // Stats for count-up cards
  public stats: DashboardStats = { totalClients: 0, totalFournisseurs: 0, totalArticles: 0, totalCommandes: 0 };
  public statsLoaded = false;

  // Recent data
  public recentClients: Client[] = [];
  public recentFournisseurs: Fournisseur[] = [];
  public recentArticles: RecentArticleView[] = [];
  public recentCommandes: CommandeVente[] = [];

  // Loading states
  public loadingClients = true;
  public loadingFournisseurs = true;
  public loadingArticles = true;
  public loadingCommandes = true;

  // Error states
  public errorClients = false;
  public errorFournisseurs = false;
  public errorArticles = false;
  public errorCommandes = false;

  // Count-up options
  public countUpOptions = { duration: 2, separator: ' ' };

  constructor(
    private router: Router,
    private clientService: ClientService,
    private fournisseurService: FournisseurService,
    private articleService: ArticleAchatService,
    private stockService: StockService,
    private commandeService: CommandeVenteService,
  ) {}

  ngOnInit(): void {
    this.loadEnterpriseContext();
    this.loadAllData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // trackBy helpers — keep DOM nodes stable across change detection cycles so
  // Angular doesn't tear down and recreate every <tr> on each re-render.
  trackById(_index: number, item: { id?: number | null }): number {
    return item?.id ?? _index;
  }

  trackByCommandeId(_index: number, item: CommandeVente): number {
    return (item?.id as number) ?? _index;
  }

  loadEnterpriseContext(): void {
    try {
      const item = localStorage.getItem('myLSkey');
      if (item) {
        const session = JSON.parse(decodeURIComponent(escape(atob(item))));
        // Extract enterprise info from session
        this.entrepriseId = session.entrepriseId ?? null;
        this.entrepriseName = session.entrepriseName || session.entreprise?.nom || '';
        // Extract user display name from session or JWT payload
        this.userName = session.name || session.username || 'Administrateur';
        if (!this.userName || this.userName === 'Administrateur') {
          const token = session.token;
          if (token && token.split('.').length === 3) {
            const payload = JSON.parse(atob(token.split('.')[1]));
            this.userName = payload.firstName || payload.sub || 'Administrateur';
          }
        }
      }
    } catch {
      // keep defaults
    }
  }

  loadAllData(): void {
    // Each source uses catchError so a single API failure only marks its own section as errored.
    forkJoin({
      clients: this.clientService.getAllClients().pipe(
        catchError(err => this.handleLoadError(err, 'clients', () => this.errorClients = true))
      ),
      fournisseurs: this.fournisseurService.getAllFournisseurs().pipe(
        catchError(err => this.handleLoadError(err, 'fournisseurs', () => this.errorFournisseurs = true))
      ),
      articles: this.articleService.getAllArticlesAchat().pipe(
        catchError(err => this.handleLoadError(err, 'articles', () => this.errorArticles = true))
      ),
      stocks: this.stockService.getAllStocks().pipe(
        catchError(err => this.handleLoadError(err, 'stocks', () => this.errorArticles = true))
      ),
      commandes: this.commandeService.getAllCommandes().pipe(
        catchError(err => this.handleLoadError(err, 'commandes', () => this.errorCommandes = true))
      ),
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ clients, fournisseurs, articles, stocks, commandes }) => {
        // Stats
        this.stats = {
          totalClients: clients ? clients.length : 0,
          totalFournisseurs: fournisseurs ? fournisseurs.length : 0,
          totalArticles: articles ? articles.length : 0,
          totalCommandes: commandes ? commandes.length : 0,
        };
        this.statsLoaded = true;

        // Recent clients (last 5, sorted by id desc)
        if (clients) {
          this.recentClients = [...clients].sort((a, b) => (b.id ?? 0) - (a.id ?? 0)).slice(0, 5);
        }
        this.loadingClients = false;

        // Recent fournisseurs (last 5, sorted by id desc)
        if (fournisseurs) {
          this.recentFournisseurs = [...fournisseurs].sort((a, b) => (b.id ?? 0) - (a.id ?? 0)).slice(0, 5);
        }
        this.loadingFournisseurs = false;

        // Recent articles with stock status (from Stock entries, last 5 by id desc)
        if (stocks) {
          const typedStocks = stocks as Stock[];
          const qtys = typedStocks.map(s => s.quantiteDisponible ?? 0);
          const maxQty = qtys.length > 0 ? Math.max(...qtys, 1) : 1;
          this.recentArticles = [...typedStocks]
            .sort((a, b) => (b.id ?? 0) - (a.id ?? 0))
            .slice(0, 5)
            .map(s => {
              const qty = s.quantiteDisponible ?? 0;
              const percent = Math.round((qty / maxQty) * 100);
              let stockStatus: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
              if (qty === 0) stockStatus = 'OUT_OF_STOCK';
              else if (qty < this.LOW_STOCK_THRESHOLD) stockStatus = 'LOW_STOCK';
              else stockStatus = 'IN_STOCK';
              const name = s.articleAchat?.nom
                || (s.articleAchatId ? `Article #${s.articleAchatId}` : 'Article inconnu');
              return { id: s.id ?? 0, name, quantity: qty, stockStatus, stockPercent: percent };
            });
        }
        this.loadingArticles = false;

        // Recent commandes (last 5, sorted by id desc)
        if (commandes) {
          this.recentCommandes = [...commandes].sort((a, b) => (b.id ?? 0) - (a.id ?? 0)).slice(0, 5);
        }
        this.loadingCommandes = false;
      },
      error: (err: any) => {
        // Unexpected outer error (e.g. network-level failure not caught by individual catchErrors)
        console.error('Erreur chargement dashboard:', err);
        this.resetStates();
      },
    });
  }

  private handleLoadError(err: any, resource: string, setError: () => void) {
    console.error(`Error loading ${resource}:`, err);
    setError();
    // Return null; the caller checks for null before processing the result.
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return of(null as any);
  }

  private resetStates(): void {
    this.statsLoaded = true;
    this.loadingClients = false;
    this.loadingFournisseurs = false;
    this.loadingArticles = false;
    this.loadingCommandes = false;
    this.errorClients = true;
    this.errorFournisseurs = true;
    this.errorArticles = true;
    this.errorCommandes = true;
  }

  retryLoad(): void {
    this.loadingClients = true;
    this.loadingFournisseurs = true;
    this.loadingArticles = true;
    this.loadingCommandes = true;
    this.errorClients = false;
    this.errorFournisseurs = false;
    this.errorArticles = false;
    this.errorCommandes = false;
    this.statsLoaded = false;
    this.loadAllData();
  }

  getCommandeStatusClass(statut: string | undefined): string {
    switch ((statut || '').toUpperCase()) {
      case 'LIVRE':
      case 'VALIDEE':
      case 'DONE': return 'badge-done';
      case 'EN_ATTENTE':
      case 'PENDING': return 'badge-pending';
      case 'ANNULEE':
      case 'CANCELLED': return 'badge-cancelled';
      default: return 'badge-pending';
    }
  }

  getCommandeStatusLabel(statut: string | undefined): string {
    switch ((statut || '').toUpperCase()) {
      case 'LIVRE': return 'Livré';
      case 'VALIDEE': return 'Validée';
      case 'DONE': return 'Terminé';
      case 'EN_ATTENTE': return 'En Attente';
      case 'PENDING': return 'En Attente';
      case 'ANNULEE':
      case 'CANCELLED': return 'Annulée';
      default: return statut || '—';
    }
  }

  getClientName(client: Client): string {
    return `${client.nom} ${client.prenom || ''}`.trim();
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }

  onStatCardClick(event: MouseEvent, path: string): void {
    const card = (event.currentTarget as HTMLElement);
    const ripple = document.createElement('span');
    const rect = card.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    ripple.className = 'ripple';
    ripple.style.width = `${size}px`;
    ripple.style.height = `${size}px`;
    ripple.style.left = `${event.clientX - rect.left - size / 2}px`;
    ripple.style.top = `${event.clientY - rect.top - size / 2}px`;
    card.appendChild(ripple);
    ripple.addEventListener('animationend', () => ripple.remove());
    setTimeout(() => this.router.navigate([path]), 120);
  }
}