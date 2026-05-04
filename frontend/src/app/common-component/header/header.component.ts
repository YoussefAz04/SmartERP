// header.component.ts
import { Component, OnInit, OnDestroy, HostListener, ViewChild, ElementRef } from '@angular/core';
import { NavigationStart, Router, Event as RouterEvent } from '@angular/router';
import { Subject, of, interval, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, takeUntil } from 'rxjs/operators';
import { CommonService, SettingsService, SidebarService } from 'src/app/core/core.index';
import { WebstorageService } from 'src/app/shared/webstorage.service';
import { routes } from 'src/app/core/helpers/routes';
import { ResponseDto } from "../../model/reponseDto.model";
import { AuthService } from "../../auth/service/auth.service";
import { HeaderService, SearchResult, Notification } from './header.service';

export interface SearchResultGroup {
  type: string;
  label: string;
  icon: string;
  results: SearchResult[];
}

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
  public selectedIndex = -1;
  private searchSubject = new Subject<string>();

  @ViewChild('searchInput') searchInputRef!: ElementRef<HTMLInputElement>;

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
  public isSuperAdmin = false;

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
        this.getUserAuthData();
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
      this.selectedIndex = -1;
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
      this.selectedIndex = -1;
    }, 300);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults = [];
    this.showSearchDropdown = false;
    this.selectedIndex = -1;
  }

  onSearchKeydown(event: KeyboardEvent): void {
    if (!this.showSearchDropdown) return;
    const total = this.searchResults.length;
    if (total === 0) return;

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedIndex = (this.selectedIndex + 1) % total;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedIndex = (this.selectedIndex - 1 + total) % total;
    } else if (event.key === 'Enter' && this.selectedIndex >= 0) {
      event.preventDefault();
      this.navigateToResult(this.searchResults[this.selectedIndex]);
    } else if (event.key === 'Escape') {
      this.clearSearch();
    }
  }

  get groupedResults(): SearchResultGroup[] {
    const groupMap: { [key: string]: SearchResultGroup } = {
      Client:      { type: 'Client',      label: 'Clients',     icon: 'icon-users',     results: [] },
      Article:     { type: 'Article',     label: 'Articles',    icon: 'icon-box',       results: [] },
      Fournisseur: { type: 'Fournisseur', label: 'Fournisseurs',icon: 'icon-truck',     results: [] },
      Commande:    { type: 'Commande',    label: 'Commandes',   icon: 'icon-file-text', results: [] },
    };
    for (const r of this.searchResults) {
      if (groupMap[r.type]) {
        groupMap[r.type].results.push(r);
      }
    }
    return Object.values(groupMap).filter(g => g.results.length > 0);
  }

  getFlatIndex(result: SearchResult): number {
    return this.searchResults.indexOf(result);
  }

  highlightText(text: string): string {
    if (!text) return '';
    // HTML-escape first to prevent XSS before injecting into innerHTML
    const safe = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
    if (!this.searchQuery) return safe;
    const queryEscaped = this.searchQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    return safe.replace(new RegExp(`(${queryEscaped})`, 'gi'), '<mark>$1</mark>');
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
    const currentUser = this.getCurrentUser();
    if (!currentUser?.entrepriseId) return;

    this.isLoadingNotifications = true;
    this.headerService.getNotifications(currentUser.entrepriseId).subscribe({
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
    const currentUser = this.getCurrentUser();
    if (!currentUser?.entrepriseId) return;

    this.headerService.markAllNotificationsRead(currentUser.entrepriseId).subscribe({
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
    this.isSuperAdmin = currentUser.roleId === 0;

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

  // ===== OTHER METHODS =====
  getResultRoute(result: SearchResult): string[] {
    if (result.route?.length) {
      return result.route;
    }

    switch (result.type) {
      case 'Client':
        return ['/client/editClient', String(result.id)];
      case 'Article':
        return ['/article-achat/edit', String(result.id)];
      case 'Fournisseur':
        return ['/fournisseur/editFournisseur', String(result.id)];
      case 'Commande':
        return ['/commande-vente/edit', String(result.id)];
      default:
        return [routes.adminDashboard];
    }
  }

  navigateToResult(result: SearchResult): void {
    this.clearSearch();
    this.router.navigate(this.getResultRoute(result));
  }

  /**
   * Build an href for a result so the anchor behaves like a real link
   * (middle-click / Ctrl+click can open it in a new tab). The Angular
   * router still owns same-tab navigation via onResultClick().
   */
  getResultHref(result: SearchResult): string {
    const segments = this.getResultRoute(result).map(s => String(s));
    const path = segments
      .map(s => s.replace(/^\/+|\/+$/g, ''))
      .filter(s => s.length > 0)
      .join('/');
    return '/' + path;
  }

  /**
   * Handle clicks on a search result. For plain left-clicks we take over
   * with the Angular router (so the SPA navigates correctly even when the
   * dropdown is being torn down by *ngIf). Modifier-key / middle-clicks
   * fall through to the browser default so the user can open the result
   * in a new tab.
   */
  onResultClick(event: MouseEvent, result: SearchResult): void {
    if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) {
      // Let the browser handle modifier / middle clicks via the href.
      return;
    }
    event.preventDefault();
    this.navigateToResult(result);
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
  @HostListener('document:keydown', ['$event'])
  onGlobalKeydown(event: KeyboardEvent): void {
    if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
      event.preventDefault();
      this.searchInputRef?.nativeElement?.focus();
      this.showSearchDropdown = true;
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {    const target = event.target as HTMLElement;

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
