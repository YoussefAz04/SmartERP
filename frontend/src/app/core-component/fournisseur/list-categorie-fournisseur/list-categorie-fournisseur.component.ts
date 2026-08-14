import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { CategorieFournisseur } from '../../../model/categorieFournisseur.model';
import { routes } from '../../../core/helpers/routes';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-list-categorie-fournisseur',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './list-categorie-fournisseur.component.html',
  styleUrl: './list-categorie-fournisseur.component.css'
})
export class ListCategorieFournisseurComponent implements OnInit {
  categories: CategorieFournisseur[] = [];
  isLoading: boolean = false;
  canManageCategorie = false;
  protected readonly routes = routes;
  protected readonly router: Router;

  get mostUsedCategory(): string {
    const truncateLength = 10;
    if (!this.categories.length) return '—';
    const sorted = [...this.categories].sort(
      (a, b) => (b.fournisseurs?.length || 0) - (a.fournisseurs?.length || 0)
    );
    const top = sorted[0];
    if (!top.fournisseurs || top.fournisseurs.length === 0) return '—';
    const name = top.nom || '';
    return name.length > truncateLength ? name.substring(0, truncateLength) + '…' : name;
  }

  get mostUsedCount(): number {
    if (!this.categories.length) return 0;
    const sorted = [...this.categories].sort(
      (a, b) => (b.fournisseurs?.length || 0) - (a.fournisseurs?.length || 0)
    );
    return sorted[0]?.fournisseurs?.length || 0;
  }

  constructor(private fournisseurService: FournisseurService,
    router: Router,
    private notification: NotificationService) {
    this.router = router;
  }

  ngOnInit(): void {
    this.loadRolePermissions();
    this.loadCategories();
  }

  loadRolePermissions(): void {
    try {
      const raw = localStorage.getItem('myLSkey');
      if (!raw) return;
      const session = JSON.parse(decodeURIComponent(escape(atob(raw))));
      const roles: string[] = Array.isArray(session?.roles) ? session.roles : [];
      this.canManageCategorie = roles.includes('SUPER_ADMIN') || roles.includes('ADMIN');
    } catch {
      this.canManageCategorie = false;
    }
  }

  loadCategories(): void {
    this.isLoading = true;
    this.fournisseurService.getAllCategorieFournisseurs().subscribe({
      next: (data: CategorieFournisseur[]) => {
        this.categories = data;
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement:', err);
        this.notification.error('Error while loading categories');
        this.isLoading = false;
      }
    });
  }

  deleteCategorie(id: number | undefined): void {
    if (!id) {
      this.notification.error('ID invalide');
      return;
    }

    this.notification.confirm('Are you sure you want to delete this category?').then(ok => {
      if (!ok) {
        return;
      }

      this.isLoading = true;
      this.fournisseurService.deleteCategorieFournisseur(id).subscribe({
        next: (response: any) => {
          this.notification.success('✓ Category supprimée avec succès');
          this.loadCategories();
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error during deletion:', err);
          this.notification.error('Error during deletion');
          this.isLoading = false;
        }
      });
    });
  }
}
