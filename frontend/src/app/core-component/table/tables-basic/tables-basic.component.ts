import { Component } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-tables-basic',
  templateUrl: './tables-basic.component.html',
  styleUrls: ['./tables-basic.component.scss'],
})
export class TablesBasicComponent {
  public routes = routes;
}
