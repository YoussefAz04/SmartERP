/* eslint-disable @typescript-eslint/no-explicit-any */
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ClientService } from 'src/app/core/service/client/client.service';
import { FournisseurService } from 'src/app/core/service/fournisseur/fournisseur.service';
import { ArticleAchatService } from 'src/app/core/service/article-achat/article-achat.service';
import { CommandeVenteService } from 'src/app/core/service/commande-vente/commande-vente.service';
import { Client } from 'src/app/model/client.model';
import { Fournisseur } from 'src/app/model/fournisseur.model';
import { ArticleAchat } from 'src/app/model/article-achat.model';
import { CommandeVente } from 'src/app/model/commande-vente.model';

export interface DashboardStats {
  totalClients: number;
  totalFournisseurs: number;
  totalArticles: number;
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
export class AdminDashboardComponent implements OnInit {
  private readonly LOW_STOCK_THRESHOLD = 10;

  public userName = 'Administrateur';
  public today: Date = new Date();

  // Stats for count-up cards
  public stats: DashboardStats = { totalClients: 0, totalFournisseurs: 0, totalArticles: 0 };
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

  // Count-up options
  public countUpOptions = { duration: 2, separator: ' ' };

  constructor(
    private router: Router,
    private clientService: ClientService,
    private fournisseurService: FournisseurService,
    private articleService: ArticleAchatService,
    private commandeService: CommandeVenteService,
  ) {}

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadAllData();
  }

  loadUserInfo(): void {
    try {
      const item = localStorage.getItem('myLSkey');
      if (item) {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        const token = user.token;
        if (token && token.split('.').length === 3) {
          const payload = JSON.parse(atob(token.split('.')[1]));
          this.userName = payload.firstName || payload.username || 'Administrateur';
        }
      }
    } catch {
      // keep defaults
    }
  }

  loadAllData(): void {
    forkJoin({
      clients: this.clientService.getAllClients(),
      fournisseurs: this.fournisseurService.getAllFournisseurs(),
      articles: this.articleService.getAllArticlesAchat(),
      commandes: this.commandeService.getAllCommandes(),
    }).subscribe({
      next: ({ clients, fournisseurs, articles, commandes }) => {
        // Stats
        this.stats = {
          totalClients: clients.length,
          totalFournisseurs: fournisseurs.length,
          totalArticles: articles.length,
        };
        this.statsLoaded = true;

        // Recent clients (last 5, sorted by id desc)
        this.recentClients = [...clients].sort((a, b) => (b.id ?? 0) - (a.id ?? 0)).slice(0, 5);
        this.loadingClients = false;

        // Recent fournisseurs (last 5, sorted by id desc)
        this.recentFournisseurs = [...fournisseurs].sort((a, b) => (b.id ?? 0) - (a.id ?? 0)).slice(0, 5);
        this.loadingFournisseurs = false;

        // Recent articles with stock status (last 5, sorted by id desc)
        const qtys = articles.map(a => a.quantite || 0);
        const maxQty = qtys.length > 0 ? Math.max(...qtys, 1) : 1;
        this.recentArticles = [...articles]
          .sort((a, b) => (b.id ?? 0) - (a.id ?? 0))
          .slice(0, 5)
          .map(a => {
            const qty = a.quantite || 0;
            const percent = Math.round((qty / maxQty) * 100);
            let stockStatus: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
            if (qty === 0) stockStatus = 'OUT_OF_STOCK';
            else if (qty < this.LOW_STOCK_THRESHOLD) stockStatus = 'LOW_STOCK';
            else stockStatus = 'IN_STOCK';
            return { id: a.id ?? 0, name: a.nom, quantity: qty, stockStatus, stockPercent: percent };
          });
        this.loadingArticles = false;

        // Recent commandes (last 5, sorted by id desc)
        this.recentCommandes = [...commandes].sort((a, b) => (b.id ?? 0) - (a.id ?? 0)).slice(0, 5);
        this.loadingCommandes = false;
      },
      error: (err: any) => {
        console.error('Erreur chargement dashboard:', err);
        this.loadingClients = false;
        this.loadingFournisseurs = false;
        this.loadingArticles = false;
        this.loadingCommandes = false;
      },
    });
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
}

