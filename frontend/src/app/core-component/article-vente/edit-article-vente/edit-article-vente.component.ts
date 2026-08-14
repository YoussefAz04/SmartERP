import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleVenteService } from '../../../core/service/article-vente/article-vente.service';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { ArticleAchat } from '../../../model/article-achat.model';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-edit-article-vente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './edit-article-vente.component.html',
  styleUrl: './edit-article-vente.component.scss'
})
export class EditArticleVenteComponent implements OnInit {
  articleForm!: FormGroup;
  articlesAchat: ArticleAchat[] = [];
  isLoading = false;
  submitted = false;
  articleId!: number;
  selectedArticleAchatPrice: number | null = null;
  selectedArticleAchatCategorie: string | null = null;
  autoCalculated = false;

  constructor(private articleVenteService: ArticleVenteService,
    private articleAchatService: ArticleAchatService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private notify: NotificationService) {}

  ngOnInit(): void {
    this.articleId = Number(this.route.snapshot.paramMap.get('id'));
    this.initializeForm();
    this.loadArticlesAchat();
    this.setupPriceAutoCalc();
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
      markup: [null, [Validators.min(0)]],
      articleAchatId: [null],
    });
  }

  setupPriceAutoCalc(): void {
    this.articleForm.get('articleAchatId')?.valueChanges.subscribe(id => {
      if (id) {
        const found = this.articlesAchat.find(a => a.id === Number(id));
        this.selectedArticleAchatPrice = found?.prixUnitaire ?? null;
        this.selectedArticleAchatCategorie = this.extractCategorieLabel(found);
        this.recalculatePrice();
      } else {
        this.selectedArticleAchatPrice = null;
        this.selectedArticleAchatCategorie = null;
        this.autoCalculated = false;
      }
    });

    this.articleForm.get('markup')?.valueChanges.subscribe(() => {
      this.recalculatePrice();
    });
  }

  private extractCategorieLabel(article: ArticleAchat | undefined): string | null {
    if (!article) return null;
    const cat: any = (article as any).categorieArticle;
    if (!cat) return null;
    return cat.libelle || cat.nom || cat.code || null;
  }

  recalculatePrice(): void {
    const markup = this.articleForm.get('markup')?.value;
    if (this.selectedArticleAchatPrice != null && markup != null && markup >= 0) {
      const calculatedPrice = this.selectedArticleAchatPrice * (1 + markup / 100);
      this.articleForm.get('prixUnitaire')?.setValue(
        Math.round(calculatedPrice * 100) / 100,
        { emitEvent: false }
      );
      this.autoCalculated = true;
    } else {
      this.autoCalculated = false;
    }
  }

  loadArticle(): void {
    this.isLoading = true;
    this.articleVenteService.getArticleVenteById(this.articleId).subscribe({
      next: (data) => {
        this.articleForm.patchValue({
          nom: data.nom,
          description: data.description || '',
          prixUnitaire: data.prixUnitaire,
          quantite: data.quantite,
          unite: data.unite || '',
          sku: data.sku || '',
          markup: data.markup,
          articleAchatId: data.articleAchat?.id || null,
        });
        if (data.articleAchat?.prixUnitaire) {
          this.selectedArticleAchatPrice = data.articleAchat.prixUnitaire;
        }
        this.selectedArticleAchatCategorie = this.extractCategorieLabel(data.articleAchat as ArticleAchat | undefined);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement article:', err);
        this.isLoading = false;
      }
    });
  }

  loadArticlesAchat(): void {
    this.articleAchatService.getAllArticlesAchat().subscribe({
      next: (data) => { this.articlesAchat = data; },
      error: (err) => { console.error('Erreur chargement articles achat:', err); }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.articleForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  onCancel(): void {
    this.router.navigate(['/article-vente/listArticleVente']);
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
        markup: formValue.markup,
        articleAchat: formValue.articleAchatId ? { id: formValue.articleAchatId } : null,
      };

      this.articleVenteService.updateArticleVente(this.articleId, articleData).subscribe({
        next: () => {
          this.isLoading = false;
          this.notify.success('Product updated successfully!');
          setTimeout(() => this.router.navigate(['/article-vente/listArticleVente']), 1200);
        },
        error: (err: any) => {
          this.isLoading = false;
          const errorMsg = err.error?.message || err.message || 'Impossible de mettre à jour l\'article';
          this.notify.error('Error: ' + errorMsg);
        }
      });
    } else {
      this.notify.error('Veuillez remplir correctement tous les champs obligatoires.');
    }
  }
}
