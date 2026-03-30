import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CouponsRoutingModule } from './coupons-routing.module';
import { CouponsComponent } from './coupons.component';
import { SharedModule } from 'src/app/shared/shared.module';


@NgModule({
  declarations: [
    CouponsComponent
  ],
  imports: [
    CommonModule,
    CouponsRoutingModule,
    SharedModule
  ]
})
export class CouponsModule { }
