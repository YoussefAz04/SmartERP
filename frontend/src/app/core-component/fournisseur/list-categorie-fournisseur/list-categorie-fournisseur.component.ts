import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { CategorieFournisseur } from '../../../model/categorieFournisseur.model';
import { routes } from '../../../core/helpers/routes';

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

  get withDescription(): number {
    return this.categories.filter(c => c.description && c.description.trim()).length;
  }

  get withFournisseurs(): number {
    return this.categories.filter(c => c.fournisseurs && c.fournisseurs.length > 0).length;
  }

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

  constructor(
    private fournisseurService: FournisseurService,
    router: Router
  ) {
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
        alert('Erreur lors du chargement des catégories');
        this.isLoading = false;
      }
    });
  }

  deleteCategorie(id: number | undefined): void {
    if (!id) {
      alert('ID invalide');
      return;
    }

    if (!confirm('Êtes-vous sûr de vouloir supprimer cette catégorie?')) {
      return;
    }

    this.isLoading = true;
    this.fournisseurService.deleteCategorieFournisseur(id).subscribe({
      next: (response: any) => {
        alert('✓ Catégorie supprimée avec succès');
        this.loadCategories();
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Erreur lors de la suppression:', err);
        alert('Erreur lors de la suppression');
        this.isLoading = false;
      }
    });
  }
}
