import { Component, Input } from '@angular/core';
import { ComparisonMetrics } from 'src/app/core/service/ai-intelligence/ai-intelligence.service';

@Component({
  selector: 'app-comparison-metrics',
  templateUrl: './comparison-metrics.component.html',
  styleUrl: './comparison-metrics.component.scss',
})
export class ComparisonMetricsComponent {
  @Input() comparison!: ComparisonMetrics;

  formatCurrency(value: number): string {
    if (!value) return '0';
    if (value >= 1_000_000) return (value / 1_000_000).toFixed(1) + 'M';
    if (value >= 1_000) return (value / 1_000).toFixed(1) + 'k';
    return value.toFixed(2);
  }

  getProgressWidth(current: number, previous: number): number {
    const max = Math.max(current, previous, 1);
    return Math.min((current / max) * 100, 100);
  }

  getPreviousProgressWidth(current: number, previous: number): number {
    const max = Math.max(current, previous, 1);
    return Math.min((previous / max) * 100, 100);
  }
}
