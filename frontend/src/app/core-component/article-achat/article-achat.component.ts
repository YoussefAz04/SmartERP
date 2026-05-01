import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ArticleAchatService } from '../../core/service/article-achat/article-achat.service';
import { ArticleAchat } from '../../model/article-achat.model';
import { ResponseDto } from '../../model/reponseDto.model';
import { NotificationService } from '../../shared/notification/notification.service';

@Component({
  selector: 'app-article-achat',
  standalone: true,
  imports: [CommonModule, RouterOutlet, FormsModule],
  templateUrl: './article-achat.component.html',
  styleUrl: './article-achat.component.scss'
})
export class ArticleAchatComponent implements OnInit {
  articleAchatList: ArticleAchat[] = [];
  filteredList: ArticleAchat[] = [];
  isLoading = false;

  canCreateArticle = false;
  canEditArticle = false;
  canDeleteArticle = false;

  // Search & filter
  searchTerm = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  selectedFournisseur = '';
  sortField: keyof ArticleAchat | '' = '';
  sortAsc = true;

  // Bulk operations
  selectedIds: Set<number> = new Set();
  allSelected = false;

  constructor(private articleAchatService: ArticleAchatService,
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
        const canByRole = isSuperAdmin || isAdmin;

        // Match the exact module title used by this page (accent- and case-insensitive)
        // to avoid cross-matching other "achat" permission rows (e.g. Commande Achat).
        const normalize = (s: string | undefined | null) =>
          (s ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
        const targetTitle = normalize('gestion Article Achat');

        let achatPermissionFound = false;
        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          const achatPerm = currentUser.permission.find((p: any) =>
            normalize(p.moduleTitle) === targetTitle
          );
          if (achatPerm) {
            achatPermissionFound = true;
            this.canCreateArticle = achatPerm.create === true;
            this.canEditArticle = achatPerm.update === true;
            this.canDeleteArticle = achatPerm.delete === true;
          }
        }

        // Fallback: if no DB permission record found, derive access from role
        if (!achatPermissionFound) {
          this.canCreateArticle = canByRole;
          this.canEditArticle = canByRole;
          this.canDeleteArticle = canByRole;
        }

        console.log('canCreateArticle (achat):', this.canCreateArticle, 'canEditArticle:', this.canEditArticle, 'canDeleteArticle:', this.canDeleteArticle);
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
    this.cdr.detectChanges();
  }

  loadArticles(): void {
    this.isLoading = true;
    this.articleAchatService.getAllArticlesAchat().subscribe({
      next: (data) => {
        this.articleAchatList = data;
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
    return this.articleAchatList.length;
  }

  get avgPrice(): number {
    if (!this.articleAchatList.length) return 0;
    const sum = this.articleAchatList.reduce((acc, a) => acc + (a.prixUnitaire || 0), 0);
    return sum / this.articleAchatList.length;
  }

  get totalValue(): number {
    return this.articleAchatList.reduce((acc, a) => acc + (a.prixUnitaire || 0) * (a.quantite || 0), 0);
  }

  get uniqueFournisseurs(): string[] {
    const names = this.articleAchatList
      .map(a => a.fournisseur?.nom)
      .filter((n): n is string => !!n);
    return [...new Set(names)];
  }

  // --- Filtering ---
  applyFilters(): void {
    let list = [...this.articleAchatList];

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.trim().toLowerCase();
      list = list.filter(a =>
        a.nom.toLowerCase().includes(term) ||
        (a.sku || '').toLowerCase().includes(term) ||
        (a.description || '').toLowerCase().includes(term)
      );
    }

    if (this.minPrice !== null) {
      list = list.filter(a => (a.prixUnitaire || 0) >= this.minPrice!);
    }
    if (this.maxPrice !== null) {
      list = list.filter(a => (a.prixUnitaire || 0) <= this.maxPrice!);
    }

    if (this.selectedFournisseur) {
      list = list.filter(a => a.fournisseur?.nom === this.selectedFournisseur);
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
    this.selectedFournisseur = '';
    this.sortField = '';
    this.sortAsc = true;
    this.applyFilters();
  }

  setSort(field: keyof ArticleAchat): void {
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
        this.articleAchatService.deleteArticleAchat(id).subscribe({
          next: () => {
            this.articleAchatList = this.articleAchatList.filter(a => a.id !== id);
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
            const message = err?.error && typeof err.error === 'string'
              ? err.error
              : 'Erreur lors de la suppression de l\'article ' + id;
            this.notification.error('✗ ' + message);
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
    this.router.navigate(['/article-achat/add']);
  }

  editArticle(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/article-achat/edit', id]);
    }
  }

  deleteArticle(id: number | undefined): void {
    if (!id) return;
    this.notification.confirm('Êtes-vous sûr de vouloir supprimer cet article?').then(ok => { if (!ok) return;

    this.isLoading = true;
    this.articleAchatService.deleteArticleAchat(id).subscribe({
      next: () => {
        this.articleAchatList = this.articleAchatList.filter(a => a.id !== id);
        this.applyFilters();
        this.isLoading = false;
        this.notification.success('✓ Article supprimé avec succès');
      },
      error: (err) => {
        console.error('Erreur lors de la suppression:', err);
        const message = err?.error && typeof err.error === 'string'
          ? err.error
          : 'Erreur lors de la suppression de l\'article';
        this.notification.error('✗ ' + message);
        this.isLoading = false;
      }
      });
    });
  }

  // --- CSV Export ---
  exportCSV(): void {
    const headers = ['ID', 'Nom', 'SKU', 'Prix Unitaire', 'Quantité', 'Unité', 'Fournisseur', 'Catégorie', 'Créé le'];
    const rows = this.filteredList.map(a => [
      a.id ?? '',
      `"${a.nom}"`,
      a.sku ?? '',
      a.prixUnitaire ?? '',
      a.quantite ?? '',
      a.unite ?? '',
      a.fournisseur?.nom ?? '',
      a.categorieArticle?.nom ?? '',
      a.createdAt ? new Date(a.createdAt).toLocaleDateString('fr-FR') : ''
    ]);

    const csvContent = [headers, ...rows].map(r => r.join(';')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', 'articles_achat.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}
