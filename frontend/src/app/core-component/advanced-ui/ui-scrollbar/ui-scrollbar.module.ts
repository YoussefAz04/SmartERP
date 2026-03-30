import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UiScrollbarRoutingModule } from './ui-scrollbar-routing.module';
import { UiScrollbarComponent } from './ui-scrollbar.component';
import { SharedModule } from 'src/app/shared/shared.module';


@NgModule({
  declarations: [
    UiScrollbarComponent
  ],
  imports: [
    CommonModule,
    UiScrollbarRoutingModule,
    SharedModule,
  ]
})
export class UiScrollbarModule { }
