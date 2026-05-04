import {Component, OnInit} from '@angular/core';
import {FactureAchat} from "../../../model/FactureAchat.model";
import {FactureService} from "../../../core/service/facture/facture.service";
import {MatTooltip} from "@angular/material/tooltip";
import {DecimalPipe, NgForOf, NgIf, CommonModule} from "@angular/common";
import {Router, RouterLink} from "@angular/router";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import {FormsModule} from "@angular/forms";
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
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
  importing: boolean = false;
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
        this.notify.error('Erreur lors du chargement des factures.');
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
      'ANNULEE': 'Annulée'
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

  exportToPdf(facture: FactureAchat) {
    this.factureService.generateFactureAchatPDF(facture);
  }

  exportMultipleToPdf() {
    if (this.selectedFactures.size === 0) {
      this.notify.info('Veuillez sélectionner au moins une facture.');
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
      const pageHeight = doc.internal.pageSize.getHeight();
      const margin = 15;

      // Header bar
      doc.setFillColor(41, 128, 185);
      doc.rect(0, 0, 210, 28, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(22);
      doc.setFont('helvetica', 'bold');
      doc.text("FACTURE D'ACHAT", pageWidth / 2, 18.5, { align: 'center' });
      doc.setTextColor(0, 0, 0);

      // Invoice details panel (right column)
      doc.setFillColor(248, 249, 250);
      doc.rect(112, 33, 86, 44, 'F');
      doc.setFontSize(9);
      doc.setFont('helvetica', 'normal');
      doc.text(`N° Facture: ${facture.referance || '—'}`, 117, 43);
      doc.text(`Date: ${facture.dateAchat || '—'}`, 117, 51);
      doc.text(`Statut: ${facture.statut || '—'}`, 117, 59);
      doc.text(`Paiement: ${facture.modePaiement || '—'}`, 117, 67);
      if (facture.commandeAchat?.reference) {
        doc.text(`BC: ${facture.commandeAchat.reference}`, 117, 75);
      }

      // Section heading: FOURNISSEUR
      doc.setFillColor(41, 128, 185);
      doc.rect(12, 33, 86, 8, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(10.5);
      doc.setFont('helvetica', 'bold');
      doc.text('FOURNISSEUR:', 17, 39);
      doc.setTextColor(0, 0, 0);

      // Fournisseur info
      doc.setFontSize(9.5);
      doc.setFont('helvetica', 'normal');

      const fournisseurLines: string[] = [];
      if (facture.fournisseur?.nom) fournisseurLines.push(facture.fournisseur.nom);
      if (facture.fournisseur?.adresse) fournisseurLines.push(facture.fournisseur.adresse);
      if (facture.fournisseur?.email) fournisseurLines.push(facture.fournisseur.email);
      if (facture.fournisseur?.telephone) fournisseurLines.push(`Tél: ${facture.fournisseur.telephone}`);
      const villePays = [facture.fournisseur?.ville, facture.fournisseur?.pays].filter(Boolean).join(', ');
      if (villePays) fournisseurLines.push(villePays);
      if (facture.fournisseur?.numeroIdentificationFiscale) {
        fournisseurLines.push(`NIF: ${facture.fournisseur.numeroIdentificationFiscale}`);
      }
      if (facture.fournisseur?.registreCommerce) {
        fournisseurLines.push(`RC: ${facture.fournisseur.registreCommerce}`);
      }
      if (fournisseurLines.length === 0) {
        fournisseurLines.push('Non renseigné');
      }
      fournisseurLines.forEach((line, i) => {
        doc.text(line, 17, 46 + i * 7);
      });

      const separatorY = 46 + fournisseurLines.length * 7 + 6;
      doc.setDrawColor(200, 200, 200);
      doc.setLineWidth(0.4);
      doc.line(12, separatorY, 198, separatorY);

      // Line items table
      const rawDetails = facture.detaisFactureAchat;
      const details = Array.isArray(rawDetails) ? rawDetails : (rawDetails ? [rawDetails] : []);
      let rows: string[][] = details.map((d, i) => {
        const qte = d.quantite || 1;
        const prix = d.prixUnitaireHT || 0;
        const tva = d.tva || 0;
        const totalHT = qte * prix;
        const totalTTC = totalHT * (1 + tva / 100);
        return [
          d.article || `Article ${i + 1}`,
          qte.toString(),
          prix.toFixed(2) + ' DT',
          `${tva}%`,
          totalHT.toFixed(2) + ' DT',
          totalTTC.toFixed(2) + ' DT'
        ];
      });

      // Fallback to lignes (LigneFactureAchat) when no DetaisFactureAchat is available
      if (rows.length === 0 && Array.isArray(facture.lignes) && facture.lignes.length > 0) {
        rows = facture.lignes.map((l, i) => {
          const qte = Number(l.quantite) || 1;
          const prix = Number(l.prixUnitaireHT) || 0;
          const tva = Number(l.tva) || 0;
          const totalHT = l.montantHT != null ? Number(l.montantHT) : qte * prix;
          const totalTTC = l.montantTTC != null ? Number(l.montantTTC) : totalHT * (1 + tva / 100);
          return [
            l.designation || `Article ${i + 1}`,
            qte.toString(),
            prix.toFixed(2) + ' DT',
            `${tva}%`,
            totalHT.toFixed(2) + ' DT',
            totalTTC.toFixed(2) + ' DT'
          ];
        });
      }

      autoTable(doc, {
        startY: separatorY + 6,
        head: [['Article', 'Qté', 'Prix HT', 'TVA %', 'Montant HT', 'Total TTC']],
        body: rows.length > 0 ? rows : [['—', '—', '—', '—', '—', '—']],
        margin: { left: 12, right: 12 },
        theme: 'grid',
        headStyles: { fillColor: [41, 128, 185], textColor: [255, 255, 255], fontStyle: 'bold', fontSize: 9 },
        styles: { fontSize: 9 },
        alternateRowStyles: { fillColor: [252, 252, 252] }
      });

      const finalY = (doc as any).lastAutoTable.finalY + 8;
      const montantHT = facture.montantHT || 0;
      const montantTVA = facture.montantTVA || 0;
      const remise = facture.remise || 0;
      const montantTotal = facture.montantTotal || facture.montantTTC || 0;
      const hasRemise = remise > 0;

      doc.setFontSize(9.5);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(60, 60, 60);
      doc.text('Sous-total HT:', 117, finalY + 2);
      doc.text(montantHT.toFixed(2) + ' DT', 196, finalY + 2, { align: 'right' });
      doc.text('TVA:', 117, finalY + 10);
      doc.text(montantTVA.toFixed(2) + ' DT', 196, finalY + 10, { align: 'right' });
      let summaryOffset = 18;
      if (hasRemise) {
        doc.text('Remise:', 117, finalY + summaryOffset);
        doc.text('-' + remise.toFixed(2) + ' DT', 196, finalY + summaryOffset, { align: 'right' });
        summaryOffset += 8;
      }
      doc.setDrawColor(180, 180, 180);
      doc.setLineWidth(0.4);
      doc.line(112, finalY + summaryOffset - 3, 198, finalY + summaryOffset - 3);
      doc.setFillColor(41, 128, 185);
      doc.rect(112, finalY + summaryOffset - 3, 86, 11, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(11);
      doc.setFont('helvetica', 'bold');
      doc.text('TOTAL TTC:', 117, finalY + summaryOffset + 4.5);
      doc.text(montantTotal.toFixed(2) + ' DT', 196, finalY + summaryOffset + 4.5, { align: 'right' });
      doc.setTextColor(0, 0, 0);
      doc.setFont('helvetica', 'normal');

      if (facture.description) {
        doc.setFontSize(9);
        doc.setTextColor(80, 80, 80);
        doc.text(`Notes: ${facture.description}`, 17, finalY + summaryOffset + 18, { maxWidth: 90 });
        doc.setTextColor(0, 0, 0);
      }

      // Footer
      doc.setFillColor(41, 128, 185);
      doc.rect(0, 272, 210, 25, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(8);
      doc.text(`Généré le: ${new Date().toLocaleString('fr-FR')}`, 17, 283);
      doc.text('Merci pour votre collaboration !', 193, 283, { align: 'right' });
      doc.setTextColor(0, 0, 0);
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
      cancelButtonText: 'Annuler',
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
      'ANNULEE': 'Annulée',
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
      cancelButtonText: 'Annuler',
      reverseButtons: true,
      inputPlaceholder: 'Sélectionner un statut'
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

  /**
   * Importer une facture d'achat depuis un PDF : extrait les champs via OCR
   * (zones FACTURE_ACHAT), affiche un aperçu puis sauvegarde après confirmation.
   */
  onImportPdfSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0];
    if (!file) return;
    if (file.type !== 'application/pdf') {
      this.notify.warning('Veuillez sélectionner un PDF.');
      input.value = '';
      return;
    }

    this.importing = true;
    this.factureService.importFromPdf(file).subscribe({
      next: (data: Record<string, unknown>) => {
        this.showImportPreview(file, data);
        this.importing = false;
        input.value = '';
      },
      error: () => {
        this.importing = false;
        input.value = '';
        this.notify.error("L'extraction OCR a échoué. Vérifiez que des zones FACTURE_ACHAT ont été enregistrées.");
      }
    });
  }

  private showImportPreview(file: File, data: Record<string, unknown>) {
    if (data?.['warning']) {
      this.notify.warning(String(data['warning']));
      return;
    }

    const fmt = (v: unknown) => (v === null || v === undefined || v === '') ? '<em>—</em>' : String(v);
    const html = `
      <div style="text-align:left;font-size:0.95em;">
        <div><strong>Référence :</strong> ${fmt(data['referance'])}</div>
        <div><strong>Date :</strong> ${fmt(data['dateAchat'])}</div>
        <div><strong>Montant HT :</strong> ${fmt(data['montantHT'])}</div>
        <div><strong>Montant TVA :</strong> ${fmt(data['montantTVA'])}</div>
        <div><strong>Montant TTC :</strong> ${fmt(data['montantTTC'])}</div>
        <div><strong>Remise :</strong> ${fmt(data['remise'])}</div>
        <div><strong>Mode paiement :</strong> ${fmt(data['modePaiement'])}</div>
        <div><strong>Statut :</strong> ${fmt(data['statut'])}</div>
        <div><strong>Description :</strong> ${fmt(data['description'])}</div>
      </div>
    `;

    Swal.fire({
      title: 'Aperçu de la facture extraite',
      html,
      icon: 'info',
      showCancelButton: true,
      confirmButtonText: 'Sauvegarder',
      cancelButtonText: 'Annuler',
      reverseButtons: true
    }).then((res) => {
      if (res.isConfirmed) {
        this.saveImportedFromPdf(file);
      }
    });
  }

  private saveImportedFromPdf(file: File) {
    this.importing = true;
    this.factureService.saveFromPdf(file).subscribe({
      next: () => {
        this.importing = false;
        this.notify.success('La facture d\'achat a été créée avec succès.');
        this.getAllFactureAchat();
      },
      error: () => {
        this.importing = false;
        this.notify.error('Impossible d\'enregistrer la facture importée.');
      }
    });
  }
}