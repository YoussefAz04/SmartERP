import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-two-step-verification',
  templateUrl: './two-step-verification.component.html',
  styleUrl: './two-step-verification.component.scss'
})
export class TwoStepVerificationComponent {
  public routes = routes;
  constructor(private router: Router) {}

  navigation() {
    this.router.navigate([routes.adminDashboard])
  }
}
