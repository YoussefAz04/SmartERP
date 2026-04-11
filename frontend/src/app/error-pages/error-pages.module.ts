import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ErrorPagesRoutingModule } from './error-pages-routing.module';
import { ErrorPagesComponent } from './error-pages.component';
import { Error403Component } from './error-403/error-403.component';
import { Error404Component } from './error-404/error-404.component';
import { Error500Component } from './error-500/error-500.component';


@NgModule({
  declarations: [
    ErrorPagesComponent,
    Error403Component,
    Error404Component,
    Error500Component
  ],
  imports: [
    CommonModule,
    ErrorPagesRoutingModule
  ]
})
export class ErrorPagesModule { }
