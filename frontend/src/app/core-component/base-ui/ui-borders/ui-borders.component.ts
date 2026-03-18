import { Component } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-ui-borders',
  templateUrl: './ui-borders.component.html',
  styleUrl: './ui-borders.component.scss'
})
export class UiBordersComponent {
  public routes = routes;
}
