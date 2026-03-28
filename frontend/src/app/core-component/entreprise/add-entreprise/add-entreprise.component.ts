import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {Router, RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";
import {EntrepriseService} from "../../../core/service/entreprise/entreprise.service";

@Component({
  selector: 'app-add-entreprise',
  standalone: true,
  imports: [
    FormsModule,
    MatOption,
    MatSelect,
    RouterLink,
    ReactiveFormsModule
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
      nom: ['', [Validators.required, Validators.minLength(2)]],
      raisonSociale: ['', [Validators.required, Validators.minLength(2)]],
      adresse: [''],
      telephone: ['', [Validators.pattern(/^[+]?[0-9\s\-()]*$/)]],
      email: ['', [Validators.email]],
      description: [''],
      secteurActivite: [''],
      statutJuridique: [''],
      numeroIdentificationFiscale: [''],
      registreCommerce: [''],
      siteWeb: [''],
      dateCreation: ['', [Validators.required]],
      dateDerniereModification: ['']
    });
  }

  // ✅ Helper method to check if field is invalid
  isFieldInvalid(fieldName: string): boolean {
    const field = this.entrepriseFormGroup.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
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