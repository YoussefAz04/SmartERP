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

  statuts = ['EN_ATTENTE', 'VALIDEE', 'ANNULEE'];

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
      },
      error: (err) => {
        console.error('Erreur lors du chargement:', err);
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
    if (!confirm('Valider cette commande ? Une facture achat sera automatiquement générée.')) return;
    this.commandeAchatService.validerCommande(id).subscribe({
      next: (facture) => {
        this.lastGeneratedFacture = facture;
        this.showFactureModal = true;
        this.loadCommandes();
        alert('✅ Commande validée ! Facture achat générée : ' + facture.referance);
      },
      error: (err) => {
        console.error('Erreur validation:', err);
        alert('❌ Erreur lors de la validation.');
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
        console.error('Erreur annulation:', err);
        alert('❌ Erreur lors de l\'annulation.');
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
        console.error('Erreur suppression:', err);
        alert('❌ Erreur lors de la suppression.');
      }
    });
  }

  getStatutBadgeClass(statut?: string): string {
    switch (statut) {
      case 'VALIDEE': return 'badge bg-success';
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
