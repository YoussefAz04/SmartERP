import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
    this.commandeVenteService.getAllCommandes().subscribe({
      next: (data) => {
        this.commandes = data;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('❌ Erreur lors du chargement:', err);
        this.errorMessage = 'Erreur lors du chargement des commandes';
        this.isLoading = false;
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
    this.notification.confirm('Valider cette commande ? Une facture vente sera automatiquement générée.').then(ok => { if (!ok) return;
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
        this.notification.success('✅ Commande validée ! Facture vente générée : ' + facture.referance);

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
    this.notification.confirm('Marquer cette commande comme livrée ? Le stock sera automatiquement décrémenté.').then(ok => { if (!ok) return;
    this.isLoading = true;
    this.commandeVenteService.livrerCommande(id).subscribe({
      next: (response) => {
        this.loadCommandes();
        // Backend now returns { bonLivraison, stockAlerts } (LivraisonResponseDto).
        // Fall back to the legacy shape (bare BonLivraison) just in case.
        const bon = response?.bonLivraison ?? response;
        const alerts: { type?: string; message?: string; articleName?: string }[] =
          response?.stockAlerts ?? [];
        this.notification.success('✅ Commande livrée ! Bon de livraison créé : ' + bon?.reference + '. Le stock a été mis à jour.');

        // Immediately warn the user when an article fell to rupture or below
        // its seuilMinimum after this sale — a red toast per affected article.
        for (const alert of alerts) {
          const msg = alert?.message
            || (alert?.type === 'RUPTURE'
                ? `RUPTURE DE STOCK : '${alert?.articleName ?? ''}' est épuisé !`
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
    this.notification.confirm('Annuler cette commande ?').then(ok => { if (!ok) return;
    this.commandeVenteService.annulerCommande(id).subscribe({
      next: () => {
        this.loadCommandes();
        this.notification.success('✅ Commande annulée.');
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
        this.notification.success('✅ Commande supprimée.');
      },
      error: (err) => {
        console.error('❌ Erreur suppression:', err);
        const errorMessage = err?.error?.message || 'Erreur lors de la suppression';
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
      case 'CONFIRMEE':  return 'Confirmée';
      case 'LIVREE':     return 'Livrée';
      case 'ANNULEE':    return 'Annulée';
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

  exportCSV(): void {
    const headers = ['Référence', 'Date Commande', 'Date Livraison Prévue', 'Client', 'Statut', 'Montant HT', 'Montant TVA', 'Montant TTC'];
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
