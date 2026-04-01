import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {Router, RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";
import {EntrepriseService} from "../../../core/service/entreprise/entreprise.service";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-add-entreprise',
  standalone: true,
  imports: [
    FormsModule,
    MatOption,
    MatSelect,
    RouterLink,
    ReactiveFormsModule,
    CommonModule
  ],
  templateUrl: './add-entreprise.component.html',
  styleUrl: './add-entreprise.component.scss'
})
export class AddEntrepriseComponent implements OnInit{

  protected readonly routes = routes;
  entrepriseFormGroup!: FormGroup;

  constructor(
    private entrepriseService: EntrepriseService,
    private router: Router,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    this.entrepriseFormGroup = this.fb.group({
      // ✅ REQUIRED FIELDS WITH VALIDATION
      nom: ['', [
        Validators.required, 
        Validators.minLength(2),
        Validators.maxLength(100)
      ]],
      
      raisonSociale: ['', [
        Validators.required, 
        Validators.minLength(2),
        Validators.maxLength(150)
      ]],
      
      dateCreation: ['', [Validators.required]],

      // ✅ OPTIONAL FIELDS WITH VALIDATION
      adresse: ['', [Validators.maxLength(200)]],
      
      telephone: ['', [
        Validators.pattern(/^(\+?216|0)?[1-9]\d{7,8}$/)
      ]],
      
      email: ['', [Validators.email]],
      
      description: ['', [Validators.maxLength(500)]],
      
      secteurActivite: ['', [Validators.maxLength(100)]],
      
      statutJuridique: ['', [Validators.maxLength(50)]],
      
      numeroIdentificationFiscale: ['', [
        Validators.pattern(/^[0-9]{8,15}$/)
      ]],
      
      registreCommerce: ['', [Validators.maxLength(30)]],
      
      siteWeb: ['', [Validators.pattern(/^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/)]],
      
      dateDerniereModification: ['']
    });
  }

  // ✅ Check if field is invalid and touched
  isFieldInvalid(fieldName: string): boolean {
    const field = this.entrepriseFormGroup.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  // ✅ Get error message for field
  getErrorMessage(fieldName: string): string {
    const field = this.entrepriseFormGroup.get(fieldName);
    
    if (!field || !field.errors) return '';

    // NOM
    if (fieldName === 'nom') {
      if (field.hasError('required')) return 'Le nom est requis';
      if (field.hasError('minlength')) return 'Le nom doit avoir au moins 2 caractères';
      if (field.hasError('maxlength')) return 'Le nom ne doit pas dépasser 100 caractères';
    }

    // RAISON SOCIALE
    if (fieldName === 'raisonSociale') {
      if (field.hasError('required')) return 'La raison sociale est requise';
      if (field.hasError('minlength')) return 'La raison sociale doit avoir au moins 2 caractères';
      if (field.hasError('maxlength')) return 'La raison sociale ne doit pas dépasser 150 caractères';
    }

    // TELEPHONE
    if (fieldName === 'telephone') {
      if (field.hasError('pattern')) return 'Format: +216 XXXXXXXX ou 098765432';
    }

    // EMAIL
    if (fieldName === 'email') {
      if (field.hasError('email')) return 'Veuillez entrer une adresse email valide';
    }

    // ADRESSE
    if (fieldName === 'adresse') {
      if (field.hasError('maxlength')) return 'L\'adresse ne doit pas dépasser 200 caractères';
    }

    // DESCRIPTION
    if (fieldName === 'description') {
      if (field.hasError('maxlength')) return 'La description ne doit pas dépasser 500 caractères';
    }

    // SECTEUR ACTIVITE
    if (fieldName === 'secteurActivite') {
      if (field.hasError('maxlength')) return 'Le secteur ne doit pas dépasser 100 caractères';
    }

    // STATUT JURIDIQUE
    if (fieldName === 'statutJuridique') {
      if (field.hasError('maxlength')) return 'Le statut ne doit pas dépasser 50 caractères';
    }

    // NIF
    if (fieldName === 'numeroIdentificationFiscale') {
      if (field.hasError('pattern')) return 'NIF invalide (8-15 chiffres)';
    }

    // REGISTRE COMMERCE
    if (fieldName === 'registreCommerce') {
      if (field.hasError('maxlength')) return 'Le RC ne doit pas dépasser 30 caractères';
    }

    // SITE WEB
    if (fieldName === 'siteWeb') {
      if (field.hasError('pattern')) return 'Format URL invalide (ex: https://exemple.com)';
    }

    // DATE CREATION
    if (fieldName === 'dateCreation') {
      if (field.hasError('required')) return 'La date de création est requise';
    }

    return 'Format invalide';
  }

  // ✅ Get remaining characters for textarea fields
  getRemainingChars(fieldName: string, maxLength: number): number {
    const field = this.entrepriseFormGroup.get(fieldName);
    const value = field?.value || '';
    return maxLength - value.length;
  }

  createEntreprise(): void {
    if (this.entrepriseFormGroup.valid) {
      console.log('✅ Form is valid');
      console.log('Form value:', this.entrepriseFormGroup.value);

      this.entrepriseService.ajouterEntreprise(this.entrepriseFormGroup.value).subscribe({
        next: (data) => {
          console.log("✅ Entreprise créée avec succès:", data);
          alert('Entreprise créée avec succès!');
          this.router.navigate(['/entreprise/listEntreprise']);
        },
        error: (err) => {
          console.error("❌ Erreur lors de la création:", err);
          alert('Erreur lors de la création de l\'entreprise');
        }
      });
    } else {
      console.warn('❌ Form is invalid');
      this.markFormGroupTouched(this.entrepriseFormGroup);
    }
  }

  // ✅ Mark all fields as touched to show errors
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
}