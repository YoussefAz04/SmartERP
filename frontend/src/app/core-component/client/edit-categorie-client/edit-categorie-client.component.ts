import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ClientService } from '../../../core/service/client/client.service';
import { routes } from '../../../core/helpers/routes';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-edit-categorie-client',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './edit-categorie-client.component.html',
  styleUrl: './edit-categorie-client.component.css'
})
export class EditCategorieClientComponent implements OnInit {
  categorieClientForm!: FormGroup;
  isLoading = false;
  categorieId!: number;
  protected readonly routes = routes;

  constructor(
    private clientService: ClientService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.categorieId = Number(this.route.snapshot.paramMap.get('id'));
    this.initializeForm();
    this.loadCategorie();
  }

  initializeForm(): void {
    this.categorieClientForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
    });
  }

  loadCategorie(): void {
    this.isLoading = true;
    this.clientService.getCategorieClientById(this.categorieId).subscribe({
      next: (data) => {
        this.categorieClientForm.patchValue({ nom: data.nom, description: data.description });
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement:', err);
        this.notification.error('Erreur lors du chargement de la catégorie');
        this.isLoading = false;
        this.router.navigate(['/client/listCategorieClient']);
      }
    });
  }

  updateCategorieClient(): void {
    if (!this.categorieClientForm.valid) {
      this.notification.warning('Veuillez remplir correctement tous les champs');
      return;
    }

    this.isLoading = true;
    this.clientService.updateCategorieClient(this.categorieId, this.categorieClientForm.value).subscribe({
      next: () => {
        this.notification.success('✓ Catégorie mise à jour avec succès');
        this.router.navigate(['/client/listCategorieClient']);
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Erreur:', err);
        this.notification.error('Erreur lors de la mise à jour: ' + (err.error?.message || err.message));
        this.isLoading = false;
      }
    });
  }

  cancelForm(): void {
    this.router.navigate(['/client/listCategorieClient']);
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.categorieClientForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
}
