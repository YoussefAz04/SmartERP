import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppSettingsComponent } from './app-settings.component';
import { CustomFieldsComponent } from './custom-fields/custom-fields.component';
import { InvoiceSettingsComponent } from './invoice-settings/invoice-settings.component';
import { PrinterSettingsComponent } from './printer-settings/printer-settings.component';
import { PosSettingsComponent } from './pos-settings/pos-settings.component';

const routes: Routes = [
  {
    path: '',
    component: AppSettingsComponent,
    children: [
      {
        path: 'custom-fields',
        component: CustomFieldsComponent,
      },
      {
        path: 'invoice-settings',
        component: InvoiceSettingsComponent,
      },
      {
        path: 'printer-settings',
        component: PrinterSettingsComponent,
      },
      {
        path: 'pos-settings',
        component: PosSettingsComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AppSettingsRoutingModule {}
