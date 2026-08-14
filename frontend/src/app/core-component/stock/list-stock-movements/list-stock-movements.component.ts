import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { StockMouvementService } from '../../../core/service/stock-mouvement/stock-mouvement.service';
import { StockMouvement } from '../../../model/stock-mouvement.model';

@Component({
  selector: 'app-list-stock-movements',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './list-stock-movements.component.html',
  styleUrl: './list-stock-movements.component.scss'
})
export class ListStockMovementsComponent implements OnInit {
  movements: StockMouvement[] = [];
  filteredMovements: StockMouvement[] = [];
  isLoading = false;

  filterType = '';
  filterDateFrom = '';
  filterDateTo = '';
  searchTerm = '';

  pageSize = 25;
  currentPage = 1;

  get paginatedMovements(): StockMouvement[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredMovements.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredMovements.length / this.pageSize);
  }

  get pages(): number[] {
    if (this.totalPages <= 7) {
      return Array.from({ length: this.totalPages }, (_, i) => i + 1);
    }
    const around = [
      this.currentPage - 1, this.currentPage, this.currentPage + 1
    ].filter(p => p >= 1 && p <= this.totalPages);
    return [...new Set([1, ...around, this.totalPages])].sort((a, b) => a - b);
  }

  get totalEntree(): number {
    return this.filteredMovements.filter(m => m.typeMouvement === 'ENTREE').reduce((sum, m) => sum + m.quantite, 0);
  }

  get totalSortie(): number {
    return this.filteredMovements.filter(m => m.typeMouvement === 'SORTIE').reduce((sum, m) => sum + m.quantite, 0);
  }

  get totalAjustement(): number {
    return this.filteredMovements.filter(m => m.typeMouvement === 'AJUSTEMENT').length;
  }

  Math = Math;

  constructor(private mouvementService: StockMouvementService) {}

  ngOnInit(): void {
    this.loadMovements();
  }

  loadMovements(): void {
    this.isLoading = true;
    this.mouvementService.getAllMovements().subscribe({
      next: (data) => {
        this.movements = data.sort((a, b) =>
          new Date(b.dateMouvement || 0).getTime() - new Date(a.dateMouvement || 0).getTime()
        );
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement mouvements:', err);
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    let result = [...this.movements];

    if (this.filterType) {
      result = result.filter(m => m.typeMouvement === this.filterType);
    }

    if (this.filterDateFrom) {
      const from = new Date(this.filterDateFrom);
      result = result.filter(m => m.dateMouvement && new Date(m.dateMouvement) >= from);
    }

    if (this.filterDateTo) {
      const to = new Date(this.filterDateTo);
      to.setHours(23, 59, 59);
      result = result.filter(m => m.dateMouvement && new Date(m.dateMouvement) <= to);
    }

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(m =>
        (m.stock?.articleAchat?.nom || '').toLowerCase().includes(term) ||
        (m.referenceCommande || '').toLowerCase().includes(term) ||
        (m.motif || '').toLowerCase().includes(term) ||
        (m.utilisateur?.username || '').toLowerCase().includes(term)
      );
    }

    this.filteredMovements = result;
    this.currentPage = 1;
  }

  clearFilters(): void {
    this.filterType = '';
    this.filterDateFrom = '';
    this.filterDateTo = '';
    this.searchTerm = '';
    this.applyFilters();
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

  exportCSV(): void {
    const headers = ['Date', 'Article', 'Type', 'Quantity', 'Reference', 'Motif', 'User', 'Localisation'];
    const rows = this.filteredMovements.map(m => [
      m.dateMouvement ? new Date(m.dateMouvement).toLocaleString('fr-FR') : '',
      m.stock?.articleAchat?.nom || `Stock #${m.stock?.id}`,
      m.typeMouvement,
      m.quantite,
      m.referenceCommande || '',
      m.motif || '',
      m.utilisateur?.username || m.utilisateur?.email || '',
      m.stock?.localisation || ''
    ]);
    const csvContent = [headers, ...rows].map(r => r.join(';')).join('\n');
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `mouvements_stock_${new Date().toISOString().substring(0, 10)}.csv`;
    link.click();
  }
}
