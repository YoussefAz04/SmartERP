import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { EntrepriseService } from '../../../core/service/entreprise/entreprise.service';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { Fournisseur } from '../../../model/fournisseur.model';

@Component({
  selector: 'app-add-article-achat',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './add-article-achat.component.html',
  styleUrl: './add-article-achat.component.scss'
})
export class AddArticleAchatComponent implements OnInit {
  articleForm!: FormGroup;
  entreprises: any[] = [];
  fournisseurs: Fournisseur[] = [];
  categories: any[] = [];
  units: string[] = ['Pièce', 'kg', 'Mètre', 'Ream', 'Set', 'g', 'Litre', 'Unité'];
  isLoading = false;
  submitted = false;

  constructor(
    private articleAchatService: ArticleAchatService,
    private entrepriseService: EntrepriseService,
    private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadEntreprises();
    this.loadFournisseurs();
    this.loadCategories();
    this.autoSetEntrepriseId();
  }

  initializeForm(): void {
    this.articleForm = this.fb.group({
      entrepriseId: [null, [Validators.required]],
      reference: ['', [Validators.required, Validators.minLength(2)]],
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      prixUnitaire: [null, [Validators.required, Validators.min(0)]],
      quantite: [null, [Validators.required, Validators.min(0)]],
      unite: ['Pièce', [Validators.required]],
      sku: ['', [Validators.required]],
      fournisseurId: [null, [Validators.required]],
      categorieArticleId: [null, [Validators.required]],
    });
  }

  autoSetEntrepriseId(): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        const entrepriseId = user.entrepriseId;
        if (entrepriseId) {
          this.articleForm.patchValue({ entrepriseId });
        }
      } catch (e) {
        console.error('❌ Failed to read entrepriseId from localStorage:', e);
      }
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.articleForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  loadEntreprises(): void {
    this.entrepriseService.getAllEntreprises().subscribe({
      next: (data) => { this.entreprises = data; },
      error: (err) => { console.error('Erreur chargement entreprises:', err); }
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

  onCancel(): void {
    this.router.navigate(['/article-achat/listArticleAchat']);
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.articleForm.valid) {
      this.isLoading = true;
      const formValue = this.articleForm.value;
      const articleData: any = {
        reference: formValue.reference.trim(),
        nom: formValue.nom.trim(),
        description: formValue.description?.trim() || '',
        prixUnitaire: formValue.prixUnitaire,
        quantite: formValue.quantite,
        unite: formValue.unite,
        sku: formValue.sku?.trim() || '',
        fournisseurId: formValue.fournisseurId ? Number(formValue.fournisseurId) : null,
        categorieArticleId: formValue.categorieArticleId ? Number(formValue.categorieArticleId) : null,
        entrepriseId: Number(formValue.entrepriseId),
        actif: true,
      };

      this.articleAchatService.addArticleAchat(articleData).subscribe({
        next: () => {
          this.isLoading = false;
          this.submitted = false;
          this.articleForm.reset();
          alert('✓ Article achat ajouté avec succès!');
          this.router.navigate(['/article-achat/listArticleAchat']);
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
