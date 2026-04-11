import { Component } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-error-403',
  templateUrl: './error-403.component.html',
  styleUrl: './error-403.component.scss'
})
export class Error403Component {
  public routes = routes;
}
