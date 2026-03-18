import { Component, ChangeDetectionStrategy  } from '@angular/core';
import { routes } from 'src/app/core/helpers/routes';
import { CommonService } from 'src/app/core/service/common/common.service';
import { SidebarService } from 'src/app/core/service/sidebar/sidebar.service';
interface MenuItem {
  icon: string;
  title: string;
  page: string;
  subMenu: { title: string; routes: string }[];
  expanded?: boolean; 
}
@Component({
  selector: 'app-settings-sidebar',
  templateUrl: './settings-sidebar.component.html',
  styleUrl: './settings-sidebar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsSidebarComponent {
  public routes = routes;
  base = '';
  page = '';
  last = '';

constructor(private common: CommonService, private setting: SidebarService){
  this.common.base.subscribe((base: string) => {
    this.base = base;
  });
  this.common.page.subscribe((page: string) => {
    this.page = page;
  });
  this.common.last.subscribe((last: string) => {
    this.last = last;
  });
  this.menuItems = this.setting.settings_sidebar;
}

 
public menuItems: Array<MenuItem> = [];
toggleSubMenu(menuItem: MenuItem): void {
  menuItem.expanded = !menuItem.expanded;
}
}
