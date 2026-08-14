import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize, timeout } from 'rxjs/operators';
import { CommandeVenteService } from '../../../core/service/commande-vente/commande-vente.service';
import { CommandeVente } from '../../../model/commande-vente.model';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-list-commande-vente',
  standalone: true,
  imports: [CommonModule, RouterOutlet, FormsModule],
  templateUrl: './list-commande-vente.component.html',
  styleUrl: './list-commande-vente.component.scss'
})
export class ListCommandeVenteComponent implements OnInit {
  commandes: CommandeVente[] = [];
  filteredCommandes: CommandeVente[] = [];
  isLoading = false;
  searchTerm = '';
  selectedStatut = '';
  dateDebut = '';
  dateFin = '';
  lastGeneratedFacture: any = null;
  showFactureModal = false;
  errorMessage = '';

  canCreate = false;
  canUpdate = false;
  canDelete = false;

  statuts = ['BROUILLON', 'EN_ATTENTE', 'CONFIRMEE', 'LIVREE', 'ANNULEE'];

  constructor(private commandeVenteService: CommandeVenteService,
    private router: Router,
    private notification: NotificationService,
    private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadRolePermissions();
    this.loadCommandes();
  }

  loadRolePermissions(): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const currentUser: any = JSON.parse(decodeURIComponent(escape(atob(item))));
        const roles: string[] = Array.isArray(currentUser?.roles) ? currentUser.roles : [];
        const canByRole = roles.includes('SUPER_ADMIN') || roles.includes('ADMIN');

        const normalize = (s: string | undefined | null) =>
          (s ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
        const targetTitle = normalize('Commande Vente');

        let permissionFound = false;
        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          const perm = currentUser.permission.find((p: any) => normalize(p.moduleTitle) === targetTitle);
          if (perm) {
            permissionFound = true;
            this.canCreate = perm.create === true;
            this.canUpdate = perm.update === true;
            this.canDelete = perm.delete === true;
          }
        }

        if (!permissionFound) {
          this.canCreate = canByRole;
          this.canUpdate = canByRole;
          this.canDelete = canByRole;
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
    this.cdr.detectChanges();
  }

  loadCommandes(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.commandeVenteService.getAllCommandes()
      .pipe(
        timeout(15000),
        finalize(() => { this.isLoading = false; })
      )
      .subscribe({
      next: (data) => {
        this.commandes = data;
        this.applyFilters();
      },
      error: (err) => {
        console.error('❌ Erreur lors du chargement:', err);
        this.errorMessage = err?.name === 'TimeoutError'
          ? 'Loading orders is taking too long. Please try again.'
          : 'Erreur lors du chargement des commandes';
      }
    });
  }

  applyFilters(): void {
    const debut = this.dateDebut ? new Date(this.dateDebut) : null;
    const fin = this.dateFin ? new Date(new Date(this.dateFin).setHours(23, 59, 59, 999)) : null;

    this.filteredCommandes = this.commandes.filter(c => {
      const matchSearch = !this.searchTerm ||
        c.reference?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        c.client?.nom?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchStatut = !this.selectedStatut || c.statut === this.selectedStatut;
      const date = c.dateCommande ? new Date(c.dateCommande) : null;
      const matchDebut = !debut || (date !== null && date >= debut);
      const matchFin = !fin || (date !== null && date <= fin);
      return matchSearch && matchStatut && matchDebut && matchFin;
    });
  }

  addCommande(): void {
    this.router.navigate(['/commande-vente/add']);
  }

  editCommande(id: number): void {
    this.router.navigate(['/commande-vente/edit', id]);
  }

  validerCommande(id: number): void {
    this.notification.confirm('Validate this order? A sales invoice will be generated automatically.').then(ok => { if (!ok) return;
    this.isLoading = true;
    this.commandeVenteService.validerCommande(id).subscribe({
      next: (response) => {
        // Backend now returns { facture, stockAlerts } (ValidationResponseDto).
        // Fall back to the legacy shape (bare FactureDeVente) just in case.
        const facture = response?.facture ?? response;
        const alerts: { type?: string; message?: string; articleName?: string }[] =
          response?.stockAlerts ?? [];
        this.lastGeneratedFacture = facture;
        this.showFactureModal = true;
        this.loadCommandes();
        this.notification.success('✅ Order validated! Sales invoice generated: ' + facture.referance);

        // Show a red toast for each article whose stock fell below the threshold.
        for (const alert of alerts) {
          const msg = alert?.message || `Stock faible pour '${alert?.articleName ?? ''}'.`;
          this.notification.error(msg);
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('❌ Erreur validation:', err);
        const errorMessage = err?.error?.message || err?.message || 'Erreur lors de la validation';
        this.notification.error('❌ Erreur lors de la validation:\n\n' + errorMessage);
      }
      });
    });
  }

  livrerCommande(id: number): void {
    this.notification.confirm('Mark this order as delivered? Stock will be decremented automatically.').then(ok => { if (!ok) return;
    this.isLoading = true;
    this.commandeVenteService.livrerCommande(id).subscribe({
      next: (response) => {
        this.loadCommandes();
        // Backend now returns { bonLivraison, stockAlerts } (LivraisonResponseDto).
        // Fall back to the legacy shape (bare BonLivraison) just in case.
        const bon = response?.bonLivraison ?? response;
        const alerts: { type?: string; message?: string; articleName?: string }[] =
          response?.stockAlerts ?? [];
        this.notification.success('✅ Order delivered! Delivery note created: ' + bon?.reference + '. Stock has been updated.');

        // Immediately warn the user when an article fell to rupture or below
        // its seuilMinimum after this sale — a red toast per affected article.
        for (const alert of alerts) {
          const msg = alert?.message
            || (alert?.type === 'RUPTURE'
                ? `OUT OF STOCK: '${alert?.articleName ?? ''}' is depleted!`
                : `STOCK FAIBLE : '${alert?.articleName ?? ''}' en dessous du seuil.`);
          this.notification.error(msg);
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('❌ Erreur livraison:', err);
        const errorMessage = err?.error?.message || err?.message || 'Erreur lors de la livraison';
        this.notification.error('❌ Erreur lors de la livraison:\n\n' + errorMessage);
      }
      });
    });
  }

  annulerCommande(id: number): void {
    this.notification.confirm('Cancel cette commande ?').then(ok => { if (!ok) return;
    this.commandeVenteService.annulerCommande(id).subscribe({
      next: () => {
        this.loadCommandes();
        this.notification.success('✅ Order canceled.');
      },
      error: (err) => {
        console.error('❌ Erreur annulation:', err);
        const errorMessage = err?.error?.message || 'Erreur lors de l\'annulation';
        this.notification.error('❌ ' + errorMessage);
      }
      });
    });
  }

  deleteCommande(id: number): void {
    this.notification.confirm('Supprimer cette commande ?').then(ok => { if (!ok) return;
    this.commandeVenteService.deleteCommande(id).subscribe({
      next: () => {
        this.loadCommandes();
        this.notification.success('✅ Order deleted.');
      },
      error: (err) => {
        console.error('❌ Erreur suppression:', err);
        const errorMessage = err?.error?.message || 'Error during deletion';
        this.notification.error('❌ ' + errorMessage);
      }
      });
    });
  }

  canEditOrValidate(statut?: string): boolean {
    return statut !== 'CONFIRMEE' && statut !== 'LIVREE' && statut !== 'ANNULEE';
  }

  canDeliver(statut?: string): boolean {
    return statut === 'CONFIRMEE';
  }

  canCancel(statut?: string): boolean {
    return statut !== 'LIVREE' && statut !== 'ANNULEE';
  }

  getStatutClass(statut?: string): string {
    switch (statut) {
      case 'BROUILLON':  return 'status-brouillon';
      case 'EN_ATTENTE': return 'status-en-attente';
      case 'CONFIRMEE':  return 'status-confirmee';
      case 'LIVREE':     return 'status-livree';
      case 'ANNULEE':    return 'status-annulee';
      default:           return 'status-default';
    }
  }

  getStatutLabel(statut?: string): string {
    switch (statut) {
      case 'BROUILLON':  return 'Brouillon';
      case 'EN_ATTENTE': return 'En attente';
      case 'CONFIRMEE':  return 'Confirmed';
      case 'LIVREE':     return 'Delivered';
      case 'ANNULEE':    return 'Canceled';
      default:           return statut || '-';
    }
  }

  get totalMontantTTC(): number {
    return this.commandes.reduce((sum, c) => sum + (c.montantTTC || 0), 0);
  }

  get commandesEnCours(): number {
    return this.commandes.filter(c => c.statut === 'BROUILLON' || c.statut === 'EN_ATTENTE').length;
  }

  get commandesConfirmees(): number {
    return this.commandes.filter(c => c.statut === 'CONFIRMEE').length;
  }

  get commandesLivrees(): number {
    return this.commandes.filter(c => c.statut === 'LIVREE').length;
  }

  get filteredMontantHT(): number {
    return this.filteredCommandes.reduce((sum, c) => sum + (c.montantHT || 0), 0);
  }

  get filteredMontantTVA(): number {
    return this.filteredCommandes.reduce((sum, c) => sum + (c.montantTVA || 0), 0);
  }

  get filteredMontantTTC(): number {
    return this.filteredCommandes.reduce((sum, c) => sum + (c.montantTTC || 0), 0);
  }

  exportCSV(): void {
    const headers = ['Reference', 'Date Commande', 'Expected Delivery Date', 'Client', 'Statut', 'Amount excl. tax', 'VAT Amount', 'Amount incl. tax'];
    const rows = this.filteredCommandes.map(c => [
      c.reference ?? '',
      c.dateCommande ?? '',
      c.dateLivraisonPrevue ?? '',
      c.client?.nom ?? '',
      this.getStatutLabel(c.statut),
      c.montantHT ?? 0,
      c.montantTVA ?? 0,
      c.montantTTC ?? 0,
    ]);
    const csvContent = [headers, ...rows]
      .map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
      .join('\n');
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `commandes-vente-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  getStatutBadgeClass(statut?: string): string {
    switch (statut) {
      case 'CONFIRMEE':  return 'badge bg-success';
      case 'LIVREE':     return 'badge bg-primary';
      case 'ANNULEE':    return 'badge bg-danger';
      case 'EN_ATTENTE': return 'badge bg-warning text-dark';
      case 'BROUILLON':  return 'badge bg-secondary';
      default:           return 'badge bg-secondary';
    }
  }

  closeFactureModal(): void {
    this.showFactureModal = false;
    this.lastGeneratedFacture = null;
  }

  viewFacture(commandeId: number): void {
    this.router.navigate(['/facture/listFactureVente']);
  }
}
