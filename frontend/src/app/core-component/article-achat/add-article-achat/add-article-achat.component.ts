import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { Fournisseur } from '../../../model/fournisseur.model';
import { CategorieFournisseur } from '../../../model/categorieFournisseur.model';

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
  categories: CategorieFournisseur[] = [];
  isLoading = false;
  submitted = false;

  constructor(
    private articleAchatService: ArticleAchatService,
    private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadFournisseurs();
    this.loadCategories();
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
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.articleForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  loadFournisseurs(): void {
    this.fournisseurService.getAllFournisseurs().subscribe({
      next: (data) => { this.fournisseurs = data; },
      error: (err) => { console.error('Erreur chargement fournisseurs:', err); }
    });
  }

  loadCategories(): void {
    this.fournisseurService.getAllCategorieFournisseurs().subscribe({
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
        nom: formValue.nom.trim(),
        description: formValue.description?.trim() || '',
        prixUnitaire: formValue.prixUnitaire,
        quantite: formValue.quantite,
        unite: formValue.unite?.trim() || '',
        sku: formValue.sku?.trim() || '',
      };
      const fournisseurId = formValue.fournisseurId ? Number(formValue.fournisseurId) : undefined;

      this.articleAchatService.addArticleAchat(articleData, fournisseurId).subscribe({
        next: () => {
          this.isLoading = false;
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
