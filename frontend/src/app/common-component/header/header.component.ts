// header.component.ts
import { Component, OnInit, HostListener } from '@angular/core';
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
  // Routes
  public routes = routes;

  // Theme
  public lightMode = true;

  // Sidebar
  public miniSidebar = false;

  // Search
  public searchQuery = '';
  public searchResults: SearchResult[] = [];
  public isSearching = false;
  public showSearchDropdown = false;
  private searchSubject = new Subject<string>();

  // Notifications
  public notifications: Notification[] = [];
  public unreadCount = 0;
  public isLoadingNotifications = false;
  public showNotifications = false;

  // User Info
  public userInitials = '';
  public userName = '';
  public roleName: string[] = [];
  public entrepriseName = '';
  public showProfile = false;

  // Page tracking
  public activePath = '';
  public base = '';
  public page = '';
  public last = '';

  constructor(
    private router: Router,
    private common: CommonService,
    private sidebar: SidebarService,
    private webStorage: WebstorageService,
    private settings: SettingsService,
    private authService: AuthService,
    private headerService: HeaderService
  ) {
    this.activePath = this.router.url.split('/')[2];
    this.router.events.subscribe((data: RouterEvent) => {
      if (data instanceof NavigationStart) {
        this.activePath = data.url.split('/')[2];
      }
    });
  }

  ngOnInit(): void {
    this.lightMode = true;
    this.getUserAuthData();
    this.setupSearch();
    this.loadNotifications();
    this.subscribeToServices();
  }

  // ===== SETUP METHODS =====
  private setupSearch(): void {
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
  }

  private subscribeToServices(): void {
    this.sidebar.sideBarPosition.subscribe((res: string) => {
      this.miniSidebar = res === 'true';
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

  // ===== SEARCH METHODS =====
  onSearchInput(query: string): void {
    this.searchQuery = query;
    this.searchSubject.next(query);
  }

  onSearchBlur(): void {
    setTimeout(() => {
      this.showSearchDropdown = false;
    }, 200);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults = [];
    this.showSearchDropdown = false;
  }

  // ===== THEME METHODS =====
  changeThemeMode(mode: string): void {
    // Store theme in localStorage
    localStorage.setItem('theme', mode);
    
    // Apply theme
    this.lightMode = mode === 'light_mode';
    const body = document.body;
    
    if (mode === 'dark_mode') {
      body.classList.add('dark-mode');
    } else {
      body.classList.remove('dark-mode');
    }
  }

  // ===== NOTIFICATION METHODS =====
  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    this.showProfile = false;
  }

  loadNotifications(): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const currentUser: ResponseDto = JSON.parse(
          decodeURIComponent(escape(atob(item)))
        );
        const entrepriseId = currentUser.entrepriseId || 1;
        
        this.isLoadingNotifications = true;
        this.headerService.getNotifications(entrepriseId).subscribe({
          next: (data) => {
            this.notifications = data;
            this.unreadCount = data.filter(n => !n.lu).length;
            this.isLoadingNotifications = false;
          },
          error: (err) => {
            console.error('Error loading notifications:', err);
            this.isLoadingNotifications = false;
          }
        });
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
  }

  markRead(id: number): void {
    this.headerService.markNotificationRead(id).subscribe({
      next: () => {
        const notif = this.notifications.find(n => n.id === id);
        if (notif) {
          notif.lu = true;
          this.unreadCount = this.notifications.filter(n => !n.lu).length;
        }
      },
      error: (err) => {
        console.error('Error marking notification as read:', err);
      }
    });
  }

  markAllRead(): void {
    this.headerService.markAllNotificationsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.lu = true);
        this.unreadCount = 0;
      },
      error: (err) => {
        console.error('Error marking all notifications as read:', err);
      }
    });
  }

  // ===== PROFILE METHODS =====
  toggleProfile(): void {
    this.showProfile = !this.showProfile;
    this.showNotifications = false;
  }

  logout(): void {
    this.webStorage.Logout();
  }

  // ===== USER AUTH DATA =====
  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const currentUser: ResponseDto = JSON.parse(
          decodeURIComponent(escape(atob(item)))
        );
        this.userName = currentUser.name || 'User';
        this.roleName = Array.isArray(currentUser.roles) ? currentUser.roles : [];
        
        // Get enterprise name
        if (currentUser.entreprise && currentUser.entreprise.nom) {
          this.entrepriseName = currentUser.entreprise.nom;
        } else if (currentUser.entrepriseName) {
          this.entrepriseName = currentUser.entrepriseName;
        }
        
        // Generate initials
        if (this.userName) {
          this.userInitials = this.userName.split(' ')
            .map(n => n.charAt(0))
            .join('')
            .toUpperCase()
            .slice(0, 2);
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
  }

  // ===== OTHER METHODS =====
  toggleSidebar(): void {
    // Toggle sidebar by updating localStorage or using sidebar service
    const currentState = localStorage.getItem('sidebarState');
    const newState = currentState === 'collapsed' ? 'expanded' : 'collapsed';
    localStorage.setItem('sidebarState', newState);
    
    // Emit event to sidebar
    this.sidebar.sideBarPosition.next(newState === 'collapsed' ? 'true' : 'false');
    
    // Toggle body class
    document.body.classList.toggle('mini-sidebar');
  }

  togglesMobileSideBar(): void {
    document.body.classList.toggle('slide-nav');
  }

  fullscreen(): void {
    const elem = document.documentElement as any;
    if (elem.requestFullscreen) {
      elem.requestFullscreen();
    } else if (elem.mozRequestFullScreen) {
      elem.mozRequestFullScreen();
    } else if (elem.webkitRequestFullscreen) {
      elem.webkitRequestFullscreen();
    } else if (elem.msRequestFullscreen) {
      elem.msRequestFullscreen();
    }
  }

  // ===== CLOSE DROPDOWNS ON CLICK OUTSIDE =====
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    
    // Check if click is outside dropdowns
    const isOutsideNotifications = !target.closest('.notifications-wrapper');
    const isOutsideProfile = !target.closest('.profile-wrapper');
    const isOutsideSearch = !target.closest('.search-wrapper');
    
    if (isOutsideNotifications) {
      this.showNotifications = false;
    }
    
    if (isOutsideProfile) {
      this.showProfile = false;
    }
    
    if (isOutsideSearch) {
      this.showSearchDropdown = false;
    }
  }
}