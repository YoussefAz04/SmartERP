import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AiIntelligenceRoutingModule } from './ai-intelligence-routing.module';
import { AiDashboardComponent } from './ai-dashboard/ai-dashboard.component';
import { ComparisonMetricsComponent } from './ai-dashboard/comparison-metrics/comparison-metrics.component';
import { KpiDetailModalComponent } from './ai-dashboard/kpi-detail-modal/kpi-detail-modal.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [
    AiDashboardComponent,
    ComparisonMetricsComponent,
    KpiDetailModalComponent,
  ],
  imports: [
    CommonModule,
    AiIntelligenceRoutingModule,
    SharedModule
  ]
})
export class AiIntelligenceModule {}
