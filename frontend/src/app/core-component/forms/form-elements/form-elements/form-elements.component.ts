import { Component } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-form-elements',
  templateUrl: './form-elements.component.html',
  styleUrl: './form-elements.component.scss'
})
export class FormElementsComponent {
  public routes = routes;
}
