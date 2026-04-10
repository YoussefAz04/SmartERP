import { Component, OnInit } from '@angular/core';
import { NavigationStart, Router, Event as RouterEvent } from '@angular/router';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { CommonService, SettingsService, SidebarService } from 'src/app/core/core.index';
import { WebstorageService } from 'src/app/shared/webstorage.service';
import { routes } from 'src/app/core/helpers/routes';
import { ResponseDto } from "../../model/reponseDto.model";
import { AuthService } from "../../auth/service/auth.service";
import { HeaderService, SearchResult, Notification } from './header.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit {
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

  // Search
  searchQuery: string = '';
  searchResults: SearchResult[] = [];
  isSearching: boolean = false;
  showSearchDropdown: boolean = false;
  private searchSubject = new Subject<string>();

  // Notifications
  notifications: Notification[] = [];
  unreadCount: number = 0;
  isLoadingNotifications: boolean = false;

  // Enterprise
  entrepriseName: string = '';
  entrepriseId: number | null = null;

  // Profile
  userInitials: string = '';

  userName!: string | undefined;
  roleName!: string[];

  constructor(
    private Router: Router,
    private common: CommonService,
    private sidebar: SidebarService,
    private webStorage: WebstorageService,
    private settings: SettingsService,
    private authService: AuthService,
    private headerService: HeaderService
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
    this.lightMode = true;
    this.getUserAuthData();

    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(query => {
        if (!query || query.length < 2) {
          this.searchResults = [];
          this.showSearchDropdown = false;
          return of([]);
        }
        this.isSearching = true;
        return this.headerService.searchGlobal(query).pipe(
          catchError(() => of([]))
        );
      })
    ).subscribe((results: SearchResult[]) => {
      this.searchResults = results;
      this.isSearching = false;
      this.showSearchDropdown = results.length > 0;
    });

    this.loadNotifications();
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
    if (theme.includes('light_mode')) {
      this.lightMode = true;
    } else if (theme.includes('dark_mode')) {
      this.lightMode = false;
    } else {
      this.lightMode = false;
    }
    localStorage.setItem('themeMode', theme);
  }

  getUserAuthData() {
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      this.userName = currentUser?.name;

      this.entrepriseName = currentUser?.entrepriseName || currentUser?.entreprise?.nom || '';
      this.entrepriseId = currentUser?.entrepriseId || currentUser?.entreprise?.id || null;

      if (this.userName) {
        const parts = this.userName.trim().split(' ').filter(p => p.length > 0);
        this.userInitials = parts.length > 1
          ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
          : this.userName.substring(0, 2).toUpperCase();
      }
    }
    this.roleName = this.authService.getRoles();
  }

  onSearchBlur(): void {
    setTimeout(() => {
      this.showSearchDropdown = false;
    }, 200);
  }

  onSearchInput(query: string): void {
    this.searchSubject.next(query);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults = [];
    this.showSearchDropdown = false;
  }

  loadNotifications(): void {
    this.isLoadingNotifications = true;
    this.headerService.getNotifications().subscribe(notifs => {
      this.notifications = notifs;
      this.unreadCount = notifs.filter(n => !n.lu).length;
      this.isLoadingNotifications = false;
    });
  }

  markAllRead(): void {
    this.headerService.markAllNotificationsRead().subscribe(() => {
      this.notifications.forEach(n => n.lu = true);
      this.unreadCount = 0;
    });
  }

  markRead(id: number): void {
    this.headerService.markNotificationRead(id).subscribe(() => {
      const notif = this.notifications.find(n => n.id === id);
      if (notif) {
        notif.lu = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      }
    });
  }
}