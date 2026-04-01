import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleAchatService } from '../../core/service/article-achat/article-achat.service';
import { ArticleAchat } from '../../model/article-achat.model';

@Component({
  selector: 'app-article-achat',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './article-achat.component.html',
  styleUrl: './article-achat.component.scss'
})
export class ArticleAchatComponent implements OnInit {
  articleAchatList: ArticleAchat[] = [];
  isLoading = false;

  constructor(
    private articleAchatService: ArticleAchatService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadArticles();
  }

  loadArticles(): void {
    this.isLoading = true;
    this.articleAchatService.getAllArticlesAchat().subscribe({
      next: (data) => {
        this.articleAchatList = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement:', err);
        this.isLoading = false;
      }
    });
  }

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
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet article?')) return;

    this.isLoading = true;
    this.articleAchatService.deleteArticleAchat(id).subscribe({
      next: () => {
        this.articleAchatList = this.articleAchatList.filter(a => a.id !== id);
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
