import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { StockService } from '../../../core/service/stock/stock.service';
import { StockMouvementService } from '../../../core/service/stock-mouvement/stock-mouvement.service';
import { Stock } from '../../../model/stock.model';
import { StockMouvement } from '../../../model/stock-mouvement.model';

@Component({
  selector: 'app-list-stock-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './list-stock-dashboard.component.html',
  styleUrl: './list-stock-dashboard.component.scss'
})
export class ListStockDashboardComponent implements OnInit {
  stocks: Stock[] = [];
  movements: StockMouvement[] = [];
  isLoading = false;

  totalItems = 0;
  totalAvailable = 0;
  lowStockCount = 0;
  lowStockItems: Stock[] = [];

  constructor(
    private stockService: StockService,
    private stockMouvementService: StockMouvementService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.stockService.getAllStocks().subscribe({
      next: (data) => {
        this.stocks = data;
        this.calculateMetrics();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement stocks:', err);
        this.isLoading = false;
      }
    });

    this.stockMouvementService.getAllMovements().subscribe({
      next: (data) => {
        this.movements = data;
      },
      error: (err) => {
        console.error('Erreur chargement mouvements:', err);
      }
    });
  }

  calculateMetrics(): void {
    this.totalItems = this.stocks.length;
    this.totalAvailable = this.stocks.reduce((sum, s) => sum + (s.quantiteDisponible || 0), 0);
    this.lowStockItems = this.stocks.filter(s => (s.quantiteDisponible || 0) < 5);
    this.lowStockCount = this.lowStockItems.length;
  }

  getStockStatus(stock: Stock): string {
    const qty = stock.quantiteDisponible || 0;
    if (qty === 0) return 'OUT_OF_STOCK';
    if (qty < 5) return 'LOW_STOCK';
    return 'OK';
  }

  getStatusBadgeClass(stock: Stock): string {
    const status = this.getStockStatus(stock);
    if (status === 'OUT_OF_STOCK') return 'badge bg-danger';
    if (status === 'LOW_STOCK') return 'badge bg-warning text-dark';
    return 'badge bg-success';
  }

  getStatusLabel(stock: Stock): string {
    const status = this.getStockStatus(stock);
    if (status === 'OUT_OF_STOCK') return 'Rupture';
    if (status === 'LOW_STOCK') return 'Stock bas';
    return 'OK';
  }

  getMovementBadgeClass(type: string): string {
    switch (type) {
      case 'ENTREE': return 'badge bg-success';
      case 'SORTIE': return 'badge bg-danger';
      case 'AJUSTEMENT': return 'badge bg-warning text-dark';
      case 'RESERVATION': return 'badge bg-primary';
      default: return 'badge bg-secondary';
    }
  }

  get recentMovements(): StockMouvement[] {
    return this.movements.slice(0, 5);
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }
}
