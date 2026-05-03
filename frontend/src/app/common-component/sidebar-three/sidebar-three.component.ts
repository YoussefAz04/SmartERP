import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationStart, Router, Event as RouterEvent } from '@angular/router';
import { SidebarService } from 'src/app/core/core.index';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-sidebar-three',
  templateUrl: './sidebar-three.component.html',
  styleUrls: ['./sidebar-three.component.scss'],
})
export class SidebarThreeComponent implements OnInit, OnDestroy {
  opendSubMenu: Array<string | number> = [];
  public activePath = '';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public sidebarData: Array<any> = [];

  private dataSub?: Subscription;

  constructor(private sidebar: SidebarService, private router: Router) {
    this.activePath = this.router.url.split('/')[1];
    this.router.events.subscribe((data: RouterEvent) => {
      if (data instanceof NavigationStart) {
        this.activePath = data.url.split('/')[1];
      }
      this.opendSubMenu[0] = ''
    });
  }

  ngOnInit(): void {
    this.dataSub = this.sidebar.sidebarData$.subscribe(data => {
      this.sidebarData = this.transformSidebarData(data);
    });
    this.sidebar.loadSidebarWithRefresh();
  }

  ngOnDestroy(): void {
    this.dataSub?.unsubscribe();
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private transformSidebarData(data: any[]): any[] {
    return data.map(section => ({
      tittle: section.tittle,
      featherIcon: this.sidebar.getSectionFeatherIcon(section.tittle),
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      subRoutes: (section.menu || []).map((item: any) => ({
        tittle: item.menuValue,
        hasSubRoute: item.hasSubRoute,
        showSubRoute: item.showSubRoute,
        route: item.route,
        activeRoute: item.base1,
        featherIcon: item.icon || 'grid',
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        subRoutes: (item.subMenus || []).map((sub: any) => ({
          tittle: sub.menuValue,
          route: sub.route,
        }))
      }))
    }));
  }

  showMenu(val: string): void {
    if (this.opendSubMenu[0] != val) {
      this.opendSubMenu[0] = val;
    } else {
      this.opendSubMenu[0] = '';
    }
  }

  showSubMenu(val: string): void {
    if (this.opendSubMenu[1] != val) {
      this.opendSubMenu[1] = val;
    } else {
      this.opendSubMenu[1] = '';
    }
  }
}
