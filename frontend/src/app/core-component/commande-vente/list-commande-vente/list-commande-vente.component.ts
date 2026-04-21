import { Component, OnInit } from '@angular/core';
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
  lastGeneratedFacture: any = null;
  showFactureModal = false;
  errorMessage = '';

  statuts = ['BROUILLON', 'EN_ATTENTE', 'CONFIRMEE', 'LIVREE', 'ANNULEE'];

  constructor(private commandeVenteService: CommandeVenteService,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.loadCommandes();
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
    this.filteredCommandes = this.commandes.filter(c => {
      const matchSearch = !this.searchTerm ||
        c.reference?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        c.client?.nom?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchStatut = !this.selectedStatut || c.statut === this.selectedStatut;
      return matchSearch && matchStatut;
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
      next: (facture) => {
        this.lastGeneratedFacture = facture;
        this.showFactureModal = true;
        this.loadCommandes();
        this.notification.success('✅ Commande validée ! Facture vente générée : ' + facture.referance);
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
      next: (bon) => {
        this.loadCommandes();
        this.notification.success('✅ Commande livrée ! Bon de livraison créé : ' + bon.reference + '. Le stock a été mis à jour.');
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
