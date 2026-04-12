import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommandeAchatService } from '../../../core/service/commande-achat/commande-achat.service';
import { CommandeAchat } from '../../../model/commande-achat.model';

@Component({
  selector: 'app-list-commande-achat',
  standalone: true,
  imports: [CommonModule, RouterOutlet, FormsModule],
  templateUrl: './list-commande-achat.component.html',
  styleUrl: './list-commande-achat.component.scss'
})
export class ListCommandeAchatComponent implements OnInit {
  commandes: CommandeAchat[] = [];
  filteredCommandes: CommandeAchat[] = [];
  isLoading = false;
  searchTerm = '';
  selectedStatut = '';
  lastGeneratedFacture: any = null;
  showFactureModal = false;
  errorMessage = '';

  statuts = ['EN_ATTENTE', 'CONFIRMEE', 'RECUE', 'ANNULEE'];

  constructor(
    private commandeAchatService: CommandeAchatService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCommandes();
  }

  loadCommandes(): void {
    this.isLoading = true;
    this.commandeAchatService.getAllCommandes().subscribe({
      next: (data) => {
        this.commandes = data;
        this.applyFilters();
        this.isLoading = false;
        console.log('✅ Commandes loaded:', data);
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
        c.fournisseur?.nom?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchStatut = !this.selectedStatut || c.statut === this.selectedStatut;
      return matchSearch && matchStatut;
    });
  }

  addCommande(): void {
    this.router.navigate(['/commande-achat/add']);
  }

  editCommande(id: number): void {
    this.router.navigate(['/commande-achat/edit', id]);
  }

  validerCommande(id: number): void {
    if (!confirm('Valider cette commande ? Une facture achat sera automatiquement générée et le stock mis à jour.')) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.commandeAchatService.validerCommande(id).subscribe({
      next: (facture) => {
        this.lastGeneratedFacture = facture;
        this.showFactureModal = true;
        this.loadCommandes();
        console.log('✅ Commande validée ! Facture achat générée:', facture);
        alert('✅ Commande validée ! Facture achat générée : ' + facture.referance + '. Le stock a été mis à jour.');
      },
      error: (err) => {
        this.isLoading = false;
        console.error('❌ Erreur validation:', err);
        
        // Extract detailed error message from backend
        const errorMessage = err?.error?.message || 
                           err?.message || 
                           'Erreur lors de la validation';
        
        this.errorMessage = errorMessage;
        console.error('Message d\'erreur détaillé:', errorMessage);
        alert('❌ Erreur lors de la validation:\n\n' + errorMessage);
      }
    });
  }

  recevoirCommande(id: number): void {
    if (!confirm('Marquer cette commande comme reçue ? Le stock sera automatiquement mis à jour.')) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.commandeAchatService.recevoirCommande(id).subscribe({
      next: (bon) => {
        this.loadCommandes();
        this.isLoading = false;
        alert('✅ Commande reçue ! Bon de réception créé : ' + bon.reference + '. Le stock a été mis à jour.');
      },
      error: (err) => {
        this.isLoading = false;
        const errorMessage = err?.error?.message || err?.message || 'Erreur lors de la réception';
        this.errorMessage = errorMessage;
        alert('❌ Erreur lors de la réception:\n\n' + errorMessage);
      }
    });
  }

  annulerCommande(id: number): void {
    if (!confirm('Annuler cette commande ?')) return;
    
    this.commandeAchatService.annulerCommande(id).subscribe({
      next: () => {
        this.loadCommandes();
        alert('✅ Commande annulée.');
      },
      error: (err) => {
        console.error('❌ Erreur annulation:', err);
        const errorMessage = err?.error?.message || 'Erreur lors de l\'annulation';
        alert('❌ ' + errorMessage);
      }
    });
  }

  deleteCommande(id: number): void {
    if (!confirm('Supprimer cette commande ?')) return;
    
    this.commandeAchatService.deleteCommande(id).subscribe({
      next: () => {
        this.loadCommandes();
        alert('✅ Commande supprimée.');
      },
      error: (err) => {
        console.error('❌ Erreur suppression:', err);
        const errorMessage = err?.error?.message || 'Erreur lors de la suppression';
        alert('❌ ' + errorMessage);
      }
    });
  }

  getStatutBadgeClass(statut?: string): string {
    switch (statut) {
      case 'CONFIRMEE': return 'badge bg-success';
      case 'RECUE': return 'badge bg-info';
      case 'ANNULEE': return 'badge bg-danger';
      case 'EN_ATTENTE': return 'badge bg-warning text-dark';
      default: return 'badge bg-secondary';
    }
  }

  closeFactureModal(): void {
    this.showFactureModal = false;
    this.lastGeneratedFacture = null;
  }
}