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
  }

  initializeForm(): void {
    this.articleForm = this.fb.group({
      entrepriseId: [null, [Validators.required]],
      reference: ['', [Validators.required, Validators.minLength(2)]],
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      prixUnitaire: [null, [Validators.required, Validators.min(0)]],
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
