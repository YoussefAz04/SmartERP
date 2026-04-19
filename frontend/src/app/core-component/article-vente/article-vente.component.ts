import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ArticleVenteService } from '../../core/service/article-vente/article-vente.service';
import { ArticleVente } from '../../model/article-vente.model';
import { ResponseDto } from '../../model/reponseDto.model';
import { NotificationService } from '../../shared/notification/notification.service';

@Component({
  selector: 'app-article-vente',
  standalone: true,
  imports: [CommonModule, RouterOutlet, FormsModule],
  templateUrl: './article-vente.component.html',
  styleUrl: './article-vente.component.scss'
})
export class ArticleVenteComponent implements OnInit {
  articleVenteList: ArticleVente[] = [];
  filteredList: ArticleVente[] = [];
  isLoading = false;

  canCreateArticle = false;
  canEditArticle = false;
  canDeleteArticle = false;

  // Search & filter
  searchTerm = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  minMarkup: number | null = null;
  maxMarkup: number | null = null;
  sortField: keyof ArticleVente | '' = '';
  sortAsc = true;

  // Bulk operations
  selectedIds: Set<number> = new Set();
  allSelected = false;

  constructor(private articleVenteService: ArticleVenteService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.loadRolePermissions();
    this.loadArticles();
  }

  loadRolePermissions(): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));
        const roles: string[] = Array.isArray(currentUser?.roles) ? currentUser.roles : [];
        const isSuperAdmin = roles.includes('SUPER_ADMIN');
        const isAdmin = roles.includes('ADMIN');
        const isEmployee = roles.includes('EMPLOYEE');
        const canByRole = isSuperAdmin || isAdmin;

        // EMPLOYEE always has read-only access regardless of DB permissions
        if (isEmployee && !canByRole) {
          this.canCreateArticle = false;
          this.canEditArticle = false;
          this.canDeleteArticle = false;
          this.cdr.detectChanges();
          return;
        }

        let ventePermissionFound = false;
        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          const ventePerm = currentUser.permission.find((p: any) =>
            p.moduleTitle?.toLowerCase().includes('vente')
          );
          if (ventePerm) {
            ventePermissionFound = true;
            this.canCreateArticle = ventePerm.create === true;
            this.canEditArticle = ventePerm.update === true;
            this.canDeleteArticle = ventePerm.delete === true;
          }
        }

        // Fallback: if no DB permission record found, derive access from role
        if (!ventePermissionFound) {
          this.canCreateArticle = canByRole;
          this.canEditArticle = canByRole;
          this.canDeleteArticle = canByRole;
        }

        console.log('canCreateArticle (vente):', this.canCreateArticle, 'canEditArticle:', this.canEditArticle, 'canDeleteArticle:', this.canDeleteArticle);
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
    this.cdr.detectChanges();
  }

  loadArticles(): void {
    this.isLoading = true;
    this.articleVenteService.getAllArticlesVente().subscribe({
      next: (data) => {
        this.articleVenteList = data;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement:', err);
        this.isLoading = false;
      }
    });
  }

  // --- Stats ---
  get totalCount(): number {
    return this.articleVenteList.length;
  }

  get avgPrice(): number {
    if (!this.articleVenteList.length) return 0;
    const sum = this.articleVenteList.reduce((acc, a) => acc + (a.prixUnitaire || 0), 0);
    return sum / this.articleVenteList.length;
  }

  get avgMarkup(): number {
    const withMarkup = this.articleVenteList.filter(a => a.markup != null);
    if (!withMarkup.length) return 0;
    const sum = withMarkup.reduce((acc, a) => acc + (a.markup || 0), 0);
    return sum / withMarkup.length;
  }

  get totalValue(): number {
    return this.articleVenteList.reduce((acc, a) => acc + (a.prixUnitaire || 0) * (a.quantite || 0), 0);
  }

  // --- Filtering ---
  applyFilters(): void {
    let list = [...this.articleVenteList];

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.trim().toLowerCase();
      list = list.filter(a =>
        a.nom.toLowerCase().includes(term) ||
        (a.sku || '').toLowerCase().includes(term) ||
        (a.description || '').toLowerCase().includes(term) ||
        (a.articleAchat?.nom || '').toLowerCase().includes(term)
      );
    }

    if (this.minPrice !== null) {
      list = list.filter(a => (a.prixUnitaire || 0) >= this.minPrice!);
    }
    if (this.maxPrice !== null) {
      list = list.filter(a => (a.prixUnitaire || 0) <= this.maxPrice!);
    }
    if (this.minMarkup !== null) {
      list = list.filter(a => (a.markup || 0) >= this.minMarkup!);
    }
    if (this.maxMarkup !== null) {
      list = list.filter(a => (a.markup || 0) <= this.maxMarkup!);
    }

    if (this.sortField) {
      const field = this.sortField;
      list.sort((a, b) => {
        const va = a[field] ?? '';
        const vb = b[field] ?? '';
        const sa = typeof va === 'object' ? '' : String(va);
        const sb = typeof vb === 'object' ? '' : String(vb);
        const na = Number(va);
        const nb = Number(vb);
        if (!isNaN(na) && !isNaN(nb)) {
          return this.sortAsc ? na - nb : nb - na;
        }
        return this.sortAsc ? sa.localeCompare(sb) : sb.localeCompare(sa);
      });
    }

    this.filteredList = list;
    this.allSelected = this.filteredList.length > 0 &&
      this.filteredList.every(a => a.id !== undefined && this.selectedIds.has(a.id));
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.minPrice = null;
    this.maxPrice = null;
    this.minMarkup = null;
    this.maxMarkup = null;
    this.sortField = '';
    this.sortAsc = true;
    this.applyFilters();
  }

  setSort(field: keyof ArticleVente): void {
    if (this.sortField === field) {
      this.sortAsc = !this.sortAsc;
    } else {
      this.sortField = field;
      this.sortAsc = true;
    }
    this.applyFilters();
  }

  // --- Bulk selection ---
  toggleSelectAll(): void {
    this.allSelected = !this.allSelected;
    if (this.allSelected) {
      this.filteredList.forEach(a => { if (a.id !== undefined) this.selectedIds.add(a.id); });
    } else {
      this.selectedIds.clear();
    }
  }

  toggleSelect(id: number | undefined): void {
    if (id === undefined) return;
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
    this.allSelected = this.filteredList.length > 0 &&
      this.filteredList.every(a => a.id !== undefined && this.selectedIds.has(a.id));
  }

  isSelected(id: number | undefined): boolean {
    return id !== undefined && this.selectedIds.has(id);
  }

  bulkDelete(): void {
    if (!this.selectedIds.size) return;
    this.notification.confirm(`Êtes-vous sûr de vouloir supprimer ${this.selectedIds.size} article(s)?`).then(ok => {
      if (!ok) return;

      const ids = [...this.selectedIds];
      let completed = 0;
      this.isLoading = true;
      ids.forEach(id => {
        this.articleVenteService.deleteArticleVente(id).subscribe({
          next: () => {
            this.articleVenteList = this.articleVenteList.filter(a => a.id !== id);
            this.selectedIds.delete(id);
            completed++;
            if (completed === ids.length) {
              this.applyFilters();
              this.isLoading = false;
              this.notification.success(`✓ ${ids.length} article(s) supprimé(s) avec succès`);
            }
          },
          error: (err) => {
            console.error('Erreur lors de la suppression:', err);
            completed++;
            if (completed === ids.length) {
              this.applyFilters();
              this.isLoading = false;
            }
          }
        });
      });
    });
  }

  // --- Navigation ---
  addArticle(): void {
    this.router.navigate(['/article-vente/add']);
  }

  editArticle(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/article-vente/edit', id]);
    }
  }

  deleteArticle(id: number | undefined): void {
    if (!id) return;
    this.notification.confirm('Êtes-vous sûr de vouloir supprimer cet article?').then(ok => { if (!ok) return;

    this.isLoading = true;
    this.articleVenteService.deleteArticleVente(id).subscribe({
      next: () => {
        this.articleVenteList = this.articleVenteList.filter(a => a.id !== id);
        this.applyFilters();
        this.isLoading = false;
        this.notification.success('✓ Article supprimé avec succès');
      },
      error: (err) => {
        console.error('Erreur lors de la suppression:', err);
        this.notification.error('✗ Erreur lors de la suppression');
        this.isLoading = false;
      }
      });
    });
  }

  // --- CSV Export ---
  exportCSV(): void {
    const headers = ['ID', 'Nom', 'SKU', 'Prix Unitaire', 'Quantité', 'Unité', 'Markup (%)', 'Article Achat', 'Créé le'];
    const rows = this.filteredList.map(a => [
      a.id ?? '',
      `"${a.nom}"`,
      a.sku ?? '',
      a.prixUnitaire ?? '',
      a.quantite ?? '',
      a.unite ?? '',
      a.markup ?? '',
      a.articleAchat?.nom ?? '',
      a.createdAt ? new Date(a.createdAt).toLocaleDateString('fr-FR') : ''
    ]);

    const csvContent = [headers, ...rows].map(r => r.join(';')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', 'articles_vente.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}
