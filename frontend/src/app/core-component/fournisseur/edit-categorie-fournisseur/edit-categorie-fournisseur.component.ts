import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-edit-categorie-fournisseur',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './edit-categorie-fournisseur.component.html',
  styleUrl: './edit-categorie-fournisseur.component.css'
})
export class EditCategorieFournisseurComponent implements OnInit {
  categorieForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  categorieId!: number;

  constructor(private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.categorieId = Number(this.route.snapshot.paramMap.get('id'));
    this.initializeForm();
    if (this.categorieId) {
      this.loadCategorie();
    } else {
      this.errorMessage = 'ID catégorie invalide';
    }
  }

  initializeForm(): void {
    this.categorieForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
    });
  }

  loadCategorie(): void {
    this.isLoading = true;
    this.fournisseurService.getCategorieFournisseurById(this.categorieId).subscribe({
      next: (data) => {
        this.categorieForm.patchValue({
          nom: data.nom,
          description: data.description,
        });
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement de la catégorie:', err);
        this.errorMessage = 'Erreur lors du chargement de la catégorie';
        this.isLoading = false;
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.categorieForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  onSubmit(): void {
    if (!this.categorieForm.valid) {
      this.notification.warning('Veuillez remplir correctement tous les champs');
      return;
    }

    this.isLoading = true;
    this.fournisseurService.updateCategorieFournisseur(this.categorieId, this.categorieForm.value).subscribe({
      next: (data) => {
        console.log('Catégorie mise à jour avec succès', data);
        this.notification.success('✓ Catégorie mise à jour avec succès');
        this.router.navigate(['/fournisseur/listCategorieFournisseur']);
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Erreur lors de la mise à jour:', err);
        this.notification.error('Erreur lors de la mise à jour: ' + (err.error?.message || err.message));
        this.isLoading = false;
      }
    });
  }

  cancelForm(): void {
    this.router.navigate(['/fournisseur/listCategorieFournisseur']);
  }
}
