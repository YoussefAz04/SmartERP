import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ClientService } from '../../../core/service/client/client.service';
import { CategorieClient } from '../../../model/categorieClient.model';
import { routes } from '../../../core/helpers/routes';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-list-categorie-client',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './list-categorie-client.component.html',
  styleUrl: './list-categorie-client.component.css'
})
export class ListCategorieClientComponent implements OnInit {
  categories: CategorieClient[] = [];
  isLoading: boolean = false;
  protected readonly routes = routes;
  protected readonly router: Router;

  get mostUsedCategory(): string {
    const truncateLength = 10;
    const top = this.topCategory;
    if (!top || !this.getCount(top)) return '—';
    const name = top.nom || '';
    return name.length > truncateLength ? name.substring(0, truncateLength) + '…' : name;
  }

  get mostUsedCount(): number {
    const top = this.topCategory;
    return this.getCount(top);
  }

  private getCount(cat: CategorieClient | null): number {
    if (!cat) return 0;
    return cat.clientCount ?? cat.clients?.length ?? 0;
  }

  private get topCategory(): CategorieClient | null {
    if (!this.categories.length) return null;
    return [...this.categories].sort(
      (a, b) => this.getCount(b) - this.getCount(a)
    )[0];
  }

  constructor(private clientService: ClientService,
    router: Router,
    private notification: NotificationService) {
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
        this.notification.error('Error while loading categories');
        this.isLoading = false;
      }
    });
  }

  editCategorie(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/client/editCategorieClient', id]);
    } else {
      this.notification.error('ID invalide');
    }
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
      this.clientService.deleteCategorie(id).subscribe({
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