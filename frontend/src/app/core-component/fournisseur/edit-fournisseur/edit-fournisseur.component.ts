import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, NgForOf } from '@angular/common';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { CategorieFournisseur } from '../../../model/categorieFournisseur.model';

@Component({
  selector: 'app-edit-fournisseur',
  standalone: true,
  imports: [CommonModule, NgForOf, ReactiveFormsModule, RouterLink],
  templateUrl: './edit-fournisseur.component.html',
  styleUrl: './edit-fournisseur.component.scss'
})
export class EditFournisseurComponent implements OnInit {
  fournisseurForm!: FormGroup;
  categories: CategorieFournisseur[] = [];
  isLoading = false;
  submitted = false;
  errorMessage = '';
  fournisseurId!: number;

  constructor(
    private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.fournisseurId = Number(this.route.snapshot.paramMap.get('id'));
    this.initializeForm();
    this.loadCategories();
    if (this.fournisseurId) {
      this.loadFournisseur();
    } else {
      this.errorMessage = 'ID fournisseur invalide';
    }
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

  loadFournisseur(): void {
    this.isLoading = true;
    this.fournisseurService.getFournisseurById(this.fournisseurId).subscribe({
      next: (data) => {
        this.fournisseurForm.patchValue({
          nom: data.nom,
          email: data.email,
          telephone: data.telephone,
          adresse: data.adresse,
          categorieId: data.categorieFournisseur?.id || '',
        });
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement du fournisseur:', err);
        this.errorMessage = 'Erreur lors du chargement du fournisseur';
        this.isLoading = false;
      }
    });
  }

  loadCategories(): void {
    this.fournisseurService.getAllCategorieFournisseurs().subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des catégories:', err);
      }
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

      const fournisseurData: any = {
        nom: formValue.nom.trim(),
        email: formValue.email.trim(),
        telephone: formValue.telephone.trim(),
        adresse: formValue.adresse.trim(),
        categorieFournisseur: { id: formValue.categorieId }
      };

      this.fournisseurService.updateFournisseur(this.fournisseurId, fournisseurData).subscribe({
        next: (data) => {
          console.log('Fournisseur mis à jour avec succès:', data);
          this.isLoading = false;
          alert('✓ Fournisseur mis à jour avec succès!');
          this.router.navigate(['/fournisseur/listFournisseur']);
        },
        error: (err: any) => {
          this.isLoading = false;
          console.error('Erreur lors de la mise à jour:', err);
          const errorMsg = err.error?.message || err.message || 'Impossible de mettre à jour le fournisseur';
          alert('✗ Erreur: ' + errorMsg);
        }
      });
    } else {
      alert('⚠ Veuillez remplir correctement tous les champs');
    }
  }
}
