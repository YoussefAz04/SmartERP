import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleVenteService } from '../../../core/service/article-vente/article-vente.service';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { ArticleAchat } from '../../../model/article-achat.model';

@Component({
  selector: 'app-add-article-vente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './add-article-vente.component.html',
  styleUrl: './add-article-vente.component.scss'
})
export class AddArticleVenteComponent implements OnInit {
  articleForm!: FormGroup;
  articlesAchat: ArticleAchat[] = [];
  isLoading = false;
  submitted = false;
  selectedArticleAchatPrice: number | null = null;
  autoCalculated = false;

  constructor(
    private articleVenteService: ArticleVenteService,
    private articleAchatService: ArticleAchatService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkAdminAccess();
    this.initializeForm();
    this.loadArticlesAchat();
    this.setupPriceAutoCalc();
  }

  checkAdminAccess(): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        const roles = user?.roles || [];
        const isAdmin = roles.includes('ADMIN') || roles.includes('SUPER_ADMIN');
        if (!isAdmin) {
          alert('❌ Accès refusé. Seuls les administrateurs peuvent ajouter des articles.');
          this.router.navigate(['/article-vente/listArticleVente']);
        }
      } catch (error) {
        console.error('Error checking access:', error);
        this.router.navigate(['/article-vente/listArticleVente']);
      }
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
        this.recalculatePrice();
      } else {
        this.selectedArticleAchatPrice = null;
        this.autoCalculated = false;
      }
    });

    this.articleForm.get('markup')?.valueChanges.subscribe(() => {
      this.recalculatePrice();
    });
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

  isFieldInvalid(fieldName: string): boolean {
    const field = this.articleForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  loadArticlesAchat(): void {
    this.articleAchatService.getAllArticlesAchat().subscribe({
      next: (data) => { this.articlesAchat = data; },
      error: (err) => { console.error('Erreur chargement articles achat:', err); }
    });
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
      };
      const articleAchatId = formValue.articleAchatId ? Number(formValue.articleAchatId) : undefined;

      this.articleVenteService.addArticleVente(articleData, articleAchatId).subscribe({
        next: () => {
          this.isLoading = false;
          alert('✓ Article vente ajouté avec succès!');
          this.router.navigate(['/article-vente/listArticleVente']);
        },
        error: (err: any) => {
          this.isLoading = false;
          const errorMsg = err.error?.message || err.message || 'Impossible d\'ajouter l\'article';
          alert('✗ Erreur: ' + errorMsg);
        }
      });
    } else {
      alert('⚠ Veuillez remplir correctement tous les champs obligatoires');
    }
  }
}
