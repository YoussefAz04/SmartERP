import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { ClientService } from "../../../core/service/client/client.service";
import { routes } from "../../../core/helpers/routes";
import { RouterLink, Router } from "@angular/router";
import { CommonModule } from "@angular/common";
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-add-categorie-client',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CommonModule
  ],
  templateUrl: './add-categorie-client.component.html',
  styleUrl: './add-categorie-client.component.css'
})
export class AddCategorieClientComponent implements OnInit {
  categorieClientForm!: FormGroup;
  isLoading = false;

  constructor(private clientService: ClientService,
    private fb: FormBuilder,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    this.categorieClientForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
    });
  }

  createCategorieClient(): void {
    if (!this.categorieClientForm.valid) {
      this.notification.warning('Veuillez remplir correctement tous les champs');
      return;
    }

    this.isLoading = true;
    this.clientService.addCategorieClient(this.categorieClientForm.value).subscribe({
      next: (data: any) => {
        console.log('Category ajoutée avec succès', data);
        this.notification.success('✓ Category ajoutée avec succès');
        this.router.navigate(['/client/listCategorieClient']);
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
    this.router.navigate(['/client/listCategorieClient']);
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.categorieClientForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  protected readonly routes = routes;
}