import {Component, OnInit, ViewChild} from '@angular/core';
import {BaseChartDirective, NgChartsModule} from "ng2-charts";
import {ChartConfiguration, ChartData} from "chart.js";
import {ProjetService} from "../../../core/service/projet/projet.service";
import {StatutProjet} from "../../../model/projet.model";
import {forkJoin} from "rxjs";
import {StatutTache} from "../../../model/tache.model";
import {TacheService} from "../../../core/service/taches/tache.service";

@Component({
  selector: 'app-pie-chart-tache',
  standalone: true,
  imports: [
    NgChartsModule
  ],
  templateUrl: './pie-chart-tache.component.html',
  styleUrl: './pie-chart-tache.component.scss'
})
export class PieChartTacheComponent implements OnInit{
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  public pieChartLabels: string[] = ['A_FAIRE', 'EN_COURS', 'TERMINEE', 'BLOQUEE'];

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

  constructor(private tacheService: TacheService) {
  }

  ngOnInit(): void {
    this.loadData();
  }

  // Solution 1: Utiliser forkJoin pour charger toutes les données en même temps
  private loadData(): void {
    const statuts = [
      StatutTache.EN_COURS,
      StatutTache.A_FAIRE,
      StatutTache.TERMINEE,
      StatutTache.BLOQUEE,
    ];

    const requests = statuts.map(statut =>
      this.tacheService.getNombreTacheByStatus(statut)
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
