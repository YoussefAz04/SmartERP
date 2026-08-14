/* eslint-disable @typescript-eslint/no-explicit-any */
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
  DashboardService,
  RecentClientSummary,
  RecentCommandeSummary,
  RecentFournisseurSummary,
} from 'src/app/core/service/dashboard/dashboard.service';
import {
  AppLanguage,
  LanguageService,
} from 'src/app/core/service/language/language.service';

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
  public currentLanguage: AppLanguage = 'en';

  // Stats for count-up cards
  public stats: DashboardStats = { totalClients: 0, totalFournisseurs: 0, totalArticles: 0, totalCommandes: 0 };
  public statsLoaded = false;

  // Recent data — uses slim DTO shapes returned by the dashboard summary endpoint
  public recentClients: RecentClientSummary[] = [];
  public recentFournisseurs: RecentFournisseurSummary[] = [];
  public recentArticles: RecentArticleView[] = [];
  public recentCommandes: RecentCommandeSummary[] = [];

  // Loading / error state — single endpoint now drives all four sections
  public loadingClients = true;
  public loadingFournisseurs = true;
  public loadingArticles = true;
  public loadingCommandes = true;

  public errorClients = false;
  public errorFournisseurs = false;
  public errorArticles = false;
  public errorCommandes = false;

  // Count-up options
  public countUpOptions = { duration: 2, separator: ' ' };

  constructor(
    private router: Router,
    private dashboardService: DashboardService,
    private languageService: LanguageService,
  ) {}

  ngOnInit(): void {
    this.languageService.currentLanguage$
      .pipe(takeUntil(this.destroy$))
      .subscribe((language) => {
        this.currentLanguage = language;
      });
    this.loadEnterpriseContext();
    this.loadAllData();
  }

  get formattedToday(): string {
    return new Intl.DateTimeFormat(
      this.currentLanguage === 'fr' ? 'fr-FR' : 'en-US',
      {
        weekday: 'long',
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      }
    ).format(this.today);
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

  trackByCommandeId(_index: number, item: RecentCommandeSummary): number {
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
    // Single backend round-trip: counts + 5 most-recent rows of each section,
    // server-aggregated and cached. Replaces the old "fetch every list and
    // slice 5" pattern that was the root cause of the slow page load.
    this.dashboardService.getHomeSummary()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: summary => {
          this.stats = {
            totalClients: summary.totalClients ?? 0,
            totalFournisseurs: summary.totalFournisseurs ?? 0,
            totalArticles: summary.totalArticles ?? 0,
            totalCommandes: summary.totalCommandes ?? 0,
          };
          this.statsLoaded = true;

          this.recentClients = summary.recentClients ?? [];
          this.loadingClients = false;

          this.recentFournisseurs = summary.recentFournisseurs ?? [];
          this.loadingFournisseurs = false;

          const stocks = summary.recentStocks ?? [];
          const qtys = stocks.map(s => s.quantiteDisponible ?? 0);
          const maxQty = qtys.length > 0 ? Math.max(...qtys, 1) : 1;
          this.recentArticles = stocks.map(s => {
            const qty = s.quantiteDisponible ?? 0;
            const percent = Math.round((qty / maxQty) * 100);
            let stockStatus: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
            if (qty === 0) stockStatus = 'OUT_OF_STOCK';
            else if (qty < this.LOW_STOCK_THRESHOLD) stockStatus = 'LOW_STOCK';
            else stockStatus = 'IN_STOCK';
            const name = s.articleNom
              || (s.articleAchatId ? `Article #${s.articleAchatId}` : 'Article inconnu');
            return { id: s.id ?? 0, name, quantity: qty, stockStatus, stockPercent: percent };
          });
          this.loadingArticles = false;

          this.recentCommandes = summary.recentCommandes ?? [];
          this.loadingCommandes = false;
        },
        error: (err: any) => {
          console.error('Erreur chargement dashboard:', err);
          this.resetStates();
        },
      });
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

  getCommandeStatusLabelKey(statut: string | undefined): string {
    switch ((statut || '').toUpperCase()) {
      case 'LIVRE': return 'dashboard.orderStatus.delivered';
      case 'VALIDEE': return 'dashboard.orderStatus.validated';
      case 'DONE': return 'dashboard.orderStatus.completed';
      case 'EN_ATTENTE':
      case 'PENDING': return 'dashboard.orderStatus.pending';
      case 'ANNULEE':
      case 'CANCELLED': return 'dashboard.orderStatus.cancelled';
      default: return statut || 'common.notAvailable';
    }
  }

  getClientName(client: RecentClientSummary): string {
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
