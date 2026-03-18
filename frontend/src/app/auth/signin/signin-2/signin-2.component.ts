import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-signin-2',
  templateUrl: './signin-2.component.html',
  styleUrl: './signin-2.component.scss'
})
export class Signin2Component {
  public routes = routes;
  constructor(private router: Router) {}

  navigation() {
    this.router.navigate([routes.adminDashboard])
  }
  public password : boolean[] = [false];

  public togglePassword(index: number){
    this.password[index] = !this.password[index]
  }
}
