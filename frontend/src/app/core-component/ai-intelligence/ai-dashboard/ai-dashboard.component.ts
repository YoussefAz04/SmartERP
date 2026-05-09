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
  WhatIfResult,
  WhatIfScenario,
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
  plotOptions: ApexPlotOptions | any;
  stroke: ApexStroke | any;
  fill: ApexFill | any;
  tooltip: any;
  states: any;
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

  // What-if scenario state
  public showWhatIf = false;
  public whatIfLoading = false;
  public whatIfResult: WhatIfResult | null = null;
  public whatIfError = '';
  public whatIfScenarios: WhatIfScenario[] = [
    { name: 'Promotion commerciale', deltas: { promo: true } },
    { name: 'Ramadan', deltas: { ramadan: true } },
    { name: 'Après Ramadan', deltas: { ramadan: false } },
    { name: 'Jours ouvrés réduits (-3j)', deltas: { workingDays: -3 } },
    { name: 'Budget marketing +30%', deltas: { marketingSpend: 1 } },
  ];

  public salesChartOptions: Partial<ChartOptions> = {};
  public forecastChartOptions: Partial<ChartOptions> = {};
  public productChartOptions: Partial<DonutChartOptions> = {};

  /** Shared chart font stack — keeps charts visually consistent. */
  private readonly chartFontFamily = 'Inter, Poppins, "Segoe UI", sans-serif';

  /** Compact axis label formatter (e.g. 1.2M, 850k, 42). */
  private formatAxisValue(v: number): string {
    if (v >= 1_000_000) return (v / 1_000_000).toFixed(1) + 'M';
    if (v >= 1000) return (v / 1000).toFixed(0) + 'k';
    return String(v);
  }

  /** Localised TND tooltip formatter for chart series values. */
  private formatTooltipValue(v: number | null | undefined): string {
    if (v == null) return '—';
    return v.toLocaleString('fr-FR') + ' TND';
  }

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
        { name: 'Ventes Réelles', type: 'column', data: values },
        { name: 'Tendance', type: 'area', data: trend },
      ],
      chart: {
        height: 360,
        type: 'line',
        fontFamily: this.chartFontFamily,
        toolbar: { show: false },
        zoom: { enabled: false },
        selection: { enabled: false },
        background: 'transparent',
        animations: { enabled: true, speed: 800, animateGradually: { enabled: true, delay: 100 } },
        dropShadow: {
          enabled: true,
          enabledOnSeries: [0],
          top: 8,
          left: 0,
          blur: 10,
          color: '#2563EB',
          opacity: 0.25,
        },
      },
      colors: ['#2563EB', '#10B981'],
      // Disable ApexCharts' active-state darken filter so clicking a column
      // doesn't wipe the gradient-filled mixed (column + area) series.
      states: {
        hover: { filter: { type: 'lighten', value: 0.04 } },
        active: { allowMultipleDataPointsSelection: false, filter: { type: 'none' } },
      },
      plotOptions: {
        bar: {
          borderRadius: 10,
          borderRadiusApplication: 'end',
          columnWidth: '40%',
        },
      },
      dataLabels: { enabled: false },
      stroke: {
        width: [0, 3],
        curve: 'smooth',
        lineCap: 'round',
      },
      xaxis: {
        categories: months,
        axisBorder: { show: false },
        axisTicks: { show: false },
        labels: {
          style: { fontSize: '12px', fontWeight: 600, colors: '#64748b', fontFamily: this.chartFontFamily },
        },
        tooltip: { enabled: false },
        crosshairs: { show: false },
      },
      yaxis: {
        labels: {
          style: { fontSize: '11px', colors: '#94a3b8', fontFamily: this.chartFontFamily },
          formatter: (v: number) => this.formatAxisValue(v),
        },
      },
      legend: {
        position: 'top',
        horizontalAlign: 'right',
        fontSize: '12px',
        fontWeight: 600,
        labels: { colors: '#475569' },
        markers: { width: 10, height: 10, strokeWidth: 0, offsetX: -4 },
        itemMargin: { horizontal: 12, vertical: 0 },
      },
      fill: {
        type: ['gradient', 'gradient'],
        gradient: {
          type: 'vertical',
          shadeIntensity: 0.35,
          opacityFrom: [1, 0.45],
          opacityTo: [0.8, 0],
          stops: [0, 100],
        },
      },
      // ApexCharts options not declared in our partial type — accepted at runtime
      grid: {
        borderColor: '#e2e8f0',
        strokeDashArray: 5,
        padding: { top: 4, right: 16, bottom: 0, left: 16 },
        xaxis: { lines: { show: false } },
        yaxis: { lines: { show: true } },
      },
      markers: {
        size: [0, 4],
        strokeWidth: 2,
        strokeColors: '#ffffff',
        hover: { size: 7 },
      },
      tooltip: {
        theme: 'light',
        shared: true,
        intersect: false,
        style: { fontSize: '13px', fontFamily: this.chartFontFamily },
        y: { formatter: (v: number) => this.formatTooltipValue(v) },
      },
    } as Partial<ChartOptions>;
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
        { name: 'Réel', type: 'line', data: actual },
        { name: 'Prévision', type: 'line', data: fore },
        { name: 'Borne Haute', type: 'area', data: upper },
        { name: 'Borne Basse', type: 'area', data: lower },
      ],
      chart: {
        height: 360,
        type: 'line',
        fontFamily: this.chartFontFamily,
        toolbar: { show: false },
        background: 'transparent',
        animations: { enabled: true, speed: 800 },
        dropShadow: {
          enabled: true,
          enabledOnSeries: [0, 1],
          top: 5,
          left: 0,
          blur: 8,
          opacity: 0.18,
        },
      },
      colors: ['#F59E0B', '#9333EA', '#EA580C', '#6B7280'],
      dataLabels: { enabled: false },
      stroke: {
        width: [4, 3, 1, 1],
        curve: 'smooth',
        dashArray: [0, 8, 0, 0],
        lineCap: 'round',
      },
      xaxis: {
        categories: periods,
        axisBorder: { show: false },
        axisTicks: { show: false },
        labels: {
          style: { fontSize: '12px', fontWeight: 600, colors: '#64748b', fontFamily: this.chartFontFamily },
        },
      },
      yaxis: {
        labels: {
          style: { fontSize: '11px', colors: '#94a3b8', fontFamily: this.chartFontFamily },
          formatter: (v: number) => this.formatAxisValue(v),
        },
      },
      legend: {
        position: 'top',
        horizontalAlign: 'right',
        fontSize: '12px',
        fontWeight: 600,
        labels: { colors: '#475569' },
        markers: { width: 10, height: 10, strokeWidth: 0, offsetX: -4 },
        itemMargin: { horizontal: 12, vertical: 0 },
      },
      fill: {
        type: ['solid', 'solid', 'gradient', 'gradient'],
        opacity: [1, 1, 0.25, 0.25],
        gradient: {
          shade: 'light',
          type: 'vertical',
          opacityFrom: 0.32,
          opacityTo: 0,
          stops: [0, 100],
        },
      },
      grid: {
        borderColor: '#e2e8f0',
        strokeDashArray: 5,
        padding: { top: 4, right: 16, bottom: 0, left: 16 },
        xaxis: { lines: { show: false } },
        yaxis: { lines: { show: true } },
      },
      markers: {
        size: [5, 4, 0, 0],
        strokeWidth: 2,
        strokeColors: '#ffffff',
        hover: { size: 8 },
      },
      tooltip: {
        theme: 'light',
        shared: true,
        intersect: false,
        style: { fontSize: '13px', fontFamily: this.chartFontFamily },
        y: { formatter: (v: number) => this.formatTooltipValue(v) },
      },
    } as Partial<ChartOptions>;
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

    const totalSeries = seriesValues.reduce((acc, v) => acc + v, 0);
    const isCurrency = hasRevenue;
    const formatCenter = (v: number) =>
      isCurrency ? this.formatCurrency(v) + ' TND' : v.toLocaleString('fr-FR');

    this.productChartOptions = {
      series: seriesValues,
      chart: {
        type: 'donut',
        height: 360,
        fontFamily: this.chartFontFamily,
        background: 'transparent',
        animations: { enabled: true, speed: 800 },
        dropShadow: {
          enabled: true,
          top: 4,
          left: 0,
          blur: 12,
          opacity: 0.18,
        },
      },
      labels: top5.map((p) => p.productName),
      colors: ['#E11D48', '#0D9488', '#FBBF24', '#C026D3', '#65A30D'],
      legend: {
        position: 'bottom',
        fontSize: '12px',
        fontWeight: 600,
        labels: { colors: '#475569' },
        markers: { width: 10, height: 10, strokeWidth: 0, offsetX: -4 },
        itemMargin: { horizontal: 10, vertical: 6 },
      },
      dataLabels: {
        enabled: true,
        style: {
          fontSize: '11px',
          fontWeight: 700,
          fontFamily: this.chartFontFamily,
        },
        dropShadow: { enabled: true, top: 1, left: 0, blur: 2, opacity: 0.35 },
        formatter: (val: number) => `${Number(val).toFixed(1)}%`,
      },
      // Extra options accepted at runtime by ApexCharts even if not in our partial type
      stroke: { width: 3, colors: ['#ffffff'] },
      fill: {
        type: 'gradient',
        gradient: {
          shade: 'light',
          type: 'diagonal2',
          shadeIntensity: 0.35,
          gradientToColors: ['#FB7185', '#2DD4BF', '#FDE68A', '#E879F9', '#BEF264'],
          inverseColors: false,
          opacityFrom: 1,
          opacityTo: 1,
          stops: [0, 100],
        },
      },
      plotOptions: {
        pie: {
          startAngle: -90,
          endAngle: 270,
          expandOnClick: true,
          donut: {
            size: '72%',
            background: 'transparent',
            labels: {
              show: true,
              name: {
                show: true,
                fontSize: '12px',
                fontWeight: 700,
                color: '#94a3b8',
                offsetY: -10,
              },
              value: {
                show: true,
                fontSize: '22px',
                fontWeight: 800,
                color: '#0f172a',
                fontFamily: this.chartFontFamily,
                offsetY: 6,
                formatter: (val: string) => {
                  const num = parseFloat(val);
                  return formatCenter(isNaN(num) ? 0 : num);
                },
              },
              total: {
                show: true,
                showAlways: false,
                label: 'Total',
                fontSize: '12px',
                fontWeight: 700,
                color: '#94a3b8',
                formatter: () => formatCenter(totalSeries),
              },
            },
          },
        },
      },
      tooltip: {
        theme: 'light',
        style: { fontSize: '13px', fontFamily: this.chartFontFamily },
        y: {
          formatter: (v: number) => formatCenter(v),
        },
      },
      states: {
        hover: { filter: { type: 'lighten', value: 0.05 } },
        active: { filter: { type: 'darken', value: 0.1 } },
      },
    } as Partial<DonutChartOptions>;
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

  /**
   * Convert raw recommendation type codes (e.g. "ML_RISQUE_STOCK",
   * "STOCK_CRITIQUE", "FOURNISSEUR_PERFORMANT") into clean, human-readable
   * labels for display in the recommendation card footer.
   */
  formatRecType(type: string | null | undefined): string {
    if (!type) { return ''; }
    return type
      .replace(/[_-]+/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (c) => c.toUpperCase());
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

  toggleWhatIf(): void {
    this.showWhatIf = !this.showWhatIf;
    if (this.showWhatIf && !this.whatIfResult) {
      this.runWhatIf();
    }
  }

  runWhatIf(): void {
    this.whatIfLoading = true;
    this.whatIfError = '';
    this.whatIfResult = null;
    this.aiService.whatIfForecast(this.whatIfScenarios).subscribe({
      next: (result) => {
        this.whatIfResult = result;
        this.whatIfLoading = false;
      },
      error: (err) => {
        console.error('What-if forecast error:', err);
        this.whatIfError = 'Erreur lors du calcul des scénarios.';
        this.whatIfLoading = false;
      },
    });
  }

  navigateFromRecommendation(rec: Recommendation): void {
    switch (rec.type) {
      case 'ML_RISQUE_STOCK':
        this.router.navigate([routes.stockList]);
        break;
      case 'STOCK_CRITIQUE':
      case 'VALEUR_STOCK':
        this.router.navigate([routes.stockList]);
        break;
      case 'STOCK_FAIBLE':
        this.router.navigate([routes.stockAdjust]);
        break;
      case 'VENTES_FAIBLES':
      case 'TENDANCE_CROISSANCE':
      case 'PANIER_MOYEN_BAISSE':
      case 'PANIER_MOYEN_HAUSSE':
      case 'TAUX_ANNULATION':
        this.router.navigate([routes.commandeVenteList]);
        break;
      case 'PRODUIT_STAR':
      case 'OPPORTUNITE':
      case 'PRODUITS_LENTS':
      case 'CATEGORIE_DOMINANTE':
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
      case 'DIVERSIFICATION_FOURNISSEUR':
      case 'FOURNISSEUR_PERFORMANT':
      case 'CASH_FLOW':
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
