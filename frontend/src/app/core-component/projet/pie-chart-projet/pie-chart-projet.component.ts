import { Component, OnInit, ViewChild } from '@angular/core';
import { ChartConfiguration, ChartData } from 'chart.js';
import { NgChartsModule, BaseChartDirective } from 'ng2-charts';
import { ProjetService } from '../../../core/service/projet/projet.service';
import { StatutProjet } from '../../../model/projet.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-pie-chart-projet',
  standalone: true,
  imports: [NgChartsModule],
  templateUrl: './pie-chart-projet.component.html',
  styleUrl: './pie-chart-projet.component.scss',
})
export class PieChartProjetComponent implements OnInit {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  public pieChartLabels: string[] = ['EN_COURS', 'TERMINE', 'ANNULE', 'EN_ATTENTE'];

  public pieChartOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
      },
    },
  };

  public pieChartType: 'pie' = 'pie';

  public pieChartData: ChartData<'pie', number[], string> = {
    labels: this.pieChartLabels,
    datasets: [{
      data: [0, 0, 0, 0],
      backgroundColor: ['#36A2EB', '#4BC0C0', '#FF6384', '#FFCE56'],
    }],
  };

  constructor(private projetService: ProjetService) {
  }

  ngOnInit(): void {
    this.loadData();
  }

  // Solution 1: Utiliser forkJoin pour charger toutes les données en même temps
  private loadData(): void {
    const statuts = [
      StatutProjet.EN_COURS,
      StatutProjet.TERMINE,
      StatutProjet.ANNULE,
      StatutProjet.EN_ATTENTE,
    ];

    const requests = statuts.map(statut =>
      this.projetService.getNombreProjetByStatut(statut)
    );

    forkJoin(requests).subscribe({
      next: (counts) => {
        this.pieChartData.datasets[0].data = counts;
        console.log('Données chargées:', counts);

        // Forcer la mise à jour du graphique
        this.chart?.update();
      },
      error: (error) => {
        console.error('Erreur lors du chargement des données:', error);
        this.pieChartData.datasets[0].data = [0, 0, 0, 0];
        this.chart?.update();
      }
    });
  }
}
