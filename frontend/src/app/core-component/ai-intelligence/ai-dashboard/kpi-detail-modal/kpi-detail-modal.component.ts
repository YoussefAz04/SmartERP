/* eslint-disable @typescript-eslint/no-explicit-any */
import { Component, EventEmitter, HostListener, Input, OnChanges, Output } from '@angular/core';
import { KpiDetails } from 'src/app/core/service/ai-intelligence/ai-intelligence.service';

export type KpiModalType = 'revenue' | 'orders' | 'products' | 'forecast' | null;

@Component({
  selector: 'app-kpi-detail-modal',
  templateUrl: './kpi-detail-modal.component.html',
  styleUrl: './kpi-detail-modal.component.scss',
})
export class KpiDetailModalComponent implements OnChanges {
  @Input() modalType: KpiModalType = null;
  @Input() kpiDetails!: KpiDetails;
  @Output() closed = new EventEmitter<void>();

  activeTab = 0;

  ngOnChanges(): void {
    this.activeTab = 0;
  }

  close(): void {
    this.closed.emit();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.modalType) this.close();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.close();
    }
  }

  setTab(index: number): void {
    this.activeTab = index;
  }

  formatCurrency(value: number): string {
    if (!value) return '0';
    if (value >= 1_000_000) return (value / 1_000_000).toFixed(1) + 'M';
    if (value >= 1_000) return (value / 1_000).toFixed(1) + 'k';
    return value.toFixed(2);
  }

  get modalTitle(): string {
    switch (this.modalType) {
      case 'revenue': return 'Revenue Details';
      case 'orders': return 'Order Details';
      case 'products': return 'Product Details';
      case 'forecast': return 'Détails des Forecasts';
      default: return '';
    }
  }

  get modalIcon(): string {
    switch (this.modalType) {
      case 'revenue': return 'fas fa-coins';
      case 'orders': return 'fas fa-shopping-cart';
      case 'products': return 'fas fa-boxes';
      case 'forecast': return 'fas fa-chart-line';
      default: return 'fas fa-chart-bar';
    }
  }

  get tabs(): string[] {
    switch (this.modalType) {
      case 'revenue': return ['Top Produits', 'Top Clients'];
      case 'orders': return ['Statut', 'Vue d\'ensemble'];
      case 'products': return ['Stock Levels', 'Movements'];
      case 'forecast': return ['Forecasts', 'Accuracy'];
      default: return [];
    }
  }

  exportCsv(): void {
    let rows: string[] = [];
    let filename = 'export.csv';

    if (this.modalType === 'revenue' && this.kpiDetails?.revenue) {
      if (this.activeTab === 0) {
        filename = 'top_produits.csv';
        rows = ['Product,Revenue (TND)'];
        (this.kpiDetails.revenue.topProducts || []).forEach(p =>
          rows.push(`"${p.name}",${p.value}`)
        );
      } else {
        filename = 'top_clients.csv';
        rows = ['Customer,Revenue (TND)'];
        (this.kpiDetails.revenue.topCustomers || []).forEach(c =>
          rows.push(`"${c.name}",${c.value}`)
        );
      }
    } else if (this.modalType === 'orders' && this.kpiDetails?.orders) {
      filename = 'statut_commandes.csv';
      rows = ['Statut,Nombre'];
      (this.kpiDetails.orders.byStatus || []).forEach(s =>
        rows.push(`"${s.status}",${s.count}`)
      );
    } else if (this.modalType === 'products' && this.kpiDetails?.products) {
      filename = 'produits.csv';
      rows = ['Produit,Quantity Sold'];
      (this.kpiDetails.products.topProducts || []).forEach(p =>
        rows.push(`"${p.name}",${p.value}`)
      );
    } else if (this.modalType === 'forecast' && this.kpiDetails?.forecast) {
      filename = 'previsions.csv';
      rows = ['Period,Value (TND)'];
      rows.push(`"Mois prochain",${this.kpiDetails.forecast.predictedNextMonth}`);
      rows.push(`"3 prochains mois",${this.kpiDetails.forecast.predictedNext3Months}`);
      rows.push(`"6 prochains mois",${this.kpiDetails.forecast.predictedNext6Months}`);
    }

    if (!rows.length) return;
    const blob = new Blob([rows.join('\n')], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  }
}
