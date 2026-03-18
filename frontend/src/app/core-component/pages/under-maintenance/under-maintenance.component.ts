import { Component } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-under-maintenance',
  templateUrl: './under-maintenance.component.html',
  styleUrl: './under-maintenance.component.scss'
})
export class UnderMaintenanceComponent {
  public routes = routes;
}
