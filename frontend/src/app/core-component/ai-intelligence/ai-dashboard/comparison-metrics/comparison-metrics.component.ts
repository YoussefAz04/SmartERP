import { Component, Input } from '@angular/core';
import { ComparisonMetrics, PeriodComparison } from 'src/app/core/service/ai-intelligence/ai-intelligence.service';

@Component({
  selector: 'app-comparison-metrics',
  templateUrl: './comparison-metrics.component.html',
  styleUrl: './comparison-metrics.component.scss',
})
export class ComparisonMetricsComponent {
  @Input() comparison!: ComparisonMetrics;

  /** Format a currency value, keeping the sign for negatives and the 2-decimal precision for small ones. */
  formatCurrency(value: number | null | undefined): string {
    if (value === null || value === undefined || Number.isNaN(value)) return '0';
    const abs = Math.abs(value);
    const sign = value < 0 ? '-' : '';
    if (abs >= 1_000_000) return sign + (abs / 1_000_000).toFixed(1) + 'M';
    if (abs >= 1_000) return sign + (abs / 1_000).toFixed(1) + 'k';
    return value.toFixed(2);
  }

  /** Width of the "current" bar (0‒100 %) – uses absolute values so negative margins still render a bar. */
  getProgressWidth(current: number, previous: number): number {
    const max = Math.max(Math.abs(current), Math.abs(previous), 1);
    return Math.min((Math.abs(current) / max) * 100, 100);
  }

  getPreviousProgressWidth(current: number, previous: number): number {
    const max = Math.max(Math.abs(current), Math.abs(previous), 1);
    return Math.min((Math.abs(previous) / max) * 100, 100);
  }

  /** Gross margin percentage = margin / sales. */
  getMarginPercent(): number {
    const salesYtd = this.comparison?.ytdVsLastYear?.current ?? 0;
    if (!salesYtd) return 0;
    const margin = this.comparison.grossMarginYtd ?? 0;
    return Math.round((margin / salesYtd) * 1000) / 10;
  }

  getMarginClass(): string {
    const pct = this.getMarginPercent();
    if (pct >= 30) return 'text-success';
    if (pct >= 15) return 'text-warning';
    if (pct >= 0) return 'text-muted';
    return 'text-danger';
  }

  getMtdMarginClass(): string {
    const current = this.comparison?.marginMtdVsLtd?.current ?? 0;
    if (current > 0) return 'text-success';
    if (current === 0) return 'text-muted';
    return 'text-danger';
  }

  /** True when the previous period had no activity – we display a "NOUV." badge instead of 0 %. */
  isNew(p: PeriodComparison | null | undefined): boolean {
    return !!p && (p.changePercent === null || p.changePercent === undefined) && p.current !== 0;
  }

  /** Build a human-readable change label, e.g. "+12.4 %" or "NOUV." when there is no previous. */
  changeLabel(p: PeriodComparison | null | undefined): string {
    if (!p) return '—';
    if (this.isNew(p)) return 'NOUV.';
    const v = p.changePercent ?? 0;
    const sign = v >= 0 ? '+' : '';
    // When the value is at the cap boundary, signal that it is truncated.
    if (v >= 9999) return '>9999%';
    if (v <= -9999) return '<-9999%';
    return `${sign}${v.toFixed(1)}%`;
  }

  /** CSS class for a trend badge – "new" gets the positive colour but a distinct label. */
  badgeClass(p: PeriodComparison | null | undefined, invert = false): string {
    if (!p) return 'badge-stable';
    if (this.isNew(p)) {
      const newTrend = p.current >= 0 ? 'up' : 'down';
      return this.trendToBadgeClass(newTrend, invert);
    }
    return this.trendToBadgeClass(p.trend, invert);
  }

  private trendToBadgeClass(trend: 'up' | 'down' | 'stable', invert: boolean): string {
    if (trend === 'up') return invert ? 'badge-down' : 'badge-up';
    if (trend === 'down') return invert ? 'badge-up' : 'badge-down';
    return 'badge-stable';
  }

  /** Icon class for a "sales/revenue" category label – green up when growing, red down when shrinking. */
  salesIconClass(p: PeriodComparison | null | undefined): string {
    if (!p || p.trend === 'stable') return 'fas fa-minus text-muted';
    return p.trend === 'up' ? 'fas fa-arrow-up text-success' : 'fas fa-arrow-down text-danger';
  }

  /** Icon class for a "purchases/cost" category label – inverted: up purchases = red, down = green. */
  purchaseIconClass(p: PeriodComparison | null | undefined): string {
    if (!p || p.trend === 'stable') return 'fas fa-minus text-muted';
    return p.trend === 'up' ? 'fas fa-arrow-up text-danger' : 'fas fa-arrow-down text-success';
  }

  /** Icon class for a "margin" category label – same direction as sales icon. */
  marginIconClass(p: PeriodComparison | null | undefined): string {
    return this.salesIconClass(p);
  }
}
