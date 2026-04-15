import { Component, Input } from '@angular/core';
import { SupplierPerformance } from 'src/app/core/service/ai-intelligence/ai-intelligence.service';

@Component({
  selector: 'app-supplier-performance',
  templateUrl: './supplier-performance.component.html',
  styleUrl: './supplier-performance.component.scss',
})
export class SupplierPerformanceComponent {
  @Input() suppliers: SupplierPerformance[] = [];

  getStars(rating: number): number[] {
    const full = Math.round(rating || 0);
    return Array.from({ length: 5 }, (_, i) => i < full ? 1 : 0);
  }

  getDeliveryRateClass(rate: number): string {
    if (rate >= 85) return 'rate-good';
    if (rate >= 60) return 'rate-medium';
    return 'rate-poor';
  }

  getStatusCount(supplier: SupplierPerformance, status: string): number {
    const found = supplier.statusBreakdown?.find(s => s.status === status);
    return found ? found.count : 0;
  }

  formatCurrency(value: number): string {
    if (!value) return '0';
    if (value >= 1_000_000) return (value / 1_000_000).toFixed(1) + 'M';
    if (value >= 1_000) return (value / 1_000).toFixed(1) + 'k';
    return value.toFixed(2);
  }
}
