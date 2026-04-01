import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleVenteService } from '../../core/service/article-vente/article-vente.service';
import { ArticleVente } from '../../model/article-vente.model';

@Component({
  selector: 'app-article-vente',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './article-vente.component.html',
  styleUrl: './article-vente.component.scss'
})
export class ArticleVenteComponent implements OnInit {
  articleVenteList: ArticleVente[] = [];
  isLoading = false;

  constructor(
    private articleVenteService: ArticleVenteService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadArticles();
  }

  loadArticles(): void {
    this.isLoading = true;
    this.articleVenteService.getAllArticlesVente().subscribe({
      next: (data) => {
        this.articleVenteList = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement:', err);
        this.isLoading = false;
      }
    });
  }

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
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet article?')) return;

    this.isLoading = true;
    this.articleVenteService.deleteArticleVente(id).subscribe({
      next: () => {
        this.articleVenteList = this.articleVenteList.filter(a => a.id !== id);
        this.isLoading = false;
        alert('✓ Article supprimé avec succès');
      },
      error: (err) => {
        console.error('Erreur lors de la suppression:', err);
        alert('✗ Erreur lors de la suppression');
        this.isLoading = false;
      }
    });
  }
}
