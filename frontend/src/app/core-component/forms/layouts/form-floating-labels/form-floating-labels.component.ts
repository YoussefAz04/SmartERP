import { Component } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-form-floating-labels',
  templateUrl: './form-floating-labels.component.html',
  styleUrl: './form-floating-labels.component.scss'
})
export class FormFloatingLabelsComponent {
  public routes = routes;
}
