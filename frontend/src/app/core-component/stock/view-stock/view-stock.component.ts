import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StockService } from '../../../core/service/stock/stock.service';
import { StockMouvementService } from '../../../core/service/stock-mouvement/stock-mouvement.service';
import { Stock } from '../../../model/stock.model';
import { StockMouvement } from '../../../model/stock-mouvement.model';

@Component({
  selector: 'app-view-stock',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './view-stock.component.html',
  styleUrl: './view-stock.component.scss'
})
export class ViewStockComponent implements OnInit {
  stock: Stock | null = null;
  movements: StockMouvement[] = [];
  filteredMovements: StockMouvement[] = [];
  isLoading = false;
  isLoadingMovements = false;

  activeTab: 'overview' | 'movements' = 'overview';
  filterType = '';

  pageSize = 10;
  currentPage = 1;

  get paginatedMovements(): StockMouvement[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredMovements.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredMovements.length / this.pageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private stockService: StockService,
    private mouvementService: StockMouvementService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    const tab = this.route.snapshot.queryParamMap.get('tab');
    if (tab === 'movements') {
      this.activeTab = 'movements';
    }
    if (id) {
      this.loadStock(+id);
      this.loadMovements(+id);
    }
  }

  loadStock(id: number): void {
    this.isLoading = true;
    this.stockService.getStockById(id).subscribe({
      next: (data) => {
        this.stock = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Stock loading error:', err);
        this.isLoading = false;
      }
    });
  }

  loadMovements(stockId: number): void {
    this.isLoadingMovements = true;
    this.mouvementService.getMovementsByStock(stockId).subscribe({
      next: (data) => {
        this.movements = data.sort((a, b) =>
          new Date(b.dateMouvement || 0).getTime() - new Date(a.dateMouvement || 0).getTime()
        );
        this.applyFilters();
        this.isLoadingMovements = false;
      },
      error: (err) => {
        console.error('Erreur chargement mouvements:', err);
        this.isLoadingMovements = false;
      }
    });
  }

  applyFilters(): void {
    let result = [...this.movements];
    if (this.filterType) {
      result = result.filter(m => m.typeMouvement === this.filterType);
    }
    this.filteredMovements = result;
    this.currentPage = 1;
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

  getStockStatus(): string {
    if (!this.stock) return '';
    const qty = this.stock.quantiteDisponible || 0;
    if (qty === 0) return 'Out of stock de stock';
    if (qty < 5) return 'Low stock';
    return 'OK';
  }

  getStatusClass(): string {
    if (!this.stock) return '';
    const qty = this.stock.quantiteDisponible || 0;
    if (qty === 0) return 'badge bg-danger';
    if (qty < 5) return 'badge bg-warning text-dark';
    return 'badge bg-success';
  }

  exportCSV(): void {
    const headers = ['Date', 'Type', 'Quantity', 'Reference', 'Motif', 'User'];
    const rows = this.filteredMovements.map(m => [
      m.dateMouvement ? new Date(m.dateMouvement).toLocaleString('fr-FR') : '',
      m.typeMouvement,
      m.quantite,
      m.referenceCommande || '',
      m.motif || '',
      m.utilisateur?.username || m.utilisateur?.email || ''
    ]);
    const csvContent = [headers, ...rows].map(r => r.join(';')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `mouvements_stock_${this.stock?.id}.csv`;
    link.click();
  }

  goBack(): void {
    this.router.navigate(['/stock/list']);
  }
}
