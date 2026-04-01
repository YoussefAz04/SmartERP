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

  constructor(
    private articleVenteService: ArticleVenteService,
    private articleAchatService: ArticleAchatService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadArticlesAchat();
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
