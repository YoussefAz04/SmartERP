import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChartNg2Component } from './chart-ng2.component';

const routes: Routes = [{ path: '', component: ChartNg2Component }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ChartNg2RoutingModule { }
