import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth.component';
import { SharedModule } from '../shared/shared.module';
import { RegisterComponent } from './register/register/register.component';
import { SigninComponent } from './signin/signin/signin.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password/forgot-password.component';



@NgModule({
  declarations: [
    AuthComponent,
    RegisterComponent,
    SigninComponent,
    ForgotPasswordComponent
  ],
  imports: [
    CommonModule,
    AuthRoutingModule,
    SharedModule
  ]
})
export class AuthModule { }
