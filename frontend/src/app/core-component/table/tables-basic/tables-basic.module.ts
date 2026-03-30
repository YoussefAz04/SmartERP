import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TablesBasicRoutingModule } from './tables-basic-routing.module';
import { TablesBasicComponent } from './tables-basic.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [TablesBasicComponent],
  imports: [CommonModule, TablesBasicRoutingModule, SharedModule],
})
export class TablesBasicModule {}
