import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeatherModule } from 'angular-feather';

import { User, Settings} from 'angular-feather/icons';


const icons = {
  User,
  Settings
};

@NgModule({
  declarations: [],

  imports: [
    CommonModule,
    FeatherModule.pick(icons),
  ]
})
export class HeaderModule { }
