import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UiTooltipsRoutingModule } from './ui-tooltips-routing.module';
import { UiTooltipsComponent } from './ui-tooltips.component';
import { SharedModule } from 'src/app/shared/shared.module';


@NgModule({
  declarations: [
    UiTooltipsComponent
  ],
  imports: [
    CommonModule,
    UiTooltipsRoutingModule,
    SharedModule
  ]
})
export class UiTooltipsModule { }
