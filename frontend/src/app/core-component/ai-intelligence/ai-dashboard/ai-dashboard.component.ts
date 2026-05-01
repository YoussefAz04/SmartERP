/* eslint-disable @typescript-eslint/no-explicit-any */
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexFill,
  ApexLegend,
  ApexNonAxisChartSeries,
  ApexPlotOptions,
  ApexStroke,
  ApexXAxis,
  ApexYAxis,
} from 'ng-apexcharts';
import {
  AiDashboard,
  AiIntelligenceService,
  Recommendation,
} from 'src/app/core/service/ai-intelligence/ai-intelligence.service';
import { routes } from 'src/app/core/helpers/routes';
import { KpiModalType } from './kpi-detail-modal/kpi-detail-modal.component';

export type ChartOptions = {
  series: ApexAxisChartSeries | any;
  chart: ApexChart | any;
  colors: any;
  dataLabels: ApexDataLabels | any;
  plotOptions: ApexPlotOptions | any;
  yaxis: ApexYAxis | any;
  xaxis: ApexXAxis | any;
  legend: ApexLegend | any;
  fill: ApexFill | any;
  stroke: ApexStroke | any;
};

export type DonutChartOptions = {
  series: ApexNonAxisChartSeries | any;
  chart: ApexChart | any;
  labels: any;
  colors: any;
  legend: ApexLegend | any;
  dataLabels: ApexDataLabels | any;
};

@Component({
  selector: 'app-ai-dashboard',
  templateUrl: './ai-dashboard.component.html',
  styleUrl: './ai-dashboard.component.scss',
})
export class AiDashboardComponent implements OnInit {
  public dashboard: AiDashboard | null = null;
  public loading = true;
  public error = false;
  public isRefreshing = false;
  public today = new Date();

  public activeModal: KpiModalType = null;

  public salesChartOptions: Partial<ChartOptions> = {};
  public forecastChartOptions: Partial<ChartOptions> = {};
  public productChartOptions: Partial<DonutChartOptions> = {};

  constructor(private aiService: AiIntelligenceService, private router: Router) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = false;
    this.aiService.getAiDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
        this.isRefreshing = false;
        this.buildCharts();
      },
      error: (err) => {
        console.error('Erreur chargement IA:', err);
        this.error = true;
        this.loading = false;
        this.isRefreshing = false;
      },
    });
  }

  refresh(): void {
    this.isRefreshing = true;
    this.loadDashboard();
  }

  buildCharts(): void {
    if (!this.dashboard) return;
    this.buildSalesChart();
    this.buildForecastChart();
    this.buildProductChart();
  }

  buildSalesChart(): void {
    const sales = this.dashboard?.salesAnalysis;
    if (!sales) return;
    const months = sales.monthlySales.map((m) => m.month);
    const values = sales.monthlySales.map((m) => Math.round(m.amount));
    const trend = sales.trendLine.map((v) => Math.round(v));

    this.salesChartOptions = {
      series: [
        { name: 'Ventes Réelles', type: 'bar', data: values },
        { name: 'Tendance', type: 'line', data: trend },
      ],
      chart: { height: 320, type: 'line' },
      colors: ['#4361EE', '#F72585'],
      plotOptions: { bar: { borderRadius: 4, columnWidth: '55%' } },
      dataLabels: { enabled: false },
      stroke: { width: [0, 3], curve: 'smooth' },
      xaxis: { categories: months },
      yaxis: {
        labels: { formatter: (v: number) => (v >= 1000 ? (v / 1000).toFixed(0) + 'k' : String(v)) },
      },
      legend: { position: 'top' },
      fill: { opacity: [0.85, 1] },
    };
  }

  buildForecastChart(): void {
    const forecast = this.dashboard?.forecast;
    if (!forecast?.forecastPoints?.length) return;
    const periods = forecast.forecastPoints.map((p) => p.period);
    const actual = forecast.forecastPoints.map((p) => (p.actual != null ? Math.round(p.actual) : null));
    const fore = forecast.forecastPoints.map((p) => Math.round(p.forecast || 0));
    const upper = forecast.forecastPoints.map((p) => Math.round(p.upperBound || 0));
    const lower = forecast.forecastPoints.map((p) => Math.round(p.lowerBound || 0));

    this.forecastChartOptions = {
      series: [
        { name: 'Réel', data: actual },
        { name: 'Prévision', data: fore },
        { name: 'Borne Haute', data: upper },
        { name: 'Borne Basse', data: lower },
      ],
      chart: { height: 320, type: 'line' },
      colors: ['#4361EE', '#F72585', '#06d6a0', '#ef476f'],
      dataLabels: { enabled: false },
      stroke: { width: [3, 3, 1, 1], curve: 'smooth', dashArray: [0, 5, 4, 4] },
      xaxis: { categories: periods },
      yaxis: {
        labels: { formatter: (v: number) => (v >= 1000 ? (v / 1000).toFixed(0) + 'k' : String(v)) },
      },
      legend: { position: 'top' },
      fill: { opacity: 1 },
    };
  }

  buildProductChart(): void {
    const products = this.dashboard?.productIntelligence?.topProducts;
    if (!products?.length) return;
    const top5 = products.slice(0, 5);

    // Prefer revenue; fall back to quantitySold (e.g. stock qty) when all
    // revenues are 0 — this happens for enterprises that have catalogue items
    // but no recorded sales yet. If both are zero, leave series empty so the
    // "Aucune donnée produit" placeholder is displayed instead of a blank chart.
    const revenues = top5.map((p) => Math.round(p.revenue));
    const hasRevenue = revenues.some((v) => v > 0);
    let seriesValues: number[];
    if (hasRevenue) {
      seriesValues = revenues;
    } else {
      const quantities = top5.map((p) => Math.round(p.quantitySold ?? 0));
      if (!quantities.some((v) => v > 0)) return;
      seriesValues = quantities;
    }

    this.productChartOptions = {
      series: seriesValues,
      chart: { type: 'donut', height: 300 },
      labels: top5.map((p) => p.productName),
      colors: ['#4361EE', '#F72585', '#06d6a0', '#ef476f', '#ffd166'],
      legend: { position: 'bottom', fontSize: '12px' },
      dataLabels: { enabled: true },
    };
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'CRITIQUE': return 'badge-critique';
      case 'HAUTE': return 'badge-haute';
      case 'MOYENNE': return 'badge-moyenne';
      case 'BASSE': return 'badge-basse';
      default: return 'badge-info';
    }
  }

  getPriorityLabel(priority: string): string {
    switch (priority) {
      case 'CRITIQUE': return 'Critique';
      case 'HAUTE': return 'Haute';
      case 'MOYENNE': return 'Moyenne';
      case 'BASSE': return 'Basse';
      default: return 'Info';
    }
  }

  getRecommendationsByPriority(priority: string): Recommendation[] {
    return (this.dashboard?.recommendations || []).filter((r) => r.priority === priority);
  }

  get criticalRecs(): Recommendation[] { return this.getRecommendationsByPriority('CRITIQUE'); }
  get highRecs(): Recommendation[] { return this.getRecommendationsByPriority('HAUTE'); }
  get mediumRecs(): Recommendation[] { return this.getRecommendationsByPriority('MOYENNE'); }
  get infoRecs(): Recommendation[] {
    return (this.dashboard?.recommendations || []).filter(r => ['BASSE', 'INFO'].includes(r.priority));
  }

  get growthArrow(): string {
    const g = this.dashboard?.salesAnalysis?.growthRate ?? 0;
    return g >= 0 ? '▲' : '▼';
  }

  get growthClass(): string {
    const g = this.dashboard?.salesAnalysis?.growthRate ?? 0;
    return g >= 0 ? 'text-success' : 'text-danger';
  }

  get confidenceLevelLabel(): string {
    const score = this.dashboard?.forecast?.confidenceScore ?? 0;
    if (score >= 80) return 'Très élevée';
    if (score >= 65) return 'Élevée';
    if (score >= 45) return 'Moyenne';
    return 'À renforcer';
  }

  formatCurrency(value: number): string {
    if (!value) return '0';
    const abs = Math.abs(value);
    const sign = value < 0 ? '-' : '';
    if (abs >= 1_000_000) return sign + (abs / 1_000_000).toFixed(1) + 'M';
    if (abs >= 1_000) return sign + (abs / 1_000).toFixed(1) + 'k';
    return value.toFixed(2);
  }

  openModal(type: KpiModalType): void {
    this.activeModal = type;
  }

  closeModal(): void {
    this.activeModal = null;
  }

  navigateFromRecommendation(rec: Recommendation): void {
    switch (rec.type) {
      case 'ML_RISQUE_STOCK':
        this.router.navigate([routes.stockList]);
        break;
      case 'STOCK_CRITIQUE':
        this.router.navigate([routes.stockList]);
        break;
      case 'STOCK_FAIBLE':
        this.router.navigate([routes.stockAdjust]);
        break;
      case 'VENTES_FAIBLES':
        this.router.navigate([routes.commandeVenteList]);
        break;
      case 'PRODUIT_STAR':
      case 'OPPORTUNITE':
      case 'PRODUITS_LENTS':
        this.router.navigate([routes.articleVenteList]);
        break;
      case 'MARGE_FAIBLE':
      case 'MARGE_CORRECTE':
      case 'BONNE_PERFORMANCE':
      case 'RATIO_ACHATS':
        this.router.navigate([routes.articleAchatList]);
        break;
      case 'LIVRAISON_RETARD':
      case 'LIVRAISON_OK':
        this.router.navigate([routes.commandeAchatList]);
        break;
      case 'CONCENTRATION_CLIENT':
        this.router.navigate([routes.commandeVenteList]);
        break;
      case 'DONNEES_INSUFFISANTES':
        this.router.navigate([routes.addCommandeAchat]);
        break;
      default:
        break;
    }
  }
}
