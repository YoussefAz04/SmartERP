import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { EntrepriseService } from '../../../core/service/entreprise/entreprise.service';

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
  categories: any[] = [];
  mesures: any[] = [];
  isLoading = false;
  submitted = false;

  constructor(
    private articleAchatService: ArticleAchatService,
    private entrepriseService: EntrepriseService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadEntreprises();
    this.loadCategories();
    this.loadMesures();
  }

  initializeForm(): void {
    this.articleForm = this.fb.group({
      entrepriseId: [null, [Validators.required]],
      reference: ['', [Validators.required, Validators.minLength(2)]],
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      prixUnitaire: [null, [Validators.required, Validators.min(0)]],
      prixVente: [null, [Validators.required, Validators.min(0)]],
      categorieArticleId: [null],
      mesureId: [null],
    });
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

  loadCategories(): void {
    this.articleAchatService.getAllCategories().subscribe({
      next: (data) => { this.categories = data; },
      error: (err) => { console.error('Erreur chargement catégories:', err); }
    });
  }

  loadMesures(): void {
    this.articleAchatService.getAllMesures().subscribe({
      next: (data) => { this.mesures = data; },
      error: (err) => { console.error('Erreur chargement mesures:', err); }
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
        prixVente: formValue.prixVente,
        entrepriseId: Number(formValue.entrepriseId),
        categorieArticleId: formValue.categorieArticleId ? Number(formValue.categorieArticleId) : null,
        mesureId: formValue.mesureId ? Number(formValue.mesureId) : null,
        actif: true,
      };

      this.articleAchatService.addArticleAchat(articleData).subscribe({
        next: () => {
          this.isLoading = false;
          alert('✓ Article achat ajouté avec succès!');
          this.articleForm.reset();
          this.submitted = false;
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
