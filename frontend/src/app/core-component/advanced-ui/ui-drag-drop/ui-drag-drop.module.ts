import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UiDragDropRoutingModule } from './ui-drag-drop-routing.module';
import { UiDragDropComponent } from './ui-drag-drop.component';
import { SharedModule } from 'src/app/shared/shared.module';


@NgModule({
  declarations: [
    UiDragDropComponent
  ],
  imports: [
    CommonModule,
    UiDragDropRoutingModule,
    SharedModule
  ]
})
export class UiDragDropModule { }
