import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { Fournisseur } from '../../../model/fournisseur.model';
import { NotificationService } from '../../../shared/notification/notification.service';
import { getArticleCategoryLabel } from '../../../shared/article-category-label';

@Component({
  selector: 'app-edit-article-achat',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './edit-article-achat.component.html',
  styleUrl: './edit-article-achat.component.scss'
})
export class EditArticleAchatComponent implements OnInit {
  articleForm!: FormGroup;
  fournisseurs: Fournisseur[] = [];
  categories: any[] = [];
  isLoading = false;
  submitted = false;
  articleId!: number;

  constructor(private articleAchatService: ArticleAchatService,
    private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private notify: NotificationService) {}

  ngOnInit(): void {
    this.articleId = Number(this.route.snapshot.paramMap.get('id'));
    this.initializeForm();
    this.loadFournisseurs();
    this.loadCategories();
    if (this.articleId) {
      this.loadArticle();
    }
  }
  initializeForm(): void {
    this.articleForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      prixUnitaire: [null, [Validators.required, Validators.min(0)]],
      quantite: [0, [Validators.required, Validators.min(0)]],
      unite: [''],
      sku: [''],
      fournisseurId: [null],
      categorieArticleId: [null],
    });
  }

  loadArticle(): void {
    this.isLoading = true;
    this.articleAchatService.getArticleAchatById(this.articleId).subscribe({
      next: (data) => {
        this.articleForm.patchValue({
          nom: data.nom,
          description: data.description || '',
          prixUnitaire: data.prixUnitaire,
          quantite: data.quantite,
          unite: data.unite || '',
          sku: data.sku || '',
          fournisseurId: data.fournisseur?.id || null,
          categorieArticleId: data.categorieArticle?.id || null,
        });
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement article:', err);
        this.isLoading = false;
      }
    });
  }

  loadFournisseurs(): void {
    this.fournisseurService.getAllFournisseurs().subscribe({
      next: (data) => { this.fournisseurs = data; },
      error: (err) => { console.error('Erreur chargement fournisseurs:', err); }
    });
  }

  loadCategories(): void {
    this.articleAchatService.getCategoriesArticle().subscribe({
      next: (data) => { this.categories = data; },
      error: (err) => { console.error('Erreur chargement catégories:', err); }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.articleForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  getCategoryLabel(category: any): string {
    return getArticleCategoryLabel(category);
  }

  onCancel(): void {
    this.router.navigate(['/article-achat/listArticleAchat']);
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.articleForm.valid) {
      this.isLoading = true;
      const formValue = this.articleForm.value;
      const articleData: any = {
        nom: formValue.nom.trim(),
        description: formValue.description?.trim() || '',
        prixUnitaire: formValue.prixUnitaire,
        quantite: formValue.quantite,
        unite: formValue.unite?.trim() || '',
        sku: formValue.sku?.trim() || '',
        fournisseurId: formValue.fournisseurId ? Number(formValue.fournisseurId) : null,
        categorieArticleId: formValue.categorieArticleId ? Number(formValue.categorieArticleId) : null,
      };

      this.articleAchatService.updateArticleAchat(this.articleId, articleData).subscribe({
        next: () => {
          this.isLoading = false;
          this.notify.success('Article mis à jour avec succès !');
          setTimeout(() => this.router.navigate(['/article-achat/listArticleAchat']), 1200);
        },
        error: (err: any) => {
          this.isLoading = false;
          const errorMsg = err.error?.message || err.message || 'Impossible de mettre à jour l\'article';
          this.notify.error('Erreur : ' + errorMsg);
        }
      });
    } else {
      this.notify.error('Veuillez remplir correctement tous les champs obligatoires.');
    }
  }
}
