import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { HeaderService, Notification } from '../../common-component/header/header.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.scss',
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  isLoading = true;
  private destroy$ = new Subject<void>();

  constructor(private headerService: HeaderService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private getEntrepriseId(): number | null {
    const item = localStorage.getItem('myLSkey');
    if (!item) return null;
    try {
      const user = JSON.parse(decodeURIComponent(escape(atob(item))));
      return user.entrepriseId || null;
    } catch {
      return null;
    }
  }

  loadNotifications(): void {
    const entrepriseId = this.getEntrepriseId();
    if (!entrepriseId) {
      this.isLoading = false;
      return;
    }
    this.isLoading = true;
    this.headerService.getNotifications(entrepriseId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.notifications = Array.isArray(data) ? data : [];
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
        }
      });
  }

  markRead(id: number): void {
    this.headerService.markNotificationRead(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const notif = this.notifications.find(n => n.id === id);
          if (notif) notif.statut = 'VU';
        }
      });
  }

  markAllRead(): void {
    const entrepriseId = this.getEntrepriseId();
    if (!entrepriseId) return;
    this.headerService.markAllNotificationsRead(entrepriseId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notifications.forEach(n => n.statut = 'VU');
        }
      });
  }

  get unreadCount(): number {
    return this.notifications.filter(n => n.statut === 'NOUVEAU').length;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}
