import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AiIntelligenceRoutingModule } from './ai-intelligence-routing.module';
import { AiDashboardComponent } from './ai-dashboard/ai-dashboard.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [
    AiDashboardComponent
  ],
  imports: [
    CommonModule,
    AiIntelligenceRoutingModule,
    SharedModule
  ]
})
export class AiIntelligenceModule {}
