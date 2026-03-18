import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SystemSettingsComponent } from './system-settings.component';
import { EmailSettingsComponent } from './email-settings/email-settings.component';
import { GdprSettingsComponent } from './gdpr-settings/gdpr-settings.component';
import { SmsGatewayComponent } from './sms-gateway/sms-gateway.component';
import { OtpSettingsComponent } from './otp-settings/otp-settings.component';

const routes: Routes = [
  {
    path: '',
    component: SystemSettingsComponent,
    children: [
      {
        path: 'email-settings',
        component: EmailSettingsComponent,
      },
      {
        path: 'gdpr-settings',
        component: GdprSettingsComponent,
      },
      {
        path: 'sms-gateway',
        component: SmsGatewayComponent,
      },
      {
        path: 'otp-settings',
        component: OtpSettingsComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SystemSettingsRoutingModule {}
