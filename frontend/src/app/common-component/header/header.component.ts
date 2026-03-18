import {Component, OnInit} from '@angular/core';
import { NavigationStart, Router, Event as RouterEvent } from '@angular/router';
import {CommonService, SettingsService, SidebarService} from 'src/app/core/core.index';
import { WebstorgeService } from 'src/app/shared/webstorge.service';
import { routes } from 'src/app/core/helpers/routes';
import {ResponseDto} from "../../model/reponseDto.model";
import {Role} from "../../model/role.model";
import {AuthService} from "../../auth/service/auth.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit{
  themeMode: string = 'light_mode';
  lightMode: boolean = false;

  public routes = routes;
  activePath = '';
  showSearch = false;
  public changeLayout = '1';
  public darkTheme = false;
  public logoPath = '';
  public miniSidebar = false;
  elem = document.documentElement;
  public addClass = false;
  base = '';
  page = '';
  last = '';

  constructor(
    private Router: Router,
    private common: CommonService,
    private sidebar: SidebarService,
    private webStorage: WebstorgeService,
    private settings: SettingsService,
    private authService : AuthService

  ) {
    this.activePath = this.Router.url.split('/')[2];
    this.Router.events.subscribe((data: RouterEvent) => {
      if (data instanceof NavigationStart) {
        this.activePath = data.url.split('/')[2];
      }
    });
    this.sidebar.sideBarPosition.subscribe((res: string) => {
      if (res == 'true') {
        this.miniSidebar = true;
      } else {
        this.miniSidebar = false;
      }
    });
    this.common.base.subscribe((base: string) => {
      this.base = base;
    });
    this.common.page.subscribe((page: string) => {
      this.page = page;
    });
    this.common.last.subscribe((last: string) => {
      this.last = last;
    });
  }

  ngOnInit(): void {
    this.lightMode=true;
    this.getUserAuthData();
    }



  public logout(): void {
    this.webStorage.Logout();
  }

  public toggleSidebar(): void {
    this.sidebar.switchSideMenuPosition();
  }

  public togglesMobileSideBar(): void {
    this.sidebar.switchMobileSideBarPosition();
  }

  public miniSideBarMouseHover(position: string): void {
    if (position == 'over') {
      this.sidebar.expandSideBar.next(true);
    } else {
      this.sidebar.expandSideBar.next(false);
    }
  }

  fullscreen() {
    if (!document.fullscreenElement) {
      this.elem.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
  }
  public changeThemeMode(theme: string): void {
    this.settings.themeMode.next(theme);
    if(theme.includes('light_mode')){
         this.lightMode= true ;
    }else if(theme.includes('dark_mode')){
      this.lightMode= false ;

    }else {
      this.lightMode= false ;
    }
    localStorage.setItem('themeMode', theme);
  }





  userName! : string | undefined ;
  roleName!: string[]

  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      console.log('User connecté:', currentUser?.name);
      this.userName = currentUser?.name;
      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      console.log('Entreprise:', currentUser?.entreprise.nom);
    }
    this.roleName =  this.authService.getRoles() ;
  }

}
