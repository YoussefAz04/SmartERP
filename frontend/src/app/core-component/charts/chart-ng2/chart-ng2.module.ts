import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ChartNg2RoutingModule } from './chart-ng2-routing.module';
import { ChartNg2Component } from './chart-ng2.component';
import { sharedModule } from 'src/app/shared/shared.module';


@NgModule({
  declarations: [
    ChartNg2Component
  ],
  imports: [
    CommonModule,
    ChartNg2RoutingModule,
    sharedModule
  ]
})
export class ChartNg2Module { }
