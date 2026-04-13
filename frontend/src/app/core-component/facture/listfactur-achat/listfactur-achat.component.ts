import {Component, OnInit} from '@angular/core';
import {FactureAchat} from "../../../model/FactureAchat.model";
import {FactureService} from "../../../core/service/facture/facture.service";
import {MatTooltip} from "@angular/material/tooltip";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {Router} from "@angular/router";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-listfactur-achat',
  standalone: true,
  imports: [
    MatTooltip,
    DecimalPipe,
    NgForOf,
    NgIf,
    FormsModule
  ],
  templateUrl: './listfactur-achat.component.html',
  styleUrl: './listfactur-achat.component.scss'
})
export class ListfacturAchatComponent implements OnInit {
  factureAchatList: FactureAchat[] = [];
  filteredList: FactureAchat[] = [];
  permissionAchat: PermissionDto = {
    id: 0,
    moduleId: 0,
    roleId: 0,
    view: false,
    create: false,
    update: false,
    delete: false,
    moduleTitle: ''
  };
  searchTerm: string = '';
  sortBy: string = 'dateAchat';
  sortOrder: 'asc' | 'desc' = 'desc';
  isLoading: boolean = false;
  selectedFactures: Set<number> = new Set();

  constructor(private factureService: FactureService,
              private router: Router) {
  }

  ngOnInit() {
    this.getAllFactureAchat();
    this.getUserAuthData();
  }

  getAllFactureAchat() {
    this.isLoading = true;
    this.factureService.getAllFactureAchat().subscribe({
      next: (data) => {
        this.factureAchatList = data;
        this.applyFiltersAndSort();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        alert('Erreur lors du chargement des factures.');
      }
    });
  }

  applyFiltersAndSort() {
    const term = this.searchTerm.toLowerCase();
    this.filteredList = this.factureAchatList.filter(f =>
      (f.referance?.toLowerCase().includes(term) ?? false) ||
      (f.modePaiement?.toLowerCase().includes(term) ?? false) ||
      (f.statut?.toLowerCase().includes(term) ?? false)
    );

    this.filteredList.sort((a, b) => {
      let firstValue: any = a[this.sortBy as keyof FactureAchat];
      let secondValue: any = b[this.sortBy as keyof FactureAchat];

      if (typeof firstValue === 'string') {
        firstValue = firstValue.toLowerCase();
        secondValue = (secondValue as string)?.toLowerCase() ?? '';
      }

      if (firstValue == null) return 1;
      if (secondValue == null) return -1;
      if (firstValue < secondValue) return this.sortOrder === 'asc' ? -1 : 1;
      if (firstValue > secondValue) return this.sortOrder === 'asc' ? 1 : -1;
      return 0;
    });
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

  viewFacture(id: number | undefined) {
    this.router.navigate(['facture/detaisFactureAchat', id]);
  }

  deleteFacture(id: number | undefined) {
    if (id === undefined) return;
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette facture ?')) return;
    this.factureService.deleteFactureAchat(id).subscribe({
      next: () => {
        this.factureAchatList = this.factureAchatList.filter(f => f.id !== id);
        this.applyFiltersAndSort();
      },
      error: () => {
        alert('Erreur lors de la suppression de la facture.');
      }
    });
  }

  exportToPdf(facture: FactureAchat) {
    const doc = new jsPDF();
    this.renderFacturePage(doc, facture, true);
    doc.save(`Facture_${facture.referance ?? 'achat'}.pdf`);
  }

  exportMultipleToPdf() {
    if (this.selectedFactures.size === 0) {
      alert('Veuillez sélectionner au moins une facture.');
      return;
    }

    const doc = new jsPDF();
    const selectedItems = Array.from(this.selectedFactures)
      .map(id => this.factureAchatList.find(f => f.id === id))
      .filter((f): f is FactureAchat => f !== undefined);

    selectedItems.forEach((facture, index) => {
      if (index > 0) doc.addPage();
      this.renderFacturePage(doc, facture, false);
    });

    doc.save('Factures_Achat.pdf');
    this.selectedFactures.clear();
  }

  private renderFacturePage(doc: jsPDF, facture: FactureAchat, includeDescription: boolean) {
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 14;

    doc.setFillColor(41, 128, 185);
    doc.rect(0, 0, pageWidth, 30, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text("FACTURE D'ACHAT", pageWidth / 2, 19, {align: 'center'});

    doc.setTextColor(50, 50, 50);
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    let y = 40;

    doc.setFont('helvetica', 'bold');
    doc.text('Référence:', margin, y);
    doc.setFont('helvetica', 'normal');
    doc.text(`${facture.referance ?? '-'}`, margin + 30, y);
    y += 7;

    doc.setFont('helvetica', 'bold');
    doc.text("Date d'achat:", margin, y);
    doc.setFont('helvetica', 'normal');
    doc.text(`${facture.dateAchat ?? '-'}`, margin + 30, y);
    y += 7;

    doc.setFont('helvetica', 'bold');
    doc.text('Mode de paiement:', margin, y);
    doc.setFont('helvetica', 'normal');
    doc.text(`${facture.modePaiement ?? '-'}`, margin + 45, y);
    y += 7;

    doc.setFont('helvetica', 'bold');
    doc.text('Statut:', margin, y);
    doc.setFont('helvetica', 'normal');
    doc.text(`${facture.statut ?? '-'}`, margin + 20, y);
    y += 12;

    autoTable(doc, {
      startY: y,
      head: [['Détail', 'Montant']],
      body: [
        ['Montant HT', `${(facture.montantHT ?? 0).toFixed(2)} €`],
        ['Montant TVA', `${(facture.montantTVA ?? 0).toFixed(2)} €`],
        ['Remise', `${(facture.remise ?? 0).toFixed(2)} €`],
        ['Montant Total', `${(facture.montantTotal ?? 0).toFixed(2)} €`],
        ['Montant TTC', `${(facture.montantTTC ?? 0).toFixed(2)} €`]
      ],
      margin: {left: margin, right: margin},
      theme: 'grid',
      headStyles: {fillColor: [41, 128, 185], textColor: [255, 255, 255], fontStyle: 'bold'},
      alternateRowStyles: {fillColor: [245, 248, 252]}
    });

    if (includeDescription && facture.description) {
      const finalY = (doc as any).lastAutoTable?.finalY ?? y + 40;
      doc.setFontSize(10);
      doc.setFont('helvetica', 'bold');
      doc.text('Description:', margin, finalY + 10);
      doc.setFont('helvetica', 'normal');
      doc.text(facture.description, margin, finalY + 17, {maxWidth: pageWidth - 2 * margin});
    }

    doc.setFontSize(8);
    doc.setTextColor(150, 150, 150);
    doc.text(`Généré le : ${new Date().toLocaleString('fr-FR')}`, margin, pageHeight - 10);
  }

  toggleSelectFacture(id: number | undefined) {
    if (id === undefined) return;
    if (this.selectedFactures.has(id)) {
      this.selectedFactures.delete(id);
    } else {
      this.selectedFactures.add(id);
    }
  }

  isFactureSelected(id: number | undefined): boolean {
    return id !== undefined && this.selectedFactures.has(id);
  }

  get allSelected(): boolean {
    return this.filteredList.length > 0 &&
      this.filteredList.every(f => f.id !== undefined && this.selectedFactures.has(f.id));
  }

  toggleSelectAll(checked: boolean) {
    if (checked) {
      this.filteredList.forEach(f => { if (f.id !== undefined) this.selectedFactures.add(f.id); });
    } else {
      this.filteredList.forEach(f => { if (f.id !== undefined) this.selectedFactures.delete(f.id); });
    }
  }

  getUserAuthData() {
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      currentUser?.permission.map(per => {
        if (per.moduleTitle == "Achat") {
          this.permissionAchat = per;
        }
      });
    }
  }
}