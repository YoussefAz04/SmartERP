import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ChartNg2RoutingModule } from './chart-ng2-routing.module';
import { ChartNg2Component } from './chart-ng2.component';
import { SharedModule } from 'src/app/shared/shared.module';


@NgModule({
  declarations: [
    ChartNg2Component
  ],
  imports: [
    CommonModule,
    ChartNg2RoutingModule,
    SharedModule
  ]
})
export class ChartNg2Module { }
