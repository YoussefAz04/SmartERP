import { Component } from '@angular/core';
import { SidebarService } from 'src/app/core/service/sidebar/sidebar.service';

@Component({
  selector: 'app-preference',
  templateUrl: './preference.component.html',
  styleUrl: './preference.component.scss'
})
export class PreferenceComponent {
  constructor(private sidebar: SidebarService) {}
  isCollapsed: boolean = false;
  toggleCollapse() {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }
}
