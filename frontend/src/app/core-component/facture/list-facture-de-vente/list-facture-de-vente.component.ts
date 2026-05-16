import {Component, OnInit} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {FactureVente} from "../../../model/FactureDeVente.model";
import {FactureDeVenteServiceService} from "../../../core/service/factureDeVente/facture-de-vente-service.service";
import {CommonModule, DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {MatTooltip} from "@angular/material/tooltip";
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {SharedModule} from "../../../shared/shared.module";
import {ClientService} from "../../../core/service/client/client.service";
import {Client} from "../../../model/client.model";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import {FormsModule} from "@angular/forms";
import Swal from 'sweetalert2';
import {forkJoin} from 'rxjs';
import {NotificationService} from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-list-facture-de-vente',
  standalone: true,
  imports: [
    DecimalPipe,
    MatTooltip,
    NgForOf,
    NgIf,
    ReactiveFormsModule,
    SharedModule,
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './list-facture-de-vente.component.html',
  styleUrl: './list-facture-de-vente.component.scss'
})
export class ListFactureDeVenteComponent implements OnInit{
  factureAchatList : FactureVente[] = [];
  filteredList: FactureVente[] = [];
  assignFactureForm!: FormGroup;
  permissionVente!: PermissionDto;
  clients: Client[] = [];
  
  // Search and filter
  searchTerm: string = '';
  clientFilter: string = '';
  sortBy: string = 'dateAchat';
  sortOrder: 'asc' | 'desc' = 'desc';
  
  // Selection
  selectedFactures: Set<number> = new Set();
  selectAll: boolean = false;
  
  // UI States
  isLoading: boolean = false;
  deletingIds: Set<number> = new Set();
  idFactureAssigne!: number;

  constructor(private factureService : FactureDeVenteServiceService,
              private router : Router,
              private clientService : ClientService,
              private fb: FormBuilder,
              private notify: NotificationService) {
  }

  ngOnInit() {
    this.getAllFactureAchat();
    this.assignFactureForm = this.fb.group({
      clientId: ['']
    });
    this.getUserAuthData();
  }

  getAllFactureAchat(){
    this.isLoading = true;
    this.factureService.getAllFactures().subscribe({
      next: (data) => {
        this.factureAchatList = data;
        this.applyFiltersAndSort();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.notify.error('Erreur lors du chargement des factures.');
      }
    });
  }

  applyFiltersAndSort() {
    this.filteredList = this.factureAchatList.filter(f => {
      const matchesSearch = 
        f.referance?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        f.modePaiement?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        f.statut?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        f.description?.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesClient = 
        !this.clientFilter || 
        f.client?.id?.toString() === this.clientFilter;
      
      return matchesSearch && matchesClient;
    });

    this.filteredList.sort((a, b) => {
      let aValue: any = a[this.sortBy as keyof FactureVente];
      let bValue: any = b[this.sortBy as keyof FactureVente];

      // Handle client sorting
      if (this.sortBy === 'client') {
        aValue = a.client?.nom || '';
        bValue = b.client?.nom || '';
      }

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

  onClientFilterChange(clientId: string) {
    this.clientFilter = clientId;
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

  getStatusClass(statut: string | undefined): string {
    if (!statut) {
      return 'status-default';
    }
    return 'status-' + statut.toLowerCase().replace(' ', '-');
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
    this.router.navigate(['facture/detaisFactureVente', id]);
  }

  deleteFacture(id: number | undefined) {
    if (id === undefined) return;

    Swal.fire({
      title: 'Confirmer la suppression',
      text: 'Êtes-vous sûr de vouloir supprimer cette facture ? Cette action est irréversible.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Oui, supprimer',
      cancelButtonText: 'Annuler',
      reverseButtons: true
    }).then((result) => {
      if (result.isConfirmed) {
        this.deletingIds.add(id);
        this.factureService.deleteFacture(id).subscribe({
          next: () => {
            this.deletingIds.delete(id);
            this.factureAchatList = this.factureAchatList.filter(f => f.id !== id);
            this.applyFiltersAndSort();
            this.notify.success('La facture a été supprimée avec succès.');
          },
          error: () => {
            this.deletingIds.delete(id);
            this.notify.error('Erreur lors de la suppression de la facture.');
          }
        });
      }
    });
  }

  exportToPdf(facture: FactureVente) {
    if (!facture.id) return;
    this.factureService.getFactureById(facture.id).subscribe(fullFacture => {
      this.factureService.generateFactureVentePDF(fullFacture);
    });
  }

  exportMultipleToPdf() {
    if (this.selectedFactures.size === 0) {
      this.notify.info('Veuillez sélectionner au moins une facture.');
      return;
    }

    const selectedIds = Array.from(this.selectedFactures).filter(id => id !== undefined);
    const requests = selectedIds.map(id => this.factureService.getFactureById(id));

    forkJoin(requests).subscribe({
      next: (fullFactures) => {
        this.factureService.generateMultipleFactureVentePDF(fullFactures);
        this.selectedFactures.clear();
        this.selectAll = false;
      },
      error: () => {
        this.notify.error('Erreur lors du chargement des détails des factures.');
      }
    });
  }

  addFacture() {
    this.router.navigate(['facture/ajouterFactureDeVente']);
  }

  getAllClients(){
    this.clientService.getAllClients().subscribe({
      next: (data) => {
        this.clients = data;
      },
      error: () => {
        this.notify.error('Erreur lors du chargement des clients.');
      }
    });
  }

  factureAssigne(id: number | undefined) {
    if (id) {
      this.idFactureAssigne = id;
      this.getAllClients();
    }
  }

  onSubmitAssignFacture() {
    const clientId = this.assignFactureForm.value.clientId;

    if (this.assignFactureForm && clientId) {
      this.factureService.affecterClient(this.idFactureAssigne, clientId).subscribe({
        next: () => {
          console.log("Facture assignée avec succès");
          this.getAllFactureAchat();
          // Close modal
          const modal = document.getElementById('edit-units');
          if (modal) {
            modal.classList.remove('show');
            modal.style.display = 'none';
          }
        },
        error: () => {
          this.notify.error('Erreur lors de l\'assignation du client.');
        }
      });
    }
  }

  getTotalAmount(): number {
    return this.filteredList.reduce((sum, f) => sum + (f.montantTTC || 0), 0);
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

  changeStatus(facture: FactureVente) {
    const statusLabels: Record<string, string> = {
      'PAYEE': 'Payée',
      'PARTIELLEMENT_PAYEE': 'Partiellement Payée',
      'ANNULEE': 'Annulée',
      'EN_ATTENTE': 'En Attente'
    };

    Swal.fire({
      title: 'Changer le statut',
      input: 'select',
      inputOptions: statusLabels,
      inputValue: facture.statut ?? '',
      showCancelButton: true,
      confirmButtonColor: '#0ea5e9',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Changer',
      cancelButtonText: 'Annuler',
      reverseButtons: true,
      inputPlaceholder: 'Sélectionner un statut'
    }).then((result) => {
      const newStatus = result.value as string;
      if (result.isConfirmed && newStatus && newStatus !== facture.statut) {
        this.factureService.updateStatut(facture.id!, newStatus).subscribe({
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
      currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Vente"){
          this.permissionVente = per;
        }
      })
    }
  }
}