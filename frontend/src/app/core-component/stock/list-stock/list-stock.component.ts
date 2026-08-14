import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { StockService } from '../../../core/service/stock/stock.service';
import { Stock } from '../../../model/stock.model';
import { ResponseDto } from '../../../model/reponseDto.model';
import Swal from 'sweetalert2';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-list-stock',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './list-stock.component.html',
  styleUrl: './list-stock.component.scss'
})
export class ListStockComponent implements OnInit {
  Math = Math;
  stocks: Stock[] = [];
  filteredStocks: Stock[] = [];
  isLoading = false;

  canAdjustStock = false;
  canDeleteStock = false;

  searchTerm = '';
  filterLocation = '';
  filterStatus = '';

  pageSize = 10;
  currentPage = 1;

  get locations(): string[] {
    return [...new Set(this.stocks.map(s => s.localisation || '').filter(l => l))];
  }

  get totalPages(): number {
    return Math.ceil(this.filteredStocks.length / this.pageSize);
  }

  get paginatedStocks(): Stock[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredStocks.slice(start, start + this.pageSize);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  constructor(
    private stockService: StockService,
    private router: Router,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadStocks();
    this.loadRolePermissions();
  }

  loadRolePermissions(): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));
        const roles: string[] = Array.isArray(currentUser?.roles) ? currentUser.roles : [];
        const isSuperAdmin = roles.includes('SUPER_ADMIN');
        const isAdmin = roles.includes('ADMIN');
        const isEmployee = roles.includes('EMPLOYEE');
        this.canAdjustStock = isSuperAdmin || isAdmin || isEmployee;
        this.canDeleteStock = isSuperAdmin || isAdmin || isEmployee;
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
  }

  loadStocks(): void {
    this.isLoading = true;
    this.stockService.getAllStocks().subscribe({
      next: (data) => {
        this.stocks = data;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Stock loading error:', err);
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    let result = [...this.stocks];

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(s =>
        (s.articleAchat?.nom || '').toLowerCase().includes(term) ||
        (s.articleAchat?.sku || '').toLowerCase().includes(term) ||
        (s.localisation || '').toLowerCase().includes(term)
      );
    }

    if (this.filterLocation) {
      result = result.filter(s => s.localisation === this.filterLocation);
    }

    if (this.filterStatus) {
      result = result.filter(s => this.getStockStatus(s) === this.filterStatus);
    }

    this.filteredStocks = result;
    this.currentPage = 1;
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
    if (status === 'OUT_OF_STOCK') return 'Out of stock';
    if (status === 'LOW_STOCK') return 'Low stock';
    return 'OK';
  }

  viewStock(id: number | undefined): void {
    if (id) this.router.navigate(['/stock/view', id]);
  }

  adjustStock(id: number | undefined): void {
    if (id) this.router.navigate(['/stock/adjust', id]);
  }

  viewMovements(id: number | undefined): void {
    if (id) this.router.navigate(['/stock/view', id], { queryParams: { tab: 'movements' } });
  }

  deleteStock(id: number | undefined): void {
    if (!id) return;
    Swal.fire({
      title: 'Are you sure?',
      text: 'This action cannot be undone!',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Oui, supprimer',
      cancelButtonText: 'Cancel'
    }).then((result) => {
      if (result.isConfirmed) {
        this.stockService.deleteStock(id).subscribe({
          next: () => {
            this.stocks = this.stocks.filter(s => s.id !== id);
            this.applyFilters();
            this.notify.success('The stock has been deleted.');
          },
          error: () => this.notify.error('Impossible de supprimer le stock.')
        });
      }
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.filterLocation = '';
    this.filterStatus = '';
    this.applyFilters();
  }
}
