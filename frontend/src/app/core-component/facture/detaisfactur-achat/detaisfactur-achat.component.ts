import {Component, OnInit} from '@angular/core';
import {FactureAchat} from "../../../model/FactureAchat.model";
import {FactureService} from "../../../core/service/facture/facture.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {DecimalPipe, NgIf} from "@angular/common";

@Component({
  selector: 'app-detaisfactur-achat',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    DecimalPipe
  ],
  templateUrl: './detaisfactur-achat.component.html',
  styleUrl: './detaisfactur-achat.component.scss'
})
export class DetaisfacturAchatComponent implements OnInit {
  factureDetais!: FactureAchat;
  factureId!: number;
  isSaving: boolean = false;
  isDeleting: boolean = false;
  showDeleteConfirm: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private factureService: FactureService,
              private activatedRouter: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.factureId = this.activatedRouter.snapshot.params['id'];
    this.factureService.getFactureAchatById(this.factureId).subscribe(data => {
      this.factureDetais = data;
    });
  }

  onSubmit() {
    this.isSaving = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.factureService.updateFactureAchat(this.factureId, this.factureDetais).subscribe({
      next: () => {
        this.isSaving = false;
        this.successMessage = 'Invoice updated successfully.';
        setTimeout(() => this.router.navigate(['/facture/listFactureAchat']), 1200);
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
    this.factureService.deleteFactureAchat(this.factureId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.router.navigate(['/facture/listFactureAchat']);
      },
      error: () => {
        this.isDeleting = false;
        this.errorMessage = 'Error while deleting the invoice.';
      }
    });
  }

  goBack() {
    this.router.navigate(['/facture/listFactureAchat']);
  }
}
