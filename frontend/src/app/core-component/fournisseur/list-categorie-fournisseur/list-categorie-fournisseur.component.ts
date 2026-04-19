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
  styleUrl: './list-categorie-fournisseur.component.scss'
})
export class ListCategorieFournisseurComponent implements OnInit {
  categories: CategorieFournisseur[] = [];
  isLoading: boolean = false;
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
    this.loadCategories();
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
        this.notification.error('Erreur lors du chargement des catégories');
        this.isLoading = false;
      }
    });
  }

  deleteCategorie(id: number | undefined): void {
    if (!id) {
      this.notification.error('ID invalide');
      return;
    }

    this.notification.confirm('Êtes-vous sûr de vouloir supprimer cette catégorie?').then(ok => {
      if (!ok) {
        return;
      }

      this.isLoading = true;
      this.fournisseurService.deleteCategorieFournisseur(id).subscribe({
        next: (response: any) => {
          this.notification.success('✓ Catégorie supprimée avec succès');
          this.loadCategories();
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Erreur lors de la suppression:', err);
          this.notification.error('Erreur lors de la suppression');
          this.isLoading = false;
        }
      });
    });
  }
}
