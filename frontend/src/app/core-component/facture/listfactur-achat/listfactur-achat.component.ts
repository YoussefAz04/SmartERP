import {Component, OnInit} from '@angular/core';
import {FactureAchat} from "../../../model/FactureAchat.model";
import {FactureService} from "../../../core/service/facture/facture.service";
import {MatTooltip} from "@angular/material/tooltip";
import {DecimalPipe, NgForOf, NgIf, CommonModule} from "@angular/common";
import {Router} from "@angular/router";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import {FormsModule} from "@angular/forms";
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-listfactur-achat',
  standalone: true,
  imports: [
    MatTooltip,
    DecimalPipe,
    NgForOf,
    NgIf,
    CommonModule,
    FormsModule
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
              private router : Router) {
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
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors du chargement des factures.',
          confirmButtonColor: '#667eea'
        });
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
        this.factureService.deleteFactureAchat(id).subscribe({
          next: () => {
            this.deletingIds.delete(id);
            this.factureAchatList = this.factureAchatList.filter(f => f.id !== id);
            this.applyFiltersAndSort();
            Swal.fire({
              icon: 'success',
              title: 'Supprimé !',
              text: 'La facture a été supprimée avec succès.',
              confirmButtonColor: '#667eea',
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
              confirmButtonColor: '#667eea'
            });
          }
        });
      }
    });
  }

  exportToPdf(facture: FactureAchat) {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 10;

    doc.setFontSize(16);
    doc.text('FACTURE D\'ACHAT', pageWidth / 2, 15, { align: 'center' });

    doc.setFontSize(10);
    let yPosition = 30;
    
    doc.text(`Référence: ${facture.referance}`, margin, yPosition);
    yPosition += 7;
    doc.text(`Date d'Achat: ${facture.dateAchat}`, margin, yPosition);
    yPosition += 7;
    doc.text(`Mode de Paiement: ${facture.modePaiement}`, margin, yPosition);
    yPosition += 7;
    doc.text(`Statut: ${facture.statut}`, margin, yPosition);
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
        fillColor: [41, 128, 185],
        textColor: [255, 255, 255],
        fontStyle: 'bold'
      },
      alternateRowStyles: {
        fillColor: [245, 245, 245]
      }
    });

    if (facture.description) {
      yPosition = (doc as any).lastAutoTable.finalY + 10;
      doc.setFontSize(10);
      doc.text('Description:', margin, yPosition);
      doc.setFontSize(9);
      doc.text(facture.description, margin, yPosition + 5, { maxWidth: pageWidth - 2 * margin });
    }

    doc.setFontSize(8);
    doc.text(`Généré le: ${new Date().toLocaleString('fr-FR')}`, margin, pageHeight - 10);

    doc.save(`Facture_${facture.referance}.pdf`);
  }

  exportMultipleToPdf() {
    if (this.selectedFactures.size === 0) {
      Swal.fire({
        icon: 'info',
        title: 'Aucune sélection',
        text: 'Veuillez sélectionner au moins une facture.',
        confirmButtonColor: '#667eea'
      });
      return;
    }

    const doc = new jsPDF();
    const selectedItems = Array.from(this.selectedFactures)
      .map(id => this.factureAchatList.find(f => f.id === id))
      .filter(f => f !== undefined) as FactureAchat[];

    selectedItems.forEach((facture, index) => {
      if (index > 0) {
        doc.addPage();
      }

      const pageWidth = doc.internal.pageSize.getWidth();
      const margin = 10;

      doc.setFontSize(16);
      doc.text('FACTURE D\'ACHAT', pageWidth / 2, 15, { align: 'center' });

      doc.setFontSize(10);
      let yPosition = 30;

      doc.text(`Référence: ${facture.referance}`, margin, yPosition);
      yPosition += 7;
      doc.text(`Date d'Achat: ${facture.dateAchat}`, margin, yPosition);
      yPosition += 7;
      doc.text(`Mode de Paiement: ${facture.modePaiement}`, margin, yPosition);
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
          fillColor: [41, 128, 185],
          textColor: [255, 255, 255],
          fontStyle: 'bold'
        }
      });
    });

    doc.save('Factures_Achat.pdf');
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