import { CommonModule, NgClass, NgForOf } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PaginatorModule } from 'primeng/paginator';
import { routes } from '../../../core/helpers/routes';
import { ClientService } from '../../../core/service/client/client.service';
import { CategorieClient } from '../../../model/categorieClient.model';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-add-client',
  standalone: true,
  imports: [
    NgForOf,
    NgClass,
    CommonModule,
    PaginatorModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './add-client.component.html',
  styleUrl: './add-client.component.css'
})
export class AddClientComponent implements OnInit{
  clientForm!: FormGroup;
  categories: CategorieClient[] = [];
  isLoading = false;
  submitted = false;

  constructor(private clientService: ClientService,
    private fb: FormBuilder,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.loadCategorie();
    this.initializeForm();
  }

  initializeForm(): void {
    this.clientForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\+?[\d\s\-]{8,}$/)]],
      adresse: ['', [Validators.required, Validators.minLength(5)]],
      secteurActivite: ['', [Validators.required, Validators.minLength(3)]],
      categorieId: ['', Validators.required],
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.clientForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  onCancel(): void {
    this.router.navigate(['/client/listClient']);
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.clientForm.valid) {
      this.isLoading = true;
      const formValue = this.clientForm.value;
      const idCategorie = formValue.categorieId;

      const clientData = {
        nom: formValue.nom.trim(),
        prenom: formValue.prenom.trim(),
        email: formValue.email.trim(),
        telephone: formValue.telephone.trim(),
        adresse: formValue.adresse.trim(),
        secteurActivite: formValue.secteurActivite.trim()
      };

      this.clientService.addClient(clientData, idCategorie).subscribe({
        next: (data) => {
          console.log('Client ajouté avec succès:', data);
          this.isLoading = false;
          this.notification.success('✓ Client ajouté avec succès!');
          this.router.navigate(['/client/listClient']);
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading = false;
          console.error('Erreur:', err);
          const errorMsg = err.error?.message || err.message || 'Impossible d\'ajouter le client';
          this.notification.error('✗ Erreur: ' + errorMsg);
        }
      });
    } else {
      this.notification.warning('⚠ Veuillez remplir correctement tous les champs');
    }
  }

  loadCategorie(): void {
    this.clientService.getAllCategorieClients().subscribe({
      next: (data) => {
        this.categories = data;
        console.log('Catégories chargées:', data);
      },
      error: (err: HttpErrorResponse) => {
        console.error('Erreur:', err);
      }
    });
  }

  protected readonly routes = routes;
}
