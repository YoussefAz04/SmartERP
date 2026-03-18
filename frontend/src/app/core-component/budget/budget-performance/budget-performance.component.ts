import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {BudgetService} from "../../../core/service/Budget/budget.service";
import {DepenseService} from "../../../core/service/depense/depense.service";
import {BudgetPerformanceDto} from "../../../model/budgetPerformanceDto.model";
import {DecimalPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import { Chart } from 'chart.js';

@Component({
  selector: 'app-budget-performance',
  standalone: true,
  imports: [
    NgIf,
    NgClass,
    DecimalPipe,
    NgForOf,

  ],
  templateUrl: './budget-performance.component.html',
  styleUrl: './budget-performance.component.scss'
})
export class BudgetPerformanceComponent implements OnInit, AfterViewInit {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  performances: BudgetPerformanceDto[] = [];
  loading = true;
  chart: Chart | null = null;

  constructor(
    private budgetService: BudgetService,
    private depenseService: DepenseService
  ) {}

  ngOnInit(): void {
    this.loadPerformances();
  }

  ngAfterViewInit(): void {
    if (!this.loading && this.performances.length > 0) {
      this.createChart();
    }
  }

  private loadPerformances(): void {
    this.depenseService.getPerformanceBudgets().subscribe({
      next: (data: BudgetPerformanceDto[]) => {
        this.performances = data;
        this.loading = false;
        // Create chart after data is loaded
        setTimeout(() => this.createChart(), 100);
      },
      error: (err) => {
        console.error("Erreur lors du chargement des performances", err);
        this.loading = false;
      }
    });
  }

  private createChart(): void {
    if (!this.chartCanvas || this.performances.length === 0) return;

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    // Destroy existing chart if it exists
    if (this.chart) {
      this.chart.destroy();
    }

    const labels = this.performances.map(p => p.nom);
    const data = this.performances.map(p => p.tauxConsommation);
    const colors = this.performances.map(p =>
      p.depassement ? '#dc3545' : p.tauxConsommation > 80 ? '#ffc107' : '#28a745'
    );

    this.chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: 'Taux de consommation (%)',
          data: data,
          backgroundColor: colors,
          borderColor: colors,
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            max: 120,
            ticks: {
              callback: function(value) {
                return value + '%';
              }
            }
          }
        },
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                return `${context.parsed.y.toFixed(2)}%`;
              }
            }
          }
        }
      }
    });
  }

  getStatusBadgeClass(depassement: boolean): string {
    return depassement ? 'badge-danger' : 'badge-success';
  }

  getStatusText(depassement: boolean): string {
    return depassement ? 'Oui' : 'Non';
  }


}
