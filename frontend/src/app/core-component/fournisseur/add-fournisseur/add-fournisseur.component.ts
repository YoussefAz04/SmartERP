import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { CategorieFournisseur } from '../../../model/categorieFournisseur.model';
import { CommonModule, NgForOf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { routes } from '../../../core/helpers/routes';

@Component({
  selector: 'app-add-fournisseur',
  standalone: true,
  imports: [
    NgForOf,
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './add-fournisseur.component.html',
  styleUrl: './add-fournisseur.component.scss'
})
export class AddFournisseurComponent implements OnInit {
  fournisseurForm!: FormGroup;
  categories: CategorieFournisseur[] = [];
  isLoading = false;
  submitted = false;

  constructor(
    private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.initializeForm();
  }

  initializeForm(): void {
    this.fournisseurForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\d{8,}$/)]],
      adresse: ['', [Validators.required, Validators.minLength(5)]],
      categorieId: ['', Validators.required],
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.fournisseurForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  onCancel(): void {
    this.router.navigate(['/fournisseur/listFournisseur']);
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.fournisseurForm.valid) {
      this.isLoading = true;
      const formValue = this.fournisseurForm.value;
      const idCategorie = formValue.categorieId;

      const fournisseurData = {
        nom: formValue.nom.trim(),
        email: formValue.email.trim(),
        telephone: formValue.telephone.trim(),
        adresse: formValue.adresse.trim(),
      };

      this.fournisseurService.addFournisseur(fournisseurData, idCategorie).subscribe({
        next: (data) => {
          console.log('Fournisseur ajouté avec succès:', data);
          this.isLoading = false;
          alert('✓ Fournisseur ajouté avec succès!');
          this.router.navigate(['/fournisseur/listFournisseur']);
        },
        error: (err: any) => {
          this.isLoading = false;
          console.error('Erreur:', err);
          const errorMsg = err.error?.message || err.message || 'Impossible d\'ajouter le fournisseur';
          alert('✗ Erreur: ' + errorMsg);
        }
      });
    } else {
      alert('⚠ Veuillez remplir correctement tous les champs');
    }
  }

  loadCategories(): void {
    this.fournisseurService.getAllCategorieFournisseurs().subscribe({
      next: (data) => {
        this.categories = data;
        console.log('Catégories chargées:', data);
      },
      error: (err: any) => {
        console.error('Erreur:', err);
      }
    });
  }

  protected readonly routes = routes;
}
