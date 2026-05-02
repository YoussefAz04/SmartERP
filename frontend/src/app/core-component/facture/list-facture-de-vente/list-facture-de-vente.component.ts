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
    this.factureService.generateFactureVentePDF(facture);
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
      const margin = 15;

      // Header bar
      doc.setFillColor(46, 125, 50);
      doc.rect(0, 0, 210, 20, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.text('FACTURE DE VENTE', pageWidth / 2, 14, { align: 'center' });
      doc.setTextColor(0, 0, 0);

      // Invoice details (right column)
      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      doc.text(`N° Facture: ${facture.referance || '—'}`, 130, 30);
      doc.text(`Date: ${facture.dateAchat || '—'}`, 130, 37);
      doc.text(`Statut: ${facture.statut || '—'}`, 130, 44);
      doc.text(`Paiement: ${facture.modePaiement || '—'}`, 130, 51);
      if (facture.commandeVente?.reference) {
        doc.text(`BC: ${facture.commandeVente.reference}`, 130, 58);
      }

      // Client section
      doc.setFontSize(11);
      doc.setFont('helvetica', 'bold');
      doc.text('FACTURÉ À:', margin, 30);
      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');

      const clientLines: string[] = [];
      const fullName = [facture.client?.nom, facture.client?.prenom].filter(Boolean).join(' ');
      if (fullName) clientLines.push(fullName);
      if (facture.client?.adresse) clientLines.push(facture.client.adresse);
      if (facture.client?.email) clientLines.push(facture.client.email);
      if (facture.client?.telephone) clientLines.push(`Tél: ${facture.client.telephone}`);
      if (facture.client?.secteurActivite) clientLines.push(`Secteur: ${facture.client.secteurActivite}`);
      if (clientLines.length === 0) clientLines.push('Client non renseigné');

      clientLines.forEach((line, i) => {
        doc.text(line, margin, 38 + i * 7);
      });

      const separatorY = 38 + clientLines.length * 7 + 4;
      doc.setLineWidth(0.5);
      doc.line(margin, separatorY, 195, separatorY);

      // Line items table
      const rawDetails = facture.detaisFactureDeVente;
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

      // Fallback to commandeVente.lignes when no DetaisFactureDeVente is available
      const commandeLignes = facture.commandeVente?.lignes;
      if (rows.length === 0 && Array.isArray(commandeLignes) && commandeLignes.length > 0) {
        rows = commandeLignes.map((l, i) => {
          const qte = Number(l.quantite) || 1;
          const prix = Number(l.prixUnitaireHT) || 0;
          const tva = Number(l.tva) || 0;
          const totalHT = l.montantHT != null ? Number(l.montantHT) : qte * prix;
          const totalTTC = l.montantTTC != null ? Number(l.montantTTC) : totalHT * (1 + tva / 100);
          return [
            l.articleVente?.nom || `Article ${i + 1}`,
            qte.toString(),
            prix.toFixed(2) + ' DT',
            `${tva}%`,
            totalHT.toFixed(2) + ' DT',
            totalTTC.toFixed(2) + ' DT'
          ];
        });
      }

      autoTable(doc, {
        startY: separatorY + 5,
        head: [['Article', 'Qté', 'Prix HT', 'TVA %', 'Montant HT', 'Total TTC']],
        body: rows.length > 0 ? rows : [['—', '—', '—', '—', '—', '—']],
        margin: { left: margin, right: margin },
        theme: 'grid',
        headStyles: { fillColor: [46, 125, 50], textColor: [255, 255, 255] },
        styles: { fontSize: 9 }
      });

      const finalY = (doc as any).lastAutoTable.finalY + 8;
      const montantHT = facture.montantHT || 0;
      const montantTVA = facture.montantTVA || 0;
      const remise = facture.remise || 0;
      const montantTotal = facture.montantTotal || facture.montantTTC || 0;

      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      doc.text('Sous-total HT:', 130, finalY);
      doc.text(montantHT.toFixed(2) + ' DT', 195, finalY, { align: 'right' });
      doc.text('TVA:', 130, finalY + 7);
      doc.text(montantTVA.toFixed(2) + ' DT', 195, finalY + 7, { align: 'right' });
      if (remise > 0) {
        doc.text('Remise:', 130, finalY + 14);
        doc.text('-' + remise.toFixed(2) + ' DT', 195, finalY + 14, { align: 'right' });
      }
      const totalY = finalY + (remise > 0 ? 21 : 14);
      doc.setFontSize(11);
      doc.setFont('helvetica', 'bold');
      doc.text('TOTAL TTC:', 130, totalY);
      doc.text(montantTotal.toFixed(2) + ' DT', 195, totalY, { align: 'right' });
      doc.setFont('helvetica', 'normal');

      if (facture.description) {
        doc.setFontSize(9);
        doc.text(`Notes: ${facture.description}`, margin, totalY + 10, { maxWidth: 180 });
      }

      // Footer
      doc.setFillColor(46, 125, 50);
      doc.rect(0, 275, 210, 20, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(8);
      doc.text(`Généré le: ${new Date().toLocaleString('fr-FR')}`, margin, 285);
      doc.text('Merci pour votre confiance !', 190, 285, { align: 'right' });
      doc.setTextColor(0, 0, 0);
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
        const updatedFacture = { ...facture, statut: newStatus };
        this.factureService.updateFacture(facture.id!, updatedFacture).subscribe({
          next: () => {
            facture.statut = newStatus;
            this.applyFiltersAndSort();
            Swal.fire({
              icon: 'success',
              title: 'Statut mis à jour !',
              text: `Le statut a été changé en "${statusLabels[newStatus] || newStatus}".`,
              confirmButtonColor: '#0ea5e9',
              timer: 2000,
              timerProgressBar: true
            });
          },
          error: () => {
            Swal.fire({
              icon: 'error',
              title: 'Erreur',
              text: 'Erreur lors de la mise à jour du statut.',
              confirmButtonColor: '#0ea5e9'
            });
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