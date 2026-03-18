import { Component } from '@angular/core';
import {PieChartProjetComponent} from "../pie-chart-projet/pie-chart-projet.component";
import {BarresVerticalesProjetComponent} from "../barres-verticales-projet/barres-verticales-projet.component";
import {PieChartTacheComponent} from "../pie-chart-tache/pie-chart-tache.component";

@Component({
  selector: 'app-statistique-projet',
  standalone: true,
  imports: [
    PieChartProjetComponent,
    BarresVerticalesProjetComponent,
    PieChartTacheComponent
  ],
  templateUrl: './statistique-projet.component.html',
  styleUrl: './statistique-projet.component.scss'
})
export class StatistiqueProjetComponent {

}
