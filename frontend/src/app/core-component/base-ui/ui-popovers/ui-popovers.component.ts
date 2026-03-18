import { Component } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-ui-popovers',
  templateUrl: './ui-popovers.component.html',
  styleUrl: './ui-popovers.component.scss'
})
export class UiPopoversComponent {
  public routes = routes;
}
