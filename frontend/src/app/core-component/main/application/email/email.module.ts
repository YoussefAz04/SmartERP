import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { EmailRoutingModule } from './email-routing.module';
import { EmailComponent } from './email.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [EmailComponent],
  imports: [CommonModule, EmailRoutingModule, SharedModule],
})
export class EmailModule {}
