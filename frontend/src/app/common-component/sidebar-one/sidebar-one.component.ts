import {Component, OnInit} from '@angular/core';
import { SidebarService, routes } from 'src/app/core/core.index';
import { NavigationEnd, Router, Event as RouterEvent } from '@angular/router';
import { url } from 'src/app/shared/model/sidebar.model';

interface MenuItem {
  menuValue: string;
  showSubRoute: boolean;
  hasSubRoute?: boolean;
  icon?: string;
  route?: string;
  base?: string;
  base1?: string;
  page?: string;
  subMenus?: SubMenu[];
}

interface SubMenu {
  menuValue: string;
  showSubRoute: boolean;
  route?: string;
  customSubmenuTwo?: boolean;
  subMenusTwo?: SubMenu[];
  page1?: string;
  page2?: string;
  page3?: string;
  page4?: string;
  page5?: string;
  page6?: string;
  page7?: string;
  page8?: string;
}

interface SidebarSection {
  tittle: string;
  menu: MenuItem[];
  showAsTab: boolean;
  separateRoute: boolean;
  hasSubRoute: boolean;
  showSubRoute: boolean;
}


@Component({
  selector: 'app-sidebar-one',
  templateUrl: './sidebar-one.component.html',
  styleUrls: ['./sidebar-one.component.scss'],
})
export class SidebarOneComponent implements OnInit {
  public routes = routes;
  base = '';
  page = '';
  currentUrl = '';

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public side_bar_data: Array<any> = [];

  constructor(
    private sidebar: SidebarService,
    private router: Router,
  ) {
    router.events.subscribe((event: RouterEvent) => {
      if (event instanceof NavigationEnd) {
        this.getRoutes(event);
      }
    });
    this.getRoutes(this.router);
    this.sidebar.getAllModuleTitles().subscribe(modules => {
      this.sidebar.generateSidebarData(modules);
    });
    this.sidebar.sidebarData$.subscribe(data => {
      // Filter out sections with no active menu items to avoid orphaned section headers
      this.side_bar_data = data.filter((section: SidebarSection) => section.menu && section.menu.length > 0);
    });  }

  private getRoutes(route: url): void {
    const splitVal = route.url.split('/');
    this.currentUrl = route.url;
    this.base = splitVal[1];
    this.page = splitVal[2];
  }

  public miniSideBarMouseHover(position: string): void {
    const shouldExpand = position === 'over';
    if (this.isHoverExpanded === shouldExpand) {
      return;
    }
    this.isHoverExpanded = shouldExpand;
    this.sidebar.expandSideBar.next(shouldExpand);
  }

  expandSubMenus(menu: MenuItem): void {
    sessionStorage.setItem('menuValue', menu.menuValue);
    this.side_bar_data.forEach((section: SidebarSection) => {
      section.menu.forEach((resMenu: MenuItem) => {
        if (resMenu.menuValue === menu.menuValue) {
          menu.showSubRoute = !menu.showSubRoute;
        } else {
          resMenu.showSubRoute = false;
        }
      });
    });
  }

  openMenuItem: MenuItem | null = null;
  openSubmenuOneItem: SubMenu | null = null;
  multiLevel1 = false;
  multiLevel2 = false;
  multiLevel3 = false;
  private isHoverExpanded = false;

  openMenu(menu: MenuItem): void {
    if (this.openMenuItem === menu) {
      this.openMenuItem = null;
    } else {
      this.openMenuItem = menu;
    }
  }

  openSubmenuOne(subMenus: SubMenu): void {
    if (this.openSubmenuOneItem === subMenus) {
      this.openSubmenuOneItem = null;
    } else {
      this.openSubmenuOneItem = subMenus;
    }
  }

  multiLevelOne() {
    this.multiLevel1 = !this.multiLevel1;
  }

  multiLevelTwo() {
    this.multiLevel2 = !this.multiLevel2;
  }

  multiLevelThree() {
    this.multiLevel3 = !this.multiLevel3;
  }

  ngOnInit(): void {
    // Reload sidebar data on init to apply role-based filtering for the logged-in user.
    // Role and permission filtering is handled inside generateSidebarData using localStorage.
    this.sidebar.getAllModuleTitles().subscribe(modules => {
      this.sidebar.generateSidebarData(modules);
    });
  }


  synchroModule(){

  }



  // sendData() {
  //   this.sidebar.getAllModuleTitles().subscribe(data => {
  //     console.log(data);
  //     this.sidebar.updateData(data);
  //   });
  // }

}
