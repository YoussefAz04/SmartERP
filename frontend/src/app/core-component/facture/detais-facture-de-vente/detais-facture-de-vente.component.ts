import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FactureDeVenteServiceService} from "../../../core/service/factureDeVente/facture-de-vente-service.service";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {PaginatorModule} from "primeng/paginator";
import {FactureVente} from "../../../model/FactureDeVente.model";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-detais-facture-de-vente',
  standalone: true,
  imports: [
    DecimalPipe,
    FormsModule,
    NgIf,
    PaginatorModule,
    NgForOf
  ],
  templateUrl: './detais-facture-de-vente.component.html',
  styleUrl: './detais-facture-de-vente.component.scss'
})
export class DetaisFactureDeVenteComponent implements OnInit {
  factureDetais!: FactureVente;
  factureId!: number;
  permissionVente!: PermissionDto;
  isSaving: boolean = false;
  isDeleting: boolean = false;
  showDeleteConfirm: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private factureService: FactureDeVenteServiceService,
              private activatedRouter: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.factureId = this.activatedRouter.snapshot.params['id'];
    this.factureService.getFactureById(this.factureId).subscribe(data => {
      this.factureDetais = data;
    });
    this.getUserAuthData();
  }

  onSubmit() {
    this.isSaving = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.factureService.updateFacture(this.factureId, this.factureDetais).subscribe({
      next: () => {
        this.isSaving = false;
        this.successMessage = 'Invoice updated successfully.';
        setTimeout(() => this.router.navigate(['/facture/listFactureVente']), 1200);
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Error while updating invoice.';
      }
    });
  }

  confirmDelete() {
    this.showDeleteConfirm = true;
  }

  cancelDelete() {
    this.showDeleteConfirm = false;
  }

  deleteFacture() {
    this.isDeleting = true;
    this.showDeleteConfirm = false;
    this.factureService.deleteFacture(this.factureId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.router.navigate(['/facture/listFactureVente']);
      },
      error: () => {
        this.isDeleting = false;
        this.errorMessage = 'Error while deleting the invoice.';
      }
    });
  }

  goBack() {
    this.router.navigate(['/facture/listFactureVente']);
  }

  getUserAuthData() {
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
      currentUser?.permission.map(per => {
        if (per.moduleTitle == "Vente") {
          this.permissionVente = per;
        }
      });
    }
  }
}
