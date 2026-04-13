import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AiDashboardComponent } from './ai-dashboard/ai-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: AiDashboardComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AiIntelligenceRoutingModule {}
