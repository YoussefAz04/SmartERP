import {Component, OnInit} from '@angular/core';
import {FactureAchat} from "../../../model/FactureAchat.model";
import {FactureService} from "../../../core/service/facture/facture.service";
import {MatTooltip} from "@angular/material/tooltip";
import {DecimalPipe, NgForOf, NgIf, CommonModule} from "@angular/common";
import {Router, RouterLink} from "@angular/router";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import {FormsModule} from "@angular/forms";
import Swal from 'sweetalert2';
import {NotificationService} from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-listfactur-achat',
  standalone: true,
  imports: [
    MatTooltip,
    DecimalPipe,
    NgForOf,
    NgIf,
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './listfactur-achat.component.html',
  styleUrl: './listfactur-achat.component.scss'
})
export class ListfacturAchatComponent implements OnInit{
  factureAchatList : FactureAchat[] = [];
  filteredList : FactureAchat[] = [];
  permissionAchat!: PermissionDto;
  searchTerm: string = '';
  sortBy: string = 'dateAchat';
  sortOrder: 'asc' | 'desc' = 'desc';
  isLoading: boolean = false;
  deletingIds: Set<number> = new Set();
  selectedFactures: Set<number> = new Set();
  selectAll: boolean = false;

  constructor(private factureService : FactureService,
              private router : Router,
              private notify: NotificationService) {
  }

  ngOnInit() {
    this.getAllFactureAchat();
    this.getUserAuthData();
  }

  getAllFactureAchat(){
    this.isLoading = true;
    this.factureService.getAllFactureAchat().subscribe({
      next: (data) => {
        this.factureAchatList = data;
        this.applyFiltersAndSort();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.notify.error('Error while loading invoices.');
      }
    });
  }

  applyFiltersAndSort() {
    this.filteredList = this.factureAchatList.filter(f => 
      f.referance?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      f.modePaiement?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      f.statut?.toLowerCase().includes(this.searchTerm.toLowerCase())
    );

    this.filteredList.sort((a, b) => {
      let aValue: any = a[this.sortBy as keyof FactureAchat];
      let bValue: any = b[this.sortBy as keyof FactureAchat];

      if (typeof aValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = bValue.toLowerCase();
      }

      if (aValue < bValue) return this.sortOrder === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortOrder === 'asc' ? 1 : -1;
      return 0;
    });

    this.selectAll = false;
  }

  onSearch(term: string) {
    this.searchTerm = term;
    this.applyFiltersAndSort();
  }

  onSort(field: string) {
    if (this.sortBy === field) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortOrder = 'asc';
    }
    this.applyFiltersAndSort();
  }

  // ✅ NEW METHOD - Safe status class generation
  getStatusClass(statut: string | undefined): string {
    if (!statut) {
      return 'status-default';
    }
    return 'status-' + statut.toLowerCase().replace(' ', '-');
  }

  formatModePaiement(mode: string | undefined): string {
    const labels: Record<string, string> = {
      'VIREMENT': 'Virement',
      'CHEQUE': 'Chèque',
      'ESPECES': 'Espèces',
      'CARTE_BANCAIRE': 'Carte bancaire',
      'TRAITE': 'Traite',
      'A_DEFINIR': 'À définir'
    };
    return mode ? (labels[mode] ?? mode) : '—';
  }

  formatStatut(statut: string | undefined): string {
    const labels: Record<string, string> = {
      'PAYEE': 'Payée',
      'PARTIELLEMENT_PAYEE': 'Partiellement payée',
      'EN_ATTENTE': 'En attente',
      'ANNULEE': 'Canceled'
    };
    return statut ? (labels[statut] ?? statut) : '—';
  }

  toggleSelectAll(checked: boolean) {
    this.selectAll = checked;
    if (checked) {
      this.filteredList.forEach(f => f.id && this.selectedFactures.add(f.id));
    } else {
      this.selectedFactures.clear();
    }
  }

  toggleSelectFacture(id: number | undefined) {
    if (id === undefined) return;
    if (this.selectedFactures.has(id)) {
      this.selectedFactures.delete(id);
      this.selectAll = false;
    } else {
      this.selectedFactures.add(id);
      if (this.selectedFactures.size === this.filteredList.length) {
        this.selectAll = true;
      }
    }
  }

  isFactureSelected(id: number | undefined): boolean {
    return id !== undefined && this.selectedFactures.has(id);
  }

  viewFacture(id: number | undefined) {
    this.router.navigate(['facture/detaisFactureAchat', id]);
  }

  deleteFacture(id: number | undefined) {
    if (id === undefined) return;

    Swal.fire({
      title: 'Confirmer la suppression',
      text: 'Are you sure you want to delete this invoice? This action cannot be undone.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Oui, supprimer',
      cancelButtonText: 'Cancel',
      reverseButtons: true
    }).then((result) => {
      if (result.isConfirmed) {
        this.deletingIds.add(id);
        this.factureService.deleteFactureAchat(id).subscribe({
          next: () => {
            this.deletingIds.delete(id);
            this.factureAchatList = this.factureAchatList.filter(f => f.id !== id);
            this.applyFiltersAndSort();
            this.notify.success('La facture a été supprimée avec succès.');
          },
          error: () => {
            this.deletingIds.delete(id);
            this.notify.error('Error while deleting the invoice.');
          }
        });
      }
    });
  }

  exportToPdf(facture: FactureAchat) {
    this.factureService.generateFactureAchatPDF(facture);
  }

  exportMultipleToPdf() {
    if (this.selectedFactures.size === 0) {
      this.notify.info('Veuillez sélectionner au moins une facture.');
      return;
    }

    const selectedItems = Array.from(this.selectedFactures)
      .map(id => this.factureAchatList.find(f => f.id === id))
      .filter(f => f !== undefined) as FactureAchat[];

    this.factureService.generateMultipleFactureAchatPDF(selectedItems);
    this.selectedFactures.clear();
    this.selectAll = false;
  }

  getTotalAmount(): number {
    return this.filteredList.reduce((sum, f) => sum + (f.montantTTC || 0), 0);
  }

  getTotalHT(): number {
    return this.filteredList.reduce((sum, f) => sum + (f.montantHT || 0), 0);
  }

  getPendingCount(): number {
    return this.filteredList.filter(f => 
      f.statut?.toUpperCase() === 'EN_ATTENTE' || f.statut?.toUpperCase() === 'EN ATTENTE'
    ).length;
  }

  getPaidCount(): number {
    return this.filteredList.filter(f => 
      f.statut?.toUpperCase() === 'PAYÉ' || f.statut?.toUpperCase() === 'PAYE' || f.statut?.toUpperCase() === 'PAYEE' || f.statut?.toUpperCase() === 'PAID'
    ).length;
  }

  changeModePaiement(facture: FactureAchat) {
    const modeLabels: Record<string, string> = {
      'VIREMENT': 'Virement Bancaire',
      'CHEQUE': 'Chèque',
      'ESPECES': 'Espèces',
      'CARTE_BANCAIRE': 'Carte Bancaire',
      'TRAITE': 'Traite',
      'A_DEFINIR': 'À Définir'
    };

    Swal.fire({
      title: 'Changer le mode de paiement',
      input: 'select',
      inputOptions: modeLabels,
      inputValue: facture.modePaiement ?? 'A_DEFINIR',
      showCancelButton: true,
      confirmButtonColor: '#2563eb',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Changer',
      cancelButtonText: 'Cancel',
      reverseButtons: true,
      inputPlaceholder: 'Sélectionner un mode de paiement'
    }).then((result) => {
      const newMode = result.value as string;
      if (result.isConfirmed && newMode && newMode !== facture.modePaiement) {
        const updatedFacture = { ...facture, modePaiement: newMode };
        this.factureService.updateFactureAchat(facture.id!, updatedFacture).subscribe({
          next: () => {
            facture.modePaiement = newMode;
            this.applyFiltersAndSort();
            this.notify.success(`Le mode de paiement a été changé en "${modeLabels[newMode] || newMode}".`);
          },
          error: () => {
            this.notify.error('Erreur lors de la mise à jour du mode de paiement.');
          }
        });
      }
    });
  }

  changeStatus(facture: FactureAchat) {
    const statusLabels: Record<string, string> = {
      'PAYEE': 'Payée',
      'PARTIELLEMENT_PAYEE': 'Partiellement Payée',
      'ANNULEE': 'Canceled',
      'EN_ATTENTE': 'En Attente'
    };

    Swal.fire({
      title: 'Changer le statut',
      input: 'select',
      inputOptions: statusLabels,
      inputValue: facture.statut ?? '',
      showCancelButton: true,
      confirmButtonColor: '#2563eb',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Changer',
      cancelButtonText: 'Cancel',
      reverseButtons: true,
      inputPlaceholder: 'Select a status'
    }).then((result) => {
      const newStatus = result.value as string;
      if (result.isConfirmed && newStatus && newStatus !== facture.statut) {
        const updatedFacture = { ...facture, statut: newStatus };
        this.factureService.updateFactureAchat(facture.id!, updatedFacture).subscribe({
          next: () => {
            facture.statut = newStatus;
            this.applyFiltersAndSort();
            this.notify.success(`Le statut a été changé en "${statusLabels[newStatus] || newStatus}".`);
          },
          error: () => {
            this.notify.error('Erreur lors de la mise à jour du statut.');
          }
        });
      }
    });
  }

  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Achat"){
          this.permissionAchat = per;
        }
      })
    }
  }
}