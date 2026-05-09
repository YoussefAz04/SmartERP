import { Component } from '@angular/core';
import { Validators, FormBuilder, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { NgForOf, NgIf } from "@angular/common";
import { FactureDeVenteServiceService } from "../../../core/service/factureDeVente/facture-de-vente-service.service";
import {FactureVente} from "../../../model/FactureDeVente.model";

@Component({
  selector: 'app-add-facture-de-vente',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf,
    NgIf
  ],
  templateUrl: './add-facture-de-vente.component.html',
  styleUrls: ['./add-facture-de-vente.component.scss']   // ✅ corrigé
})
export class AddFactureDeVenteComponent {
  factureForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private factureService: FactureDeVenteServiceService
  ) {
    this.initializeForm();
  }

  private initializeForm() {
    this.factureForm = this.fb.group({
      dateAchat: ['', Validators.required],
      referance: ['', Validators.required],
      remise: [0, [Validators.min(0), Validators.max(100)]],
      modePaiement: [''],
      statut: ['EN_ATTENTE'],
      description: [''],
      montantHT: [{ value: 0, disabled: true }],
      montantTVA: [{ value: 0, disabled: true }],
      montantTTC: [{ value: 0, disabled: true }],
      montantTotal: [{ value: 0, disabled: true }],

      detaisFactureDeVente: this.fb.group({
        article: ['', Validators.required],
        quantite: [1, [Validators.required, Validators.min(1)]],
        prixUnitaireHT: [0, [Validators.required, Validators.min(0)]],
        tva: [0, [Validators.required, Validators.min(0)]],
        totalTTC: [{ value: 0, disabled: true }]
      })
    });

    this.factureForm.get('detaisFactureDeVente')?.valueChanges.subscribe(() => {
      this.updateTotal();
    });

    this.factureForm.get('remise')?.valueChanges.subscribe(() => {
      this.updateTotal();
    });
  }

  updateTotal() {
    const detail = this.factureForm.get('detaisFactureDeVente')!;
    const remise = this.factureForm.get('remise')?.value || 0;

    const qte = detail.get('quantite')?.value || 0;
    const prix = detail.get('prixUnitaireHT')?.value || 0;
    const tva = detail.get('tva')?.value || 0;

    const totalHT = qte * prix;
    const montantTVA = totalHT * (tva / 100);
    const totalTTC = totalHT + montantTVA;

    detail.get('totalTTC')?.setValue(parseFloat(totalTTC.toFixed(2)), { emitEvent: false });

    const montantHTGlobal = totalHT;
    const montantTVAGlobal = montantTVA;
    const montantTTCGlobal = totalTTC;

    const montantRemise = montantTTCGlobal * (remise / 100);
    const montantFinal = montantTTCGlobal - montantRemise;

    this.factureForm.get('montantHT')?.setValue(parseFloat(montantHTGlobal.toFixed(2)), { emitEvent: false });
    this.factureForm.get('montantTVA')?.setValue(parseFloat(montantTVAGlobal.toFixed(2)), { emitEvent: false });
    this.factureForm.get('montantTTC')?.setValue(parseFloat(montantTTCGlobal.toFixed(2)), { emitEvent: false });
    this.factureForm.get('montantTotal')?.setValue(parseFloat(montantFinal.toFixed(2)), { emitEvent: false });
  }

  onSubmit() {
    if (this.factureForm.valid) {
      const facture: FactureVente = this.factureForm.getRawValue();

      console.log(' Facture à envoyer:', facture);

      this.factureService.ajouterFacture(facture).subscribe({
        next: (data) => {
          console.log('Facture ajoutée avec succès:', data);
          this.resetForm();
        },
        error: (error) => {
          console.error(' Erreur lors de l\'ajout:', error);
        }
      });
    } else {
      console.warn('️ Formulaire invalide');
      this.markAllFieldsAsTouched();
    }
  }

  private markAllFieldsAsTouched() {
    Object.keys(this.factureForm.controls).forEach(key => {
      const control = this.factureForm.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        Object.keys(control.controls).forEach(nestedKey => {
          control.get(nestedKey)?.markAsTouched();
        });
      }
    });
  }

  private resetForm() {
    this.factureForm.reset();
    this.initializeForm();
  }


}
