import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommandeAchatService } from '../../../core/service/commande-achat/commande-achat.service';
import { CommandeAchat } from '../../../model/commande-achat.model';
import { NotificationService } from '../../../shared/notification/notification.service';

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
  dateDebut = '';
  dateFin = '';
  lastGeneratedFacture: any = null;
  showFactureModal = false;
  errorMessage = '';

  canCreate = false;
  canUpdate = false;
  canDelete = false;

  statuts = ['EN_ATTENTE', 'CONFIRMEE', 'RECUE', 'ANNULEE'];

  constructor(
    private commandeAchatService: CommandeAchatService,
    private router: Router,
    private notification: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

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
        const targetTitle = normalize('Commande Achat');

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
    const debut = this.dateDebut ? new Date(this.dateDebut) : null;
    const fin = this.dateFin ? new Date(new Date(this.dateFin).setHours(23, 59, 59, 999)) : null;

    this.filteredCommandes = this.commandes.filter(c => {
      const matchSearch = !this.searchTerm ||
        c.reference?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        c.fournisseur?.nom?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchStatut = !this.selectedStatut || c.statut === this.selectedStatut;
      const date = c.dateCommande ? new Date(c.dateCommande) : null;
      const matchDebut = !debut || (date !== null && date >= debut);
      const matchFin = !fin || (date !== null && date <= fin);
      return matchSearch && matchStatut && matchDebut && matchFin;
    });
  }

  addCommande(): void {
    this.router.navigate(['/commande-achat/add']);
  }

  editCommande(id: number): void {
    this.router.navigate(['/commande-achat/edit', id]);
  }

  validerCommande(id: number): void {
    this.notification.confirm('Valider cette commande ? Une facture achat sera automatiquement générée et le stock mis à jour.').then(ok => {
      if (!ok) return;

      this.isLoading = true;
      this.errorMessage = '';

      this.commandeAchatService.validerCommande(id).subscribe({
        next: (facture) => {
          this.lastGeneratedFacture = facture;
          this.showFactureModal = true;
          this.loadCommandes();
          console.log('✅ Commande validée ! Facture achat générée:', facture);
          this.notification.success('✅ Commande validée ! Facture achat générée : ' + facture.referance + '. Le stock a été mis à jour.');
        },
        error: (err) => {
          this.isLoading = false;
          console.error('❌ Erreur validation:', err);
          
          const errorMessage = err?.error?.message || 
                             err?.message || 
                             'Erreur lors de la validation';
          
          this.errorMessage = errorMessage;
          console.error('Message d\'erreur détaillé:', errorMessage);
          this.notification.error('❌ Erreur lors de la validation:\n\n' + errorMessage);
        }
      });
    });
  }

  recevoirCommande(id: number): void {
    this.notification.confirm('Marquer cette commande comme reçue ? Le stock sera automatiquement mis à jour.').then(ok => {
      if (!ok) return;

      this.isLoading = true;
      this.errorMessage = '';

      this.commandeAchatService.recevoirCommande(id).subscribe({
        next: (bon) => {
          this.loadCommandes();
          this.isLoading = false;
          this.notification.success('✅ Commande reçue ! Bon de réception créé : ' + bon.reference + '. Le stock a été mis à jour.');
        },
        error: (err) => {
          this.isLoading = false;
          const errorMessage = err?.error?.message || err?.message || 'Erreur lors de la réception';
          this.errorMessage = errorMessage;
          this.notification.error('❌ Erreur lors de la réception:\n\n' + errorMessage);
        }
      });
    });
  }

  // ✅ NEW: Set status to EN_ATTENTE
  setEnAttente(id: number): void {
    this.notification.confirm('Passer cette commande à "En Attente" ?').then(ok => {
      if (!ok) return;
      
      this.isLoading = true;
      this.errorMessage = '';

      this.commandeAchatService.changeStatutToEnAttente(id).subscribe({
        next: () => {
          this.loadCommandes();
          this.isLoading = false;
          this.notification.success('✅ Statut changé en "En Attente".');
        },
        error: (err) => {
          this.isLoading = false;
          console.error('❌ Erreur changement de statut:', err);
          const errorMessage = err?.error?.message || 'Erreur lors du changement de statut';
          this.errorMessage = errorMessage;
          this.notification.error('❌ ' + errorMessage);
        }
      });
    });
  }

  annulerCommande(id: number): void {
    this.notification.confirm('Annuler cette commande ?').then(ok => {
      if (!ok) return;
      
      this.isLoading = true;
      this.errorMessage = '';

      this.commandeAchatService.annulerCommande(id).subscribe({
        next: () => {
          this.loadCommandes();
          this.isLoading = false;
          this.notification.success('✅ Commande annulée.');
        },
        error: (err) => {
          this.isLoading = false;
          console.error('❌ Erreur annulation:', err);
          const errorMessage = err?.error?.message || 'Erreur lors de l\'annulation';
          this.errorMessage = errorMessage;
          this.notification.error('❌ ' + errorMessage);
        }
      });
    });
  }

  deleteCommande(id: number): void {
    this.notification.confirm('Supprimer cette commande ?').then(ok => {
      if (!ok) return;
      
      this.isLoading = true;
      this.errorMessage = '';

      this.commandeAchatService.deleteCommande(id).subscribe({
        next: () => {
          this.loadCommandes();
          this.isLoading = false;
          this.notification.success('✅ Commande supprimée.');
        },
        error: (err) => {
          this.isLoading = false;
          console.error('❌ Erreur suppression:', err);
          const errorMessage = err?.error?.message || 'Erreur lors de la suppression';
          this.errorMessage = errorMessage;
          this.notification.error('❌ ' + errorMessage);
        }
      });
    });
  }

  getStatutClass(statut?: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'status-badge status-en-attente';
      case 'CONFIRMEE':  return 'status-badge status-confirmee';
      case 'RECUE':      return 'status-badge status-recue';
      case 'ANNULEE':    return 'status-badge status-annulee';
      default:           return 'status-badge status-default';
    }
  }

  getStatutLabel(statut?: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'En attente';
      case 'CONFIRMEE':  return 'Confirmée';
      case 'RECUE':      return 'Reçue';
      case 'ANNULEE':    return 'Annulée';
      default:           return statut || '-';
    }
  }

  get totalMontantTTC(): number {
    return this.commandes.reduce((sum, c) => sum + (c.montantTTC || 0), 0);
  }

  get commandesEnAttente(): number {
    return this.commandes.filter(c => c.statut === 'EN_ATTENTE').length;
  }

  get commandesConfirmees(): number {
    return this.commandes.filter(c => c.statut === 'CONFIRMEE').length;
  }

  get commandesRecues(): number {
    return this.commandes.filter(c => c.statut === 'RECUE').length;
  }

  exportCSV(): void {
    const headers = ['Référence', 'Date Commande', 'Fournisseur', 'Statut', 'Montant HT', 'Montant TVA', 'Montant TTC'];
    const rows = this.filteredCommandes.map(c => [
      c.reference ?? '',
      c.dateCommande ?? '',
      c.fournisseur?.nom ?? '',
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
    link.download = `commandes-achat-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
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

  viewFacture(commandeId: number): void {
    this.router.navigate(['/facture/listFactureAchat']);
  }
}