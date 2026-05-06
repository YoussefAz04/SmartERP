import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuditLog, AuditLogService } from '../../core/service/audit-log/audit-log.service';

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-log.component.html',
  styleUrl: './audit-log.component.scss',
})
export class AuditLogComponent implements OnInit, OnDestroy {
  logs: AuditLog[] = [];
  filteredLogs: AuditLog[] = [];
  isLoading = true;
  searchText = '';
  selectedAction = '';
  private destroy$ = new Subject<void>();

  readonly actionLabels: Record<string, string> = {
    LOGIN: 'Connexion',
    USER_CREATED: 'Création utilisateur',
    USER_UPDATED: 'Modification utilisateur',
    USER_DELETED: 'Suppression utilisateur',
    USER_STATUS_CHANGED: 'Statut utilisateur',
    PERMISSION_MODIFIED: 'Modification permissions',
    COMMANDE_VENTE_VALIDEE: 'Validation commande vente',
    COMMANDE_ACHAT_VALIDEE: 'Validation commande achat',
    STOCK_MOUVEMENT: 'Mouvement de stock',
  };

  constructor(private auditLogService: AuditLogService) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadLogs(): void {
    this.isLoading = true;
    this.auditLogService.getAuditLogs()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.logs = Array.isArray(data) ? data : [];
          this.applyFilters();
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
        }
      });
  }

  applyFilters(): void {
    let result = [...this.logs];
    if (this.selectedAction) {
      result = result.filter(l => l.action === this.selectedAction);
    }
    if (this.searchText.trim()) {
      const q = this.searchText.trim().toLowerCase();
      result = result.filter(l =>
        (l.username || '').toLowerCase().includes(q) ||
        (l.details || '').toLowerCase().includes(q) ||
        (l.action || '').toLowerCase().includes(q)
      );
    }
    this.filteredLogs = result;
  }

  get uniqueActions(): string[] {
    return [...new Set(this.logs.map(l => l.action))].sort();
  }

  getActionLabel(action: string): string {
    return this.actionLabels[action] || action;
  }

  getActionBadgeClass(action: string): string {
    switch (action) {
      case 'LOGIN': return 'bg-success';
      case 'USER_CREATED': return 'bg-primary';
      case 'USER_UPDATED': return 'bg-info';
      case 'USER_DELETED': return 'bg-danger';
      case 'USER_STATUS_CHANGED': return 'bg-warning';
      case 'PERMISSION_MODIFIED': return 'bg-secondary';
      case 'COMMANDE_VENTE_VALIDEE':
      case 'COMMANDE_ACHAT_VALIDEE': return 'bg-success';
      case 'STOCK_MOUVEMENT': return 'bg-info';
      default: return 'bg-dark';
    }
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
  }
}
