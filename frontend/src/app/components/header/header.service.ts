import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HeaderService {
  private apiUrl = 'http://localhost:8020/api';

  private notificationsSubject = new BehaviorSubject<any[]>([]);
  notifications$ = this.notificationsSubject.asObservable();

  private unreadCountSubject = new BehaviorSubject<number>(0);
  unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadNotifications();
  }

  // Search Clients
  searchClients(query: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/clients/search`, {
      params: { q: query },
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
  }

  // Search Articles
  searchArticles(query: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/articles/search`, {
      params: { q: query },
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
  }

  // Search Orders
  searchOrders(query: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/commandes/search`, {
      params: { q: query },
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
  }

  // Get Notifications
  loadNotifications(): void {
    this.http.get<any[]>(`${this.apiUrl}/notifications`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    }).subscribe(
      (notifications) => {
        this.notificationsSubject.next(notifications);
        const unreadCount = notifications.filter(n => !n.read).length;
        this.unreadCountSubject.next(unreadCount);
      },
      (error) => console.error('Error loading notifications:', error)
    );
  }

  // Mark as Read
  markAsRead(notificationId: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/notifications/${notificationId}/read`, {}, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
  }

  // Mark All as Read
  markAllAsRead(): Observable<any> {
    return this.http.patch(`${this.apiUrl}/notifications/read-all`, {}, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
  }

  // Get User Profile
  getUserProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/profile`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
  }

  // Get User Enterprises
  getUserEnterprises(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users/enterprises`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
  }

  // Switch Enterprise
  switchEnterprise(enterpriseId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/switch-enterprise`,
      { enterpriseId },
      {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
      }
    );
  }

  // Logout
  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/logout`, {}, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
  }
}
