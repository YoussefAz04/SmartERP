// header.component.ts
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { NavigationStart, Router, Event as RouterEvent } from '@angular/router';
import { Subject, of, interval, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, takeUntil } from 'rxjs/operators';
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
export class HeaderComponent implements OnInit, OnDestroy {
  // Routes
  public routes = routes;

  // Theme - synced with SettingsService
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
  private notificationPollSub: Subscription | null = null;

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

  // Cleanup
  private destroy$ = new Subject<void>();

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
    // Sync theme with SettingsService
    this.settings.themeMode.pipe(takeUntil(this.destroy$)).subscribe((mode: string) => {
      this.lightMode = mode !== 'dark_mode';
    });

    this.getUserAuthData();
    this.setupSearch();
    this.loadNotifications();
    this.startNotificationPolling();
    this.subscribeToServices();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.notificationPollSub) {
      this.notificationPollSub.unsubscribe();
    }
  }

  // ===== SETUP METHODS =====
  private setupSearch(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
      switchMap(query => {
        if (!query || query.length < 2) {
          this.searchResults = [];
          this.showSearchDropdown = false;
          return of([]);
        }
        this.isSearching = true;
        this.showSearchDropdown = true;
        return this.headerService.searchGlobal(query).pipe(
          catchError(() => of([]))
        );
      })
    ).subscribe((results: SearchResult[]) => {
      this.searchResults = results;
      this.isSearching = false;
    });
  }

  private subscribeToServices(): void {
    this.sidebar.sideBarPosition.pipe(takeUntil(this.destroy$)).subscribe((res: string) => {
      this.miniSidebar = res === 'true';
    });
    this.common.base.pipe(takeUntil(this.destroy$)).subscribe((base: string) => {
      this.base = base;
    });
    this.common.page.pipe(takeUntil(this.destroy$)).subscribe((page: string) => {
      this.page = page;
    });
    this.common.last.pipe(takeUntil(this.destroy$)).subscribe((last: string) => {
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
    // Delegate to SettingsService which handles localStorage and BehaviorSubject
    this.settings.changeThemeMode(mode);
  }

  // ===== NOTIFICATION METHODS =====
  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    this.showProfile = false;
    // Refresh notifications when opening
    if (this.showNotifications) {
      this.loadNotifications();
    }
  }

  private startNotificationPolling(): void {
    // Poll every 30 seconds for new notifications
    this.notificationPollSub = interval(30000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.loadNotifications();
      });
  }

  loadNotifications(): void {
    const entrepriseId = this.getCurrentEnterpriseId();
    if (!entrepriseId) return;

    this.isLoadingNotifications = true;
    this.headerService.getNotifications(entrepriseId).subscribe({
      next: (data) => {
        this.notifications = Array.isArray(data) ? data : [];
        this.unreadCount = this.notifications.filter(n => n.statut === 'NOUVEAU').length;
        this.isLoadingNotifications = false;
      },
      error: () => {
        this.isLoadingNotifications = false;
      }
    });
  }

  markRead(id: number): void {
    this.headerService.markNotificationRead(id).subscribe({
      next: () => {
        const notif = this.notifications.find(n => n.id === id);
        if (notif) {
          notif.statut = 'VU';
          this.unreadCount = this.notifications.filter(n => n.statut === 'NOUVEAU').length;
        }
      }
    });
  }

  markAllRead(): void {
    const entrepriseId = this.getCurrentEnterpriseId();
    if (!entrepriseId) return;

    this.headerService.markAllNotificationsRead(entrepriseId).subscribe({
      next: () => {
        this.notifications.forEach(n => n.statut = 'VU');
        this.unreadCount = 0;
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
  private getCurrentUser(): ResponseDto | null {
    const item = localStorage.getItem('myLSkey');
    if (!item) return null;
    try {
      return JSON.parse(decodeURIComponent(escape(atob(item)))) as ResponseDto;
    } catch {
      return null;
    }
  }

  getUserAuthData(): void {
    const currentUser = this.getCurrentUser();
    if (!currentUser) return;
    this.userName = currentUser.name || 'User';
    this.roleName = Array.isArray(currentUser.roles) ? currentUser.roles : [];

    if (currentUser.entreprise && currentUser.entreprise.nom) {
      this.entrepriseName = currentUser.entreprise.nom;
    } else if (currentUser.entrepriseName) {
      this.entrepriseName = currentUser.entrepriseName;
    }

    if (this.userName) {
      this.userInitials = this.userName.split(' ')
        .map(n => n.charAt(0))
        .join('')
        .toUpperCase()
        .slice(0, 2);
    }
  }

  private getCurrentEnterpriseId(): number | null {
    const currentUser = this.getCurrentUser();
    const fromUser = currentUser?.entrepriseId ?? currentUser?.entreprise?.id;
    if (fromUser !== null && fromUser !== undefined && !Number.isNaN(Number(fromUser))) {
      return Number(fromUser);
    }

    const fromStorage = localStorage.getItem('entrepriseId') ?? localStorage.getItem('enterpriseId');
    if (fromStorage !== null && fromStorage !== undefined && !Number.isNaN(Number(fromStorage))) {
      return Number(fromStorage);
    }

    return null;
  }

  // ===== OTHER METHODS =====
  getResultRoute(result: SearchResult): string[] {
    switch (result.type) {
      case 'Client':
        return ['/client/editClient', String(result.id)];
      case 'Article':
        return ['/article-achat/edit', String(result.id)];
      case 'Fournisseur':
        return ['/fournisseur/editFournisseur', String(result.id)];
      case 'Commande':
        return ['/commande-achat/edit', String(result.id)];
      default:
        return ['/'];
    }
  }

  navigateToResult(result: SearchResult): void {
    this.clearSearch();
    this.router.navigate(this.getResultRoute(result));
  }

  toggleSidebar(): void {
    const currentState = localStorage.getItem('sidebarState');
    const newState = currentState === 'collapsed' ? 'expanded' : 'collapsed';
    localStorage.setItem('sidebarState', newState);
    this.sidebar.sideBarPosition.next(newState === 'collapsed' ? 'true' : 'false');
    document.body.classList.toggle('mini-sidebar');
  }

  togglesMobileSideBar(): void {
    document.body.classList.toggle('slide-nav');
  }

  // ===== CLOSE DROPDOWNS ON CLICK OUTSIDE =====
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;

    if (!target.closest('.notifications-wrapper')) {
      this.showNotifications = false;
    }

    if (!target.closest('.profile-wrapper')) {
      this.showProfile = false;
    }

    if (!target.closest('.search-wrapper')) {
      this.showSearchDropdown = false;
    }
  }
}
