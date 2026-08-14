import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingsSidebarComponent } from './settings-sidebar.component';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';


@NgModule({
  declarations: [
    SettingsSidebarComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    TranslateModule
  ],
  exports: [SettingsSidebarComponent],
})
export class SettingsSidebarModule { }
