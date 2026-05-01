import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { routes } from '../../../core/helpers/routes';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-add-categorie-fournisseur',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CommonModule
  ],
  templateUrl: './add-categorie-fournisseur.component.html',
  styleUrl: './add-categorie-fournisseur.component.css'
})
export class AddCategorieFournisseurComponent implements OnInit {
  categorieFournisseurForm!: FormGroup;
  isLoading = false;

  constructor(private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    this.categorieFournisseurForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
    });
  }

  createCategorieFournisseur(): void {
    if (!this.categorieFournisseurForm.valid) {
      this.notification.warning('Veuillez remplir correctement tous les champs');
      return;
    }

    this.isLoading = true;
    this.fournisseurService.addCategorieFournisseur(this.categorieFournisseurForm.value).subscribe({
      next: (data: any) => {
        console.log('Catégorie ajoutée avec succès', data);
        this.notification.success('✓ Catégorie ajoutée avec succès');
        this.router.navigate(['/fournisseur/listCategorieFournisseur']);
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Erreur:', err);
        this.notification.error('Erreur lors de l\'ajout: ' + (err.error?.message || err.message));
        this.isLoading = false;
      }
    });
  }

  cancelForm(): void {
    this.router.navigate(['/fournisseur/listCategorieFournisseur']);
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.categorieFournisseurForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  protected readonly routes = routes;
}
