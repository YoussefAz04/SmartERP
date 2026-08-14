import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuditLog, AuditLogService } from '../../core/service/audit-log/audit-log.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './audit-log.component.html',
  styleUrl: './audit-log.component.scss',
})
export class AuditLogComponent implements OnInit, OnDestroy {
  logs: AuditLog[] = [];
  filteredLogs: AuditLog[] = [];
  isLoading = true;
  searchText = '';
  selectedAction = '';
  filterDateFrom = '';
  filterDateTo = '';
  private destroy$ = new Subject<void>();

  constructor(
    private auditLogService: AuditLogService,
    private translate: TranslateService
  ) {}

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
    if (this.filterDateFrom || this.filterDateTo) {
      const fromMs = this.filterDateFrom
        ? (() => { const d = new Date(this.filterDateFrom); d.setHours(0, 0, 0, 0); return d.getTime(); })()
        : null;
      const toMs = this.filterDateTo
        ? (() => { const d = new Date(this.filterDateTo); d.setHours(23, 59, 59, 999); return d.getTime(); })()
        : null;
      result = result.filter(l => {
        if (!l.createdAt) return false;
        const ts = Date.parse(l.createdAt);
        if (fromMs !== null && ts < fromMs) return false;
        if (toMs !== null && ts > toMs) return false;
        return true;
      });
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

  get hasActiveFilters(): boolean {
    return !!(this.searchText || this.selectedAction || this.filterDateFrom || this.filterDateTo);
  }

  resetFilters(): void {
    this.searchText = '';
    this.selectedAction = '';
    this.filterDateFrom = '';
    this.filterDateTo = '';
    this.applyFilters();
  }

  get uniqueActions(): string[] {
    return [...new Set(this.logs.map(l => l.action))].sort();
  }

  getActionLabelKey(action: string): string {
    return `auditLog.actions.${action}`;
  }

  getActionBadgeClass(action: string): string {
    switch (action) {
      case 'LOGIN':                    return 'al-badge-login';
      case 'USER_CREATED':             return 'al-badge-created';
      case 'USER_UPDATED':             return 'al-badge-updated';
      case 'USER_DELETED':             return 'al-badge-deleted';
      case 'USER_STATUS_CHANGED':      return 'al-badge-status';
      case 'PERMISSION_MODIFIED':      return 'al-badge-permission';
      case 'COMMANDE_VENTE_CREEE':     return 'al-badge-commande';
      case 'COMMANDE_VENTE_VALIDEE':   return 'al-badge-commande';
      case 'COMMANDE_VENTE_MODIFIEE':  return 'al-badge-updated';
      case 'COMMANDE_VENTE_SUPPRIMEE': return 'al-badge-deleted';
      case 'COMMANDE_VENTE_ANNULEE':   return 'al-badge-deleted';
      case 'COMMANDE_VENTE_LIVREE':    return 'al-badge-commande';
      case 'COMMANDE_ACHAT_CREEE':     return 'al-badge-commande';
      case 'COMMANDE_ACHAT_VALIDEE':   return 'al-badge-commande';
      case 'COMMANDE_ACHAT_MODIFIEE':  return 'al-badge-updated';
      case 'COMMANDE_ACHAT_SUPPRIMEE': return 'al-badge-deleted';
      case 'COMMANDE_ACHAT_ANNULEE':   return 'al-badge-deleted';
      case 'COMMANDE_ACHAT_RECUE':     return 'al-badge-commande';
      case 'STOCK_MOUVEMENT':          return 'al-badge-stock';
      case 'STOCK_CREE':               return 'al-badge-stock';
      case 'STOCK_MODIFIE':            return 'al-badge-stock';
      case 'STOCK_SUPPRIME':           return 'al-badge-deleted';
      case 'ARTICLE_CREATED':          return 'al-badge-created';
      case 'ARTICLE_UPDATED':          return 'al-badge-updated';
      case 'ARTICLE_DELETED':          return 'al-badge-deleted';
      case 'CLIENT_CREATED':           return 'al-badge-created';
      case 'CLIENT_UPDATED':           return 'al-badge-updated';
      case 'CLIENT_DELETED':           return 'al-badge-deleted';
      case 'FOURNISSEUR_CREATED':      return 'al-badge-created';
      case 'FOURNISSEUR_UPDATED':      return 'al-badge-updated';
      case 'FOURNISSEUR_DELETED':      return 'al-badge-deleted';
      case 'FACTURE_VENTE_CREATED':    return 'al-badge-created';
      case 'FACTURE_VENTE_UPDATED':    return 'al-badge-updated';
      case 'FACTURE_VENTE_VALIDATED':  return 'al-badge-commande';
      case 'FACTURE_VENTE_CANCELLED':  return 'al-badge-deleted';
      case 'FACTURE_VENTE_DELETED':    return 'al-badge-deleted';
      case 'FACTURE_ACHAT_UPDATED':    return 'al-badge-updated';
      case 'FACTURE_ACHAT_DELETED':    return 'al-badge-deleted';
      case 'ENTREPRISE_CREEE':         return 'al-badge-created';
      case 'ENTREPRISE_MODIFIEE':      return 'al-badge-updated';
      case 'ENTREPRISE_DESACTIVEE':    return 'al-badge-deleted';
      case 'ENTREPRISE_ACTIVEE':       return 'al-badge-status';
      default:                         return 'al-badge-default';
    }
  }

  getActionIcon(action: string): string {
    switch (action) {
      case 'LOGIN':                    return 'icon-log-in';
      case 'USER_CREATED':             return 'icon-user-plus';
      case 'USER_UPDATED':             return 'icon-edit-2';
      case 'USER_DELETED':             return 'icon-user-x';
      case 'USER_STATUS_CHANGED':      return 'icon-toggle-right';
      case 'PERMISSION_MODIFIED':      return 'icon-key';
      case 'COMMANDE_VENTE_CREEE':     return 'icon-shopping-bag';
      case 'COMMANDE_VENTE_VALIDEE':   return 'icon-shopping-bag';
      case 'COMMANDE_VENTE_MODIFIEE':  return 'icon-edit-2';
      case 'COMMANDE_VENTE_SUPPRIMEE': return 'icon-trash-2';
      case 'COMMANDE_VENTE_ANNULEE':   return 'icon-x-circle';
      case 'COMMANDE_VENTE_LIVREE':    return 'icon-truck';
      case 'COMMANDE_ACHAT_CREEE':     return 'icon-package';
      case 'COMMANDE_ACHAT_VALIDEE':   return 'icon-package';
      case 'COMMANDE_ACHAT_MODIFIEE':  return 'icon-edit-2';
      case 'COMMANDE_ACHAT_SUPPRIMEE': return 'icon-trash-2';
      case 'COMMANDE_ACHAT_ANNULEE':   return 'icon-x-circle';
      case 'COMMANDE_ACHAT_RECUE':     return 'icon-inbox';
      case 'STOCK_MOUVEMENT':          return 'icon-layers';
      case 'STOCK_CREE':               return 'icon-plus-square';
      case 'STOCK_MODIFIE':            return 'icon-layers';
      case 'STOCK_SUPPRIME':           return 'icon-trash-2';
      case 'ARTICLE_CREATED':          return 'icon-plus-circle';
      case 'ARTICLE_UPDATED':          return 'icon-edit-2';
      case 'ARTICLE_DELETED':          return 'icon-trash-2';
      case 'CLIENT_CREATED':           return 'icon-user-plus';
      case 'CLIENT_UPDATED':           return 'icon-edit-2';
      case 'CLIENT_DELETED':           return 'icon-user-x';
      case 'FOURNISSEUR_CREATED':      return 'icon-user-plus';
      case 'FOURNISSEUR_UPDATED':      return 'icon-edit-2';
      case 'FOURNISSEUR_DELETED':      return 'icon-user-x';
      case 'FACTURE_VENTE_CREATED':    return 'icon-file-plus';
      case 'FACTURE_VENTE_UPDATED':    return 'icon-edit-2';
      case 'FACTURE_VENTE_VALIDATED':  return 'icon-check-circle';
      case 'FACTURE_VENTE_CANCELLED':  return 'icon-x-circle';
      case 'FACTURE_VENTE_DELETED':    return 'icon-trash-2';
      case 'FACTURE_ACHAT_UPDATED':    return 'icon-edit-2';
      case 'FACTURE_ACHAT_DELETED':    return 'icon-trash-2';
      case 'ENTREPRISE_CREEE':         return 'icon-briefcase';
      case 'ENTREPRISE_MODIFIEE':      return 'icon-edit-2';
      case 'ENTREPRISE_DESACTIVEE':    return 'icon-slash';
      case 'ENTREPRISE_ACTIVEE':       return 'icon-check-circle';
      default:                         return 'icon-activity';
    }
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const locale = this.translate.currentLang === 'fr' ? 'fr-FR' : 'en-US';
    return new Date(dateStr).toLocaleDateString(locale, {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
  }
}
