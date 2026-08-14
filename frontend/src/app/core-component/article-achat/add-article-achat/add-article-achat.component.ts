import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { Fournisseur } from '../../../model/fournisseur.model';
import { NotificationService } from '../../../shared/notification/notification.service';
import { getArticleCategoryLabel } from '../../../shared/article-category-label';

@Component({
  selector: 'app-add-article-achat',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './add-article-achat.component.html',
  styleUrl: './add-article-achat.component.scss'
})
export class AddArticleAchatComponent implements OnInit {
  articleForm!: FormGroup;
  fournisseurs: Fournisseur[] = [];
  categories: any[] = [];
  units: string[] = ['Pièce', 'kg', 'Mètre', 'Ream', 'Set', 'g', 'Litre', 'Unité'];
  isLoading = false;
  submitted = false;

  private entrepriseId: number | null = null;

  private static readonly REDIRECT_DELAY_MS = 1200;

  constructor(
    private articleAchatService: ArticleAchatService,
    private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.readEntrepriseId();
    this.initializeForm();
    this.loadFournisseurs();
    this.loadCategories();
  }

  private readEntrepriseId(): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const decoded = new TextDecoder().decode(
          Uint8Array.from(atob(item), (c) => c.charCodeAt(0))
        );
        const user = JSON.parse(decoded);
        this.entrepriseId = user.entrepriseId ?? null;
      } catch (e) {
        console.error('❌ Failed to read entrepriseId from localStorage:', e);
      }
    }
    if (!this.entrepriseId) {
      const fallback = localStorage.getItem('entrepriseId') || localStorage.getItem('enterpriseId');
      if (fallback) {
        this.entrepriseId = Number(fallback);
      }
    }
  }

  initializeForm(): void {
    this.articleForm = this.fb.group({
      reference: ['', [Validators.required, Validators.minLength(2)]],
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      prixUnitaire: [null, [Validators.required, Validators.min(0)]],
      unite: ['Pièce', [Validators.required]],
      sku: ['', [Validators.required]],
      fournisseurId: [null, [Validators.required]],
      categorieArticleId: [null, [Validators.required]],
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.articleForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  getCategoryLabel(category: any): string {
    return getArticleCategoryLabel(category);
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
      error: (err) => { console.error('Error loading categories:', err); }
    });
  }

  onCancel(): void {
    this.router.navigate(['/article-achat/listArticleAchat']);
  }

  onSubmit(): void {
    this.submitted = true;

    if (!this.entrepriseId) {
      this.notify.error('Impossible de déterminer l\'entreprise. Veuillez vous reconnecter.');
      return;
    }

    if (this.articleForm.valid) {
      this.isLoading = true;
      const formValue = this.articleForm.value;
      const articleData: any = {
        reference: formValue.reference.trim(),
        nom: formValue.nom.trim(),
        description: formValue.description?.trim() || '',
        prixUnitaire: formValue.prixUnitaire,
        unite: formValue.unite,
        sku: formValue.sku?.trim() || '',
        fournisseurId: formValue.fournisseurId ? Number(formValue.fournisseurId) : null,
        categorieArticleId: formValue.categorieArticleId ? Number(formValue.categorieArticleId) : null,
        entrepriseId: this.entrepriseId,
        actif: true,
      };

      this.articleAchatService.addArticleAchat(articleData).subscribe({
        next: () => {
          this.isLoading = false;
          this.submitted = false;
          this.notify.success('Article achat ajouté avec succès !');
          this.articleForm.reset({ unite: 'Pièce' });
          setTimeout(
            () => this.router.navigate(['/article-achat/listArticleAchat']),
            AddArticleAchatComponent.REDIRECT_DELAY_MS
          );
        },
        error: (err: any) => {
          this.isLoading = false;
          this.notify.error(err.error?.message || err.message || 'Impossible d\'ajouter l\'article');
        }
      });
    } else {
      this.notify.error('Veuillez remplir correctement tous les champs obligatoires.');
    }
  }
}
