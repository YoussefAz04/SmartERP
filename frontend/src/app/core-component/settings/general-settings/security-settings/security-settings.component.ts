import { Component } from '@angular/core';
import { SidebarService } from 'src/app/core/service/sidebar/sidebar.service';

@Component({
  selector: 'app-security-settings',
  templateUrl: './security-settings.component.html',
  styleUrl: './security-settings.component.scss'
})
export class SecuritySettingsComponent {
  constructor( private sidebar: SidebarService){

  }
  isCollapsed: boolean = false;
  toggleCollapse() {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }
}
