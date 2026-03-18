import { Component } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-ui-dropdowns',
  templateUrl: './ui-dropdowns.component.html',
  styleUrl: './ui-dropdowns.component.scss'
})
export class UiDropdownsComponent {
  public routes = routes;
}
