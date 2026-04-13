/* eslint-disable @typescript-eslint/no-explicit-any */
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import {
  ApexAxisChartSeries,
  ApexChart,
  ChartComponent,
  ApexDataLabels,
  ApexPlotOptions,
  ApexYAxis,
  ApexXAxis,
  ApexResponsive,
  ApexLegend,
  ApexFill,
  ApexNonAxisChartSeries,
  ApexStroke,
} from 'ng-apexcharts';
import { apiResultFormat } from 'src/app/core/core.index';
import { routes } from 'src/app/core/helpers/routes';
import { CommonService } from 'src/app/core/service/common/common.service';
import { DataService } from 'src/app/core/service/data/data.service';
import { SettingsService } from 'src/app/core/service/settings/settings.service';
import { expiredproduct } from 'src/app/shared/model/page.model';
import {
  PaginationService,
  pageSelection,
  tablePageSize,
} from 'src/app/shared/shared.index';
import Swal from 'sweetalert2';
import { DashboardApiService, DashboardStatsDto } from 'src/app/core/service/dashboard/dashboard-api.service';
export type ChartOptions = {
  series: ApexAxisChartSeries | any;

  chart: ApexChart | any;

  responsive: ApexResponsive | any;

  colors: any;

  dataLabels: ApexDataLabels | any;

  plotOptions: ApexPlotOptions | any;

  yaxis: ApexYAxis | any;

  xaxis: ApexXAxis | any;

  legend: ApexLegend | any;

  fill: ApexFill | any;
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
};

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss',
})
export class AdminDashboardComponent implements OnInit {
  initChecked = false;
  public routes = routes;
  public currency!: string;
  public stats: DashboardStatsDto | null = null;
  public statsLoading = true;
  public statsError = false;
  public isRefreshing = false;
  public userName: string = 'Utilisateur';
  public enterpriseName: string = 'Entreprise';
  public today: Date = new Date();

  // French labels for ERP dashboard
  public labels = {
    tableauBord: 'Tableau de Bord',
    bienvenue: 'Bienvenue de retour',
    chargement: 'Chargement des données du tableau de bord...',
    erreurChargement: 'Impossible de charger les statistiques en temps réel. Affichage des données en cache si disponibles.',
    metriquesCles: 'Métriques Clés',
    ventesTotales: 'Ventes Totales',
    achatsTotaux: 'Achats Totaux',
    beneficePerte: 'Bénéfice/Perte',
    chiffreAffaires: "Chiffre d'Affaires",
    revenuVentes: 'Revenu Ventes',
    totalAchats: 'Total Achats',
    benefice: 'Bénéfice',
    perte: 'Perte',
    revenuNet: 'Revenu Net',
    apercuMetier: 'Aperçu Métier',
    totalClients: 'Total Clients',
    nouveau: 'Nouveau',
    articlesSkus: 'Articles (SKUs)',
    enStock: 'En Stock',
    commandesVente: 'Commandes de Vente',
    actif: 'Actif',
    commandesAchat: "Commandes d'Achat",
    enAttente: 'En Attente',
    etatStock: 'État du Stock',
    valeurStock: 'Valeur Stock',
    valeurTotalStock: 'Valeur totale du stock',
    stockFaible: 'Stock Faible',
    articlesAttention: 'Articles nécessitant attention',
    ruptureStock: 'Rupture Stock',
    articlesIndisponibles: 'Articles indisponibles',
    analytique: 'Analytique',
    achatsVentes: 'Achats & Ventes',
    ventes: 'Ventes',
    achats: 'Achats',
    meilleursArticles: 'Meilleurs Articles',
    numero: 'N°',
    article: 'Article',
    quantiteVendue: 'Quantité Vendue',
    revenu: 'Revenu',
    aucuneDonneeArticle: 'Aucun article disponible',
    meilleursClients: 'Meilleurs Clients',
    client: 'Client',
    commandes: 'Commandes',
    totalDepense: 'Total Dépensé',
    aucunDonneeClient: 'Aucun client disponible',
    etatCommandes: 'État des Commandes',
    commandesVenteTitre: 'Commandes de Vente',
    commandesAchatTitre: "Commandes d'Achat",
    aucuneDonnee: 'Aucune donnée',
    articlesExpires: 'Articles Expirés',
    sku: 'SKU',
    dateFabrication: 'Date de Fabrication',
    dateExpiration: "Date d'Expiration",
    action: 'Action',
  };
  @ViewChild('chart') chart!: ChartComponent;
  public chartOptions: Partial<ChartOptions>;
  public donutChartOptions: Partial<DonutChartOptions>;

  constructor(
    private common: CommonService,
    private setting: SettingsService,
    private data: DataService,
    private pagination: PaginationService,
    private router: Router,
    private dashboardApiService: DashboardApiService
  ) {
    this.chartOptions = {
      series: [
        {
          name: 'Ventes',
          data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        },
        {
          name: 'Achats',
          data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        },
      ],
      colors: ['#28C76F', '#0084D6'],
      chart: {
        type: 'bar',
        height: 300,
        stacked: false,

        zoom: {
          enabled: true,
        },
      },
      responsive: [
        {
          breakpoint: 280,
          options: {
            legend: {
              position: 'bottom',
              offsetY: 0,
            },
          },
        },
      ],
      plotOptions: {
        bar: {
          horizontal: false,
          borderRadius: 4,
          borderRadiusApplication: 'end',
          borderRadiusWhenStacked: 'last',
          columnWidth: '55%',
        },
      },
      dataLabels: {
        enabled: false,
      },
      yaxis: {
        min: 0,
        tickAmount: 5,
        labels: {
          formatter: (val: number) => val >= 1000 ? (val / 1000).toFixed(0) + 'k' : val.toString(),
        },
      },
      xaxis: {
        categories: [
          'Jan', 'Fév', 'Mars', 'Avr', 'Mai', 'Juin',
          'Juil', 'Août', 'Sept', 'Oct', 'Nov', 'Déc',
        ],
      },
      legend: {
        show: true,
        position: 'top',
        horizontalAlign: 'right',
      },
      fill: {
        opacity: 1,
      },
    };

    this.donutChartOptions = {
      series: [0, 0],
      chart: { type: 'donut', height: 260 },
      labels: ['Revenu encaissé', 'Dépenses réglées'],
      colors: ['#28C76F', '#EA5455'],
      legend: { position: 'bottom', fontSize: '13px' },
      dataLabels: { enabled: false },
      plotOptions: {
        pie: {
          donut: {
            size: '68%',
          },
        },
      },
      stroke: { width: 2, colors: ['#fff'] },
    };
    this.common.currency$.subscribe((res: string) => {
      this.currency = res;
    });
    this.data.getDataTable().subscribe((apiRes: apiResultFormat) => {
      this.totalData = apiRes.totalData;
      this.pagination.tablePageSize.subscribe((res: tablePageSize) => {
        if (this.router.url == this.routes.adminDashboard) {
          this.getTableData({ skip: res.skip, limit: this.totalData });
          this.pageSize = res.pageSize;
        }
      });
    });
  }

  ngOnInit(): void {
    this.loadDashboardStats();
    this.loadUserInfo();
  }

  loadUserInfo(): void {
    try {
      const item = localStorage.getItem('myLSkey');
      if (item) {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        const token = user.token;
        if (token && token.split('.').length === 3) {
          const payload = JSON.parse(atob(token.split('.')[1]));
          this.userName = payload.firstName || payload.username || 'Utilisateur';
          this.enterpriseName = payload.enterpriseName || 'Entreprise';
        }
      }
    } catch (e) {
      // keep defaults
    }
  }

  loadDashboardStats(): void {
    this.statsLoading = true;
    this.statsError = false;
    this.dashboardApiService.getDashboardStats().subscribe({
      next: (data: DashboardStatsDto) => {
        this.stats = data;
        this.statsLoading = false;
        this.isRefreshing = false;
        this.updateChart();
        this.updateDonutChart();
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des statistiques:', err);
        this.statsError = true;
        this.statsLoading = false;
        this.isRefreshing = false;
      }
    });
  }

  updateChart(): void {
    if (!this.stats) return;
    const salesData = (this.stats.monthlySalesChart || []).map(m => Math.round(m.amount || 0));
    const purchaseData = (this.stats.monthlyPurchasesChart || []).map(m => Math.round(m.amount || 0));
    const maxVal = Math.max(...salesData, ...purchaseData, 1);

    this.chartOptions = {
      ...this.chartOptions,
      series: [
        { name: 'Ventes', data: salesData },
        { name: 'Achats', data: purchaseData },
      ],
      chart: {
        ...this.chartOptions.chart,
        stacked: false,
      },
      yaxis: {
        min: 0,
        max: Math.ceil(maxVal * 1.2),
        tickAmount: 5,
        labels: {
          formatter: (val: number) => val >= 1000 ? (val / 1000).toFixed(0) + 'k' : val.toString(),
        },
      },
    };
  }

  updateDonutChart(): void {
    if (!this.stats) return;
    const revenue = Math.round(this.stats.totalRevenue || 0);
    const expenses = Math.round(this.stats.totalExpenses || 0);
    this.donutChartOptions = {
      ...this.donutChartOptions,
      series: [revenue, expenses],
    };
  }

  refreshDashboard(): void {
    this.isRefreshing = true;
    this.loadDashboardStats();
  }

  get conversionRate(): number {
    if (!this.stats) return 0;
    const purchases = this.stats.totalPurchases || 0;
    if (purchases === 0) return 0;
    return Math.round(((this.stats.totalSalesRevenue || 0) / purchases) * 100);
  }

  get avgSaleValue(): number {
    if (!this.stats) return 0;
    const orders = this.stats.totalOrders || 0;
    if (orders === 0) return 0;
    return Math.round((this.stats.totalSalesRevenue || 0) / orders);
  }

  get inventoryTurnover(): number {
    if (!this.stats) return 0;
    const stockValue = this.stats.totalStockValue || 0;
    if (stockValue === 0) return 0;
    return Math.round(((this.stats.totalSalesRevenue || 0) / stockValue) * 10) / 10;
  }

  getProductShare(revenue: number): number {
    if (!this.stats?.topProducts?.length) return 0;
    const total = this.stats.topProducts.reduce((sum, p) => sum + (p.revenue || 0), 0);
    if (total === 0) return 0;
    return Math.round((revenue / total) * 100);
  }

  getOrderStatusEntries(): { key: string, value: number }[] {
    if (!this.stats?.orderStatusBreakdown) return [];
    return Object.entries(this.stats.orderStatusBreakdown).map(([key, value]) => ({ key, value }));
  }

  getPurchaseOrderStatusEntries(): { key: string, value: number }[] {
    if (!this.stats?.purchaseOrderStatusBreakdown) return [];
    return Object.entries(this.stats.purchaseOrderStatusBreakdown).map(([key, value]) => ({ key, value }));
  }

  // pagination variables
  public tableData: Array<expiredproduct> = [];
  public pageSize = 10;
  public serialNumberArray: Array<number> = [];
  public totalData = 0;
  showFilter = false;
  dataSource!: MatTableDataSource<expiredproduct>;
  public searchDataValue = '';
  //** / pagination variables

  private getTableData(pageOption: pageSelection): void {
    this.data.getExpiredProduct().subscribe((apiRes: apiResultFormat) => {
      this.tableData = [];
      this.serialNumberArray = [];
      this.totalData = apiRes.totalData;
      apiRes.data.map((res: expiredproduct, index: number) => {
        const serialNumber = index + 1;
        if (index >= pageOption.skip && serialNumber <= pageOption.limit) {
          res.sNo = serialNumber;
          this.tableData.push(res);
          this.serialNumberArray.push(serialNumber);
        }
      });
      this.dataSource = new MatTableDataSource<expiredproduct>(this.tableData);
      this.pagination.calculatePageSize.next({
        totalData: this.totalData,
        pageSize: this.pageSize,
        tableData: this.tableData,
        serialNumberArray: this.serialNumberArray,
      });
    });
  }

  confirmColor() {
    const swalWithBootstrapButtons = Swal.mixin({
      customClass: {
        confirmButton: ' btn btn-success',
        cancelButton: 'me-2 btn btn-danger',
      },
      buttonsStyling: false,
    });

    swalWithBootstrapButtons
      .fire({
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        confirmButtonText: 'Yes, delete it!',
        showCancelButton: true,
        cancelButtonText: 'Cancel',
        reverseButtons: true,
      })
      .then((result) => {
        if (result.isConfirmed) {
          swalWithBootstrapButtons.fire(
            'Deleted!',
            'Your file has been deleted.',
            'success'
          );
        } else if (result.dismiss === Swal.DismissReason.cancel) {
          swalWithBootstrapButtons.fire(
            'Cancelled',
            'Your imaginary file is safe :)',
            'error'
          );
        }
      });
  }
  selectAll(initChecked: boolean) {
    if (!initChecked) {
      this.tableData.forEach((f) => {
        f.isSelected = true;
      });
    } else {
      this.tableData.forEach((f) => {
        f.isSelected = false;
      });
    }
  }
}

