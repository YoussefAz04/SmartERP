import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
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
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import Swal from 'sweetalert2';

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
    FormsModule
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
              private fb: FormBuilder) {
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
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors du chargement des factures.',
          confirmButtonColor: '#4caf50'
        });
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
            Swal.fire({
              icon: 'success',
              title: 'Supprimé !',
              text: 'La facture a été supprimée avec succès.',
              confirmButtonColor: '#4caf50',
              timer: 2500,
              timerProgressBar: true
            });
          },
          error: () => {
            this.deletingIds.delete(id);
            Swal.fire({
              icon: 'error',
              title: 'Erreur',
              text: 'Erreur lors de la suppression de la facture.',
              confirmButtonColor: '#4caf50'
            });
          }
        });
      }
    });
  }

  exportToPdf(facture: FactureVente) {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 10;

    // Header
    doc.setFontSize(16);
    doc.text('FACTURE DE VENTE', pageWidth / 2, 15, { align: 'center' });

    // Facture details
    doc.setFontSize(10);
    let yPosition = 30;
    
    doc.text(`Référence: ${facture.referance}`, margin, yPosition);
    yPosition += 7;
    doc.text(`Date: ${facture.dateAchat}`, margin, yPosition);
    yPosition += 7;
    doc.text(`Client: ${facture.client?.nom || 'N/A'}`, margin, yPosition);
    yPosition += 7;
    doc.text(`Mode de Paiement: ${facture.modePaiement}`, margin, yPosition);
    yPosition += 7;
    doc.text(`Statut: ${facture.statut}`, margin, yPosition);
    yPosition += 12;

    // Financial summary table
    const summaryData = [
      ['Montant HT', `${(facture.montantHT || 0).toFixed(2)} DT`],
      ['Montant TVA', `${(facture.montantTVA || 0).toFixed(2)} DT`],
      ['Remise', `${(facture.remise || 0).toFixed(2)} DT`],
      ['Montant Total', `${(facture.montantTotal || 0).toFixed(2)} DT`],
      ['Montant TTC', `${(facture.montantTTC || 0).toFixed(2)} DT`]
    ];

    autoTable(doc, {
      startY: yPosition,
      head: [['Détail', 'Montant']],
      body: summaryData,
      margin: margin,
      theme: 'grid',
      headStyles: {
        fillColor: [76, 175, 80],
        textColor: [255, 255, 255],
        fontStyle: 'bold'
      },
      alternateRowStyles: {
        fillColor: [245, 245, 245]
      }
    });

    // Description if available
    if (facture.description) {
      yPosition = (doc as any).lastAutoTable.finalY + 10;
      doc.setFontSize(10);
      doc.text('Description:', margin, yPosition);
      doc.setFontSize(9);
      doc.text(facture.description, margin, yPosition + 5, { maxWidth: pageWidth - 2 * margin });
    }

    // Footer
    doc.setFontSize(8);
    doc.text(`Généré le: ${new Date().toLocaleString('fr-FR')}`, margin, pageHeight - 10);

    doc.save(`Facture_Vente_${facture.referance}.pdf`);
  }

  exportMultipleToPdf() {
    if (this.selectedFactures.size === 0) {
      Swal.fire({
        icon: 'info',
        title: 'Aucune sélection',
        text: 'Veuillez sélectionner au moins une facture.',
        confirmButtonColor: '#4caf50'
      });
      return;
    }

    const doc = new jsPDF();
    const selectedItems = Array.from(this.selectedFactures)
      .map(id => this.factureAchatList.find(f => f.id === id))
      .filter(f => f !== undefined) as FactureVente[];

    selectedItems.forEach((facture, index) => {
      if (index > 0) {
        doc.addPage();
      }

      const pageWidth = doc.internal.pageSize.getWidth();
      const margin = 10;

      doc.setFontSize(16);
      doc.text('FACTURE DE VENTE', pageWidth / 2, 15, { align: 'center' });

      doc.setFontSize(10);
      let yPosition = 30;

      doc.text(`Référence: ${facture.referance}`, margin, yPosition);
      yPosition += 7;
      doc.text(`Date: ${facture.dateAchat}`, margin, yPosition);
      yPosition += 7;
      doc.text(`Client: ${facture.client?.nom || 'N/A'}`, margin, yPosition);
      yPosition += 12;

      const summaryData = [
        ['Montant HT', `${(facture.montantHT || 0).toFixed(2)} DT`],
        ['Montant TVA', `${(facture.montantTVA || 0).toFixed(2)} DT`],
        ['Remise', `${(facture.remise || 0).toFixed(2)} DT`],
        ['Montant Total', `${(facture.montantTotal || 0).toFixed(2)} DT`],
        ['Montant TTC', `${(facture.montantTTC || 0).toFixed(2)} DT`]
      ];

      autoTable(doc, {
        startY: yPosition,
        head: [['Détail', 'Montant']],
        body: summaryData,
        margin: margin,
        theme: 'grid',
        headStyles: {
          fillColor: [76, 175, 80],
          textColor: [255, 255, 255],
          fontStyle: 'bold'
        }
      });
    });

    doc.save('Factures_Vente.pdf');
    this.selectedFactures.clear();
    this.selectAll = false;
  }

  sendEmailFactureVente(facture: FactureVente) {
    this.factureService.sendEmailFactureVente(facture);
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
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors du chargement des clients.',
          confirmButtonColor: '#4caf50'
        });
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
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: 'Erreur lors de l\'assignation du client.',
            confirmButtonColor: '#4caf50'
          });
        }
      });
    }
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