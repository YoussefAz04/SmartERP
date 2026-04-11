import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ClientService } from '../../../core/service/client/client.service';
import { CategorieClient } from '../../../model/categorieClient.model';
import { routes } from '../../../core/helpers/routes';

@Component({
  selector: 'app-list-categorie-client',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './list-categorie-client.component.html',
  styleUrl: './list-categorie-client.component.scss'
})
export class ListCategorieClientComponent implements OnInit {
  categories: CategorieClient[] = [];
  isLoading: boolean = false;
  protected readonly routes = routes;
  protected readonly router: Router;

  get mostUsedCategory(): string {
    const truncateLength = 10;
    const top = this.topCategory;
    if (!top || !top.clients || top.clients.length === 0) return '—';
    const name = top.nom || '';
    return name.length > truncateLength ? name.substring(0, truncateLength) + '…' : name;
  }

  get mostUsedCount(): number {
    const top = this.topCategory;
    return top?.clients?.length || 0;
  }

  private get topCategory(): CategorieClient | null {
    if (!this.categories.length) return null;
    return [...this.categories].sort(
      (a, b) => (b.clients?.length || 0) - (a.clients?.length || 0)
    )[0];
  }

  constructor(
    private clientService: ClientService,
    router: Router
  ) {
    this.router = router;
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.clientService.getAllCategorieClients().subscribe({
      next: (data: CategorieClient[]) => {
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

  editCategorie(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/client/editCategorieClient', id]);
    } else {
      alert('ID invalide');
    }
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
    this.clientService.deleteCategorie(id).subscribe({
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