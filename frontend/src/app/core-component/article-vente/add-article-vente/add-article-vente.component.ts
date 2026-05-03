import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { ArticleVenteService } from '../../../core/service/article-vente/article-vente.service';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { ArticleAchat } from '../../../model/article-achat.model';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-add-article-vente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './add-article-vente.component.html',
  styleUrl: './add-article-vente.component.scss'
})
export class AddArticleVenteComponent implements OnInit, OnDestroy {
  articleForm!: FormGroup;
  articlesAchat: ArticleAchat[] = [];
  isLoading = false;
  submitted = false;
  selectedArticleAchatPrice: number | null = null;
  autoCalculated = false;

  private readonly NAVIGATION_DELAY = 1200;
  private subs = new Subscription();

  constructor(
    private articleVenteService: ArticleVenteService,
    private articleAchatService: ArticleAchatService,
    private fb: FormBuilder,
    private router: Router,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadArticlesAchat();
    this.setupPriceAutoCalc();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  initializeForm(): void {
    this.articleForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      prixUnitaire: [null, [Validators.required, Validators.min(0)]],
      unite: [''],
      sku: [''],
      markup: [null, [Validators.min(0)]],
      articleAchatId: [null],
    });
  }

  setupPriceAutoCalc(): void {
    const articleAchatSub = this.articleForm.get('articleAchatId')?.valueChanges.subscribe(id => {
      if (id) {
        const found = this.articlesAchat.find(a => a.id === Number(id));
        this.selectedArticleAchatPrice = found?.prixUnitaire ?? null;
        this.recalculatePrice();
      } else {
        this.selectedArticleAchatPrice = null;
        this.autoCalculated = false;
      }
    });

    const markupSub = this.articleForm.get('markup')?.valueChanges.subscribe(() => {
      this.recalculatePrice();
    });

    if (articleAchatSub) this.subs.add(articleAchatSub);
    if (markupSub) this.subs.add(markupSub);
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
    const sub = this.articleAchatService.getAllArticlesAchat().subscribe({
      next: (data) => { this.articlesAchat = data; },
      error: (err) => { console.error('Erreur chargement articles achat:', err); }
    });
    this.subs.add(sub);
  }

  onCancel(): void {
    this.router.navigate(['/article-vente/listArticleVente']);
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.isLoading) return;

    if (!this.articleForm.valid) {
      this.notify.warning('Veuillez remplir correctement tous les champs obligatoires.');
      return;
    }

    this.isLoading = true;
    const formValue = this.articleForm.value;
    const articleData: any = {
      nom: formValue.nom.trim(),
      description: formValue.description?.trim() || '',
      prixUnitaire: formValue.prixUnitaire,
      unite: formValue.unite?.trim() || '',
      sku: formValue.sku?.trim() || '',
      markup: formValue.markup,
    };
    const articleAchatId = formValue.articleAchatId ? Number(formValue.articleAchatId) : undefined;

    const sub = this.articleVenteService.addArticleVente(articleData, articleAchatId).subscribe({
      next: () => {
        this.isLoading = false;
        this.notify.success('Article vente ajouté avec succès !');
        setTimeout(() => this.router.navigate(['/article-vente/listArticleVente']), this.NAVIGATION_DELAY);
      },
      error: (err: any) => {
        this.isLoading = false;
        const errorMsg = err.error?.message || err.message || 'Impossible d\'ajouter l\'article';
        this.notify.error('Erreur : ' + errorMsg);
      }
    });
    this.subs.add(sub);
  }
}
