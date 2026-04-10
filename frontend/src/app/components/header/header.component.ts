import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderService } from './header.service';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-erp-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class ErpHeaderComponent implements OnInit {
  currentUser: any = {};
  currentEnterprise: any = {};
  userInitials: string = 'U';

  searchQuery: string = '';
  searchResults: any = { clients: [], articles: [], orders: [] };
  showSearchResults: boolean = false;
  isSearching: boolean = false;
  private searchSubject = new Subject<string>();

  notifications: any[] = [];
  unreadCount: number = 0;
  showNotifications: boolean = false;

  showProfileDropdown: boolean = false;
  showEnterprisesDropdown: boolean = false;
  enterprises: any[] = [];

  isDarkMode: boolean = false;

  constructor(
    private headerService: HeaderService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadNotifications();
    this.loadThemePreference();
    this.setupSearch();
    this.loadEnterprises();
  }

  loadUserInfo(): void {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.currentUser = {
          firstName: payload.firstName || 'User',
          lastName: payload.lastName || '',
          email: payload.email
        };
        this.currentEnterprise = {
          id: payload.entrepriseId || payload.enterpriseId,
          name: payload.entrepriseName || payload.enterpriseName
        };
        const first = payload.firstName?.charAt(0) || 'U';
        const last = payload.lastName?.charAt(0) || '';
        this.userInitials = (first + last).toUpperCase();
      } catch (e) {
        console.error('Error parsing token:', e);
      }
    }
  }

  loadNotifications(): void {
    this.headerService.notifications$.subscribe(notifications => {
      this.notifications = notifications.sort((a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
    });

    this.headerService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
    });
  }

  setupSearch(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => {
      if (query.trim().length > 0) {
        this.performSearch(query);
      } else {
        this.searchResults = { clients: [], articles: [], orders: [] };
        this.showSearchResults = false;
      }
    });
  }

  performSearch(query: string): void {
    this.isSearching = true;
    this.showSearchResults = true;

    this.headerService.searchClients(query).subscribe(
      clients => { this.searchResults.clients = clients.slice(0, 5); },
      error => console.error('Error searching clients:', error)
    );

    this.headerService.searchArticles(query).subscribe(
      articles => { this.searchResults.articles = articles.slice(0, 5); },
      error => console.error('Error searching articles:', error)
    );

    this.headerService.searchOrders(query).subscribe(
      orders => {
        this.searchResults.orders = orders.slice(0, 5);
        this.isSearching = false;
      },
      error => {
        console.error('Error searching orders:', error);
        this.isSearching = false;
      }
    );
  }

  onSearchChange(query: string): void {
    this.searchQuery = query;
    this.searchSubject.next(query);
  }

  navigateToResult(type: string, id: number): void {
    this.searchQuery = '';
    this.showSearchResults = false;

    switch (type) {
      case 'client':
        this.router.navigate(['/clients', id]);
        break;
      case 'article':
        this.router.navigate(['/articles', id]);
        break;
      case 'order':
        this.router.navigate(['/commandes', id]);
        break;
    }
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    this.showProfileDropdown = false;
  }

  markNotificationAsRead(notification: any): void {
    if (!notification.read) {
      this.headerService.markAsRead(notification.id).subscribe(() => {
        notification.read = true;
        this.headerService.loadNotifications();
      });
    }
  }

  markAllNotificationsAsRead(): void {
    this.headerService.markAllAsRead().subscribe(() => {
      this.headerService.loadNotifications();
    });
  }

  toggleProfileDropdown(): void {
    this.showProfileDropdown = !this.showProfileDropdown;
    this.showNotifications = false;
  }

  goToProfile(): void {
    this.router.navigate(['/profile']);
    this.showProfileDropdown = false;
  }

  goToSettings(): void {
    this.router.navigate(['/settings']);
    this.showProfileDropdown = false;
  }

  loadEnterprises(): void {
    this.headerService.getUserEnterprises().subscribe(
      enterprises => {
        this.enterprises = enterprises;
      },
      error => console.error('Error loading enterprises:', error)
    );
  }

  switchEnterprise(enterpriseId: number): void {
    this.headerService.switchEnterprise(enterpriseId).subscribe(
      response => {
        localStorage.setItem('token', response.token);
        window.location.reload();
      },
      error => console.error('Error switching enterprise:', error)
    );
  }

  toggleEnterprisesDropdown(): void {
    this.showEnterprisesDropdown = !this.showEnterprisesDropdown;
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
    document.body.classList.toggle('dark-mode');
  }

  loadThemePreference(): void {
    const theme = localStorage.getItem('theme') || 'light';
    this.isDarkMode = theme === 'dark';
    if (this.isDarkMode) {
      document.body.classList.add('dark-mode');
    }
  }

  logout(): void {
    this.headerService.logout().subscribe(
      () => {
        localStorage.removeItem('token');
        localStorage.removeItem('theme');
        this.router.navigate(['/login']);
      },
      error => {
        console.error('Error during logout:', error);
        localStorage.removeItem('token');
        this.router.navigate(['/login']);
      }
    );
  }

  closeDropdowns(): void {
    this.showNotifications = false;
    this.showProfileDropdown = false;
    this.showEnterprisesDropdown = false;
    this.showSearchResults = false;
  }
}
