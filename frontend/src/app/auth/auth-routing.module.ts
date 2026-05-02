import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthComponent } from './auth.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { SigninComponent } from './signin/signin/signin.component';
import { RegisterComponent } from './register/register/register.component';
import { RegisterEntrepriseStepperComponent } from './register-entreprise-stepper/register-entreprise-stepper.component';

const routes: Routes = [
  { path: '', redirectTo: 'signin', pathMatch: 'full' },

  {
    path: '',
    component: AuthComponent,
    children: [
      {
        path: 'forgot-password',
        component: ForgotPasswordComponent,
      },
      {
        path: 'reset-password',
        component: ResetPasswordComponent,
      },
      {
        path: 'register',
        component: RegisterComponent,
      },
      {
        path: 'signin',
        component: SigninComponent,
      },
      {
        path: 'stepperCompany',
        component: RegisterEntrepriseStepperComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuthRoutingModule {}
