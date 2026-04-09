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
  @ViewChild('chart') chart!: ChartComponent;
  public chartOptions: Partial<ChartOptions>;

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
          name: 'Sales',
          data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        },
        {
          name: 'Purchase',
          data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        },
      ],
      colors: ['#28C76F', '#EA5455'],
      chart: {
        type: 'bar',
        height: 320,
        stacked: true,

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
          borderRadiusWhenStacked: 'all',
          columnWidth: '20%',
        },
      },
      dataLabels: {
        enabled: false,
      },
      yaxis: {
        min: -200,
        max: 300,
        tickAmount: 5,
      },
      xaxis: {
        categories: [
          'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
          'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
        ],
      },
      legend: { show: false },
      fill: {
        opacity: 1,
      },
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
  }

  loadDashboardStats(): void {
    this.statsLoading = true;
    this.statsError = false;
    this.dashboardApiService.getDashboardStats().subscribe({
      next: (data: DashboardStatsDto) => {
        this.stats = data;
        this.statsLoading = false;
        this.updateChart();
      },
      error: (err: any) => {
        console.error('Error loading dashboard stats:', err);
        this.statsError = true;
        this.statsLoading = false;
      }
    });
  }

  updateChart(): void {
    if (!this.stats) return;
    const salesData = (this.stats.monthlySalesChart || []).map(m => m.amount || 0);
    const purchaseData = (this.stats.monthlyPurchasesChart || []).map(m => -(m.amount || 0));
    const maxVal = Math.max(...salesData, 1);
    const minVal = Math.min(...purchaseData, -1);

    this.chartOptions = {
      ...this.chartOptions,
      series: [
        { name: 'Sales', data: salesData },
        { name: 'Purchase', data: purchaseData },
      ],
      yaxis: {
        min: Math.floor(minVal * 1.2),
        max: Math.ceil(maxVal * 1.2),
        tickAmount: 5,
      },
    };
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

