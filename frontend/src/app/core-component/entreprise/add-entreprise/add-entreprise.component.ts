import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatOption } from "@angular/material/autocomplete";
import { MatSelect } from "@angular/material/select";
import { Router, RouterLink } from "@angular/router";
import { CommonModule } from "@angular/common";
import { routes } from "../../../core/helpers/routes";
import { EntrepriseService } from "../../../core/service/entreprise/entreprise.service";

@Component({
  selector: 'app-add-entreprise',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatOption,
    MatSelect,
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './add-entreprise.component.html',
  styleUrl: './add-entreprise.component.scss'
})
export class AddEntrepriseComponent implements OnInit {

  protected readonly routes = routes;

  entrepriseFormGroup!: FormGroup;
  loading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private entrepriseService: EntrepriseService,
    private router: Router,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  /**
   * Initialize the form with all fields and validators
   */
  private initializeForm(): void {
    const today = new Date().toISOString().split('T')[0];

    this.entrepriseFormGroup = this.fb.group({
      // Required fields
      nom: ['', [Validators.required, Validators.minLength(2)]],
      raisonSociale: ['', [Validators.required, Validators.minLength(2)]],
      dateCreation: [today, Validators.required],
      
      // Optional fields
      description: [''],
      secteurActivite: [''],
      statutJuridique: [''],
      numeroIdentificationFiscale: [''],
      registreCommerce: [''],
      siteWeb: [''],
      dateDerniereModification: [today],
      email: [''],
      telephone: [''],
      adresse: ['']
    });

    console.log('✅ Form initialized with today\'s date:', today);
  }

  /**
   * Create entreprise - Handle form submission
   */
  createEntreprise(): void {
    console.log('📋 Form submission started');
    
    // Mark all fields as touched to show validation errors
    this.entrepriseFormGroup.markAllAsTouched();

    // Validate form before submitting
    if (!this.entrepriseFormGroup.valid) {
      this.errorMessage = 'Veuillez remplir tous les champs requis correctement';
      console.error('❌ Form validation failed:', this.entrepriseFormGroup.errors);
      this.loading = false;
      return;
    }

    // Set loading state
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Get form data
    const formData = this.entrepriseFormGroup.value;
    console.log('📤 Submitting form data:', JSON.stringify(formData, null, 2));

    // Call service to create entreprise
    this.entrepriseService.ajouterEntreprise(formData).subscribe({
      next: (response: any) => {
        console.log('✅✅✅ SUCCESS! Entreprise créée!', response);
        console.log('Enterprise ID:', response?.id);
        
        this.successMessage = '✅ Entreprise créée avec succès! Redirection en cours...';
        this.entrepriseFormGroup.reset();
        this.initializeForm();
        
        // Redirect after 1.5 seconds
        setTimeout(() => {
          console.log('🔄 Redirecting to /entreprise');
          this.router.navigate(['/entreprise']);
        }, 1500);
      },
      error: (error: any) => {
        console.error('❌ ERROR DETAILS:');
        console.error('Full Error Object:', error);
        console.error('Status Code:', error?.status);
        console.error('Status Text:', error?.statusText);
        console.error('Error Message:', error?.message);
        console.error('Error Body:', error?.error);
        
        // Handle different error scenarios
        if (error?.status === 0) {
          this.errorMessage = '🔴 Serveur inaccessible - Vérifiez que le backend est en cours d\'exécution sur http://localhost:8020';
        } else if (error?.status === 400) {
          this.errorMessage = '⚠️ Données invalides - ' + (error?.error?.message || error?.error || 'Vérifiez vos informations');
        } else if (error?.status === 403) {
          this.errorMessage = '🔐 Accès refusé (403) - Problème de sécurité backend';
        } else if (error?.status === 404) {
          this.errorMessage = '❌ Endpoint non trouvé (404)';
        } else if (error?.status === 500) {
          this.errorMessage = '❌ Erreur serveur (500) - ' + (error?.error?.message || 'Consultez les logs du serveur');
        } else if (error?.error?.message) {
          this.errorMessage = '❌ Erreur: ' + error.error.message;
        } else if (typeof error?.error === 'string') {
          this.errorMessage = '❌ Erreur: ' + error.error;
        } else {
          this.errorMessage = '❌ Erreur inconnue - Vérifiez la console';
        }
        
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  
  /**
   * Getter for form controls - used in template for validation
   */
  get f() {
    return this.entrepriseFormGroup.controls;
  }
}