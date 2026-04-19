import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClientService } from '../../../core/service/client/client.service';
import { Client } from '../../../model/client.model';
import { CategorieClient } from '../../../model/categorieClient.model';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-edit-client',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-client.component.html',
  styleUrl: './edit-client.component.scss'
})
export class EditClientComponent implements OnInit {
  clientForm!: FormGroup;
  categories: CategorieClient[] = [];
  clientId: number | null = null;
  isLoading = false;

  constructor(private fb: FormBuilder,
    private clientService: ClientService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.clientId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCategories();
    this.initializeForm();
    
    if (this.clientId) {
      this.loadClient();
    }
  }

  initializeForm(): void {
    this.clientForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      telephone: ['', [Validators.required, Validators.pattern(/^\d{8,}$/)]],
      email: ['', [Validators.required, Validators.email]],
      adresse: ['', [Validators.required, Validators.minLength(5)]],
      secteurActivite: ['', [Validators.required, Validators.minLength(3)]],
      categorieId: ['', Validators.required]
    });
  }

  loadCategories(): void {
    this.clientService.getAllCategorieClients().subscribe({
      next: (data: CategorieClient[]) => {
        this.categories = data;
      },
      error: (err: any) => {
        console.error('Error loading categories:', err);
      }
    });
  }

  loadClient(): void {
    if (!this.clientId) return;

    this.isLoading = true;
    this.clientService.getClientById(this.clientId).subscribe({
      next: (client: Client) => {
        this.clientForm.patchValue({
          nom: client.nom || '',
          prenom: client.prenom || '',
          telephone: client.telephone || '',
          email: client.email || '',
          adresse: client.adresse || '',
          secteurActivite: client.secteurActivite || '',
          categorieId: (client as any).categorieClient?.id || ''
        });
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading client:', err);
        this.notification.error('Erreur lors du chargement du client');
        this.isLoading = false;
        this.router.navigate(['/client/listClient']);
      }
    });
  }

  isFieldInvalid(field: string): boolean {
    const formField = this.clientForm.get(field);
    return !!(formField && formField.invalid && (formField.dirty || formField.touched));
  }

  onSubmit(): void {
    if (!this.clientForm.valid) {
      this.notification.warning('Veuillez remplir correctement tous les champs');
      return;
    }

    this.isLoading = true;
    const formData = this.clientForm.value;
    const idCategorie = formData.categorieId;

    if (this.clientId) {
      // UPDATE
      this.clientService.updateClient(this.clientId, formData).subscribe({
        next: (response: any) => {
          this.notification.success('✓ Client modifié avec succès');
          this.router.navigate(['/client/listClient']);
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error:', err);
          this.notification.error('Erreur lors de la modification');
          this.isLoading = false;
        }
      });
    } else {
      // ADD NEW
      this.clientService.addClient(formData, idCategorie).subscribe({
        next: (response: any) => {
          this.notification.success('✓ Client ajouté avec succès');
          this.router.navigate(['/client/listClient']);
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error:', err);
          this.notification.error('Erreur lors de l\'ajout');
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/client/listClient']);
  }
}