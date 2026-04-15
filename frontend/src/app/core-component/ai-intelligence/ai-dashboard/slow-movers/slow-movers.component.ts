import { Component, Input } from '@angular/core';
import { ProductsDetails } from 'src/app/core/service/ai-intelligence/ai-intelligence.service';

@Component({
  selector: 'app-slow-movers',
  templateUrl: './slow-movers.component.html',
  styleUrl: './slow-movers.component.scss',
})
export class SlowMoversComponent {
  @Input() productsDetails: ProductsDetails | null = null;

  get slowMovers() {
    return this.productsDetails?.slowMovers ?? [];
  }

  formatCurrency(value: number): string {
    if (!value) return '0';
    if (value >= 1_000_000) return (value / 1_000_000).toFixed(1) + 'M';
    if (value >= 1_000) return (value / 1_000).toFixed(1) + 'k';
    return value.toFixed(2);
  }

  getRiskClass(qty: number): string {
    if (qty === 0) return 'risk-dead';
    if (qty < 5) return 'risk-high';
    return 'risk-medium';
  }

  getRiskLabel(qty: number): string {
    if (qty === 0) return 'Mort';
    if (qty < 5) return 'Critique';
    return 'Faible';
  }
}
