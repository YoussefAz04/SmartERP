import { Component } from '@angular/core';
import {PieChartTacheComponent} from "../pie-chart-tache/pie-chart-tache.component";

@Component({
  selector: 'app-statistique-tache',
  standalone: true,
  imports: [
    PieChartTacheComponent
  ],
  templateUrl: './statistique-tache.component.html',
  styleUrl: './statistique-tache.component.scss'
})
export class StatistiqueTacheComponent {

}
