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
