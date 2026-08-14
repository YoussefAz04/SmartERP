import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { CategorieFournisseur } from '../../../model/categorieFournisseur.model';
import { CommonModule, NgForOf } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { routes } from '../../../core/helpers/routes';
import { NotificationService } from '../../../shared/notification/notification.service';

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
  styleUrl: './add-fournisseur.component.css'
})
export class AddFournisseurComponent implements OnInit {
  fournisseurForm!: FormGroup;
  categories: CategorieFournisseur[] = [];
  isLoading = false;
  submitted = false;

  constructor(private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.loadCategories();
    this.initializeForm();
  }

  initializeForm(): void {
    this.fournisseurForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\d{8,}$/)]],
      adresse: ['', [Validators.required, Validators.minLength(5)]],
      secteurActivite: ['', [Validators.required, Validators.minLength(3)]],
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

      const fournisseurData: any = {
        nom: formValue.nom.trim(),
        prenom: formValue.prenom ? formValue.prenom.trim() : '',
        email: formValue.email.trim(),
        telephone: formValue.telephone.trim(),
        adresse: formValue.adresse.trim(),
        secteurActivite: formValue.secteurActivite.trim(),
      };

      this.fournisseurService.addFournisseur(fournisseurData, idCategorie).subscribe({
        next: () => {
          this.isLoading = false;
          this.notification.success('✓ Fournisseur ajouté avec succès!');
          this.router.navigate(['/fournisseur/listFournisseur']);
        },
        error: (err: any) => {
          this.isLoading = false;
          const errorMsg = err.error?.message || err.message || 'Impossible d\'ajouter le fournisseur';
          this.notification.error('Error: ' + errorMsg);
        }
      });
    } else {
      this.notification.warning('⚠ Veuillez remplir correctement tous les champs');
    }
  }

  loadCategories(): void {
    this.fournisseurService.getAllCategorieFournisseurs().subscribe({
      next: (data) => { this.categories = data; },
      error: () => { /* silent — categories list is empty, select will show no options */ }
    });
  }

  protected readonly routes = routes;
}
