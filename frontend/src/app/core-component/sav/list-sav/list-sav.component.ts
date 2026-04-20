import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterOutlet } from '@angular/router';
import { SavService } from '../../../core/service/sav/sav.service';
import { Sav } from '../../../model/sav.model';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-list-sav',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet],
  templateUrl: './list-sav.component.html',
  styleUrl: './list-sav.component.scss',
})
export class ListSavComponent implements OnInit {
  tickets: Sav[] = [];
  filtered: Sav[] = [];
  isLoading = false;
  errorMessage = '';

  searchTerm = '';
  selectedStatut = '';
  selectedType = '';
  selectedPriorite = '';

  statuts = ['NOUVEAU', 'EN_COURS', 'EN_ATTENTE_PIECE', 'RESOLU', 'CLOTURE', 'ANNULE'];
  types = ['GARANTIE', 'REPARATION', 'RETOUR', 'ECHANGE', 'RECLAMATION'];
  priorites = ['BASSE', 'NORMALE', 'HAUTE', 'URGENTE'];

  // KPI
  kpi = { total: 0, ouverts: 0, enCours: 0, resolus: 0, urgent: 0 };

  constructor(
    private savService: SavService,
    private router: Router,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.savService.getAll().subscribe({
      next: (data) => {
        this.tickets = data || [];
        this.applyFilters();
        this.computeKpi();
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Erreur lors du chargement des tickets SAV';
        this.isLoading = false;
      },
    });
  }

  applyFilters(): void {
    const q = this.searchTerm.trim().toLowerCase();
    this.filtered = this.tickets.filter((t) => {
      const matchSearch =
        !q ||
        t.reference?.toLowerCase().includes(q) ||
        t.sujet?.toLowerCase().includes(q) ||
        t.client?.nom?.toLowerCase().includes(q) ||
        t.client?.prenom?.toLowerCase().includes(q);
      const matchStatut = !this.selectedStatut || t.statut === this.selectedStatut;
      const matchType = !this.selectedType || t.type === this.selectedType;
      const matchPriorite = !this.selectedPriorite || t.priorite === this.selectedPriorite;
      return matchSearch && matchStatut && matchType && matchPriorite;
    });
  }

  computeKpi(): void {
    this.kpi = {
      total: this.tickets.length,
      ouverts: this.tickets.filter((t) => t.statut === 'NOUVEAU').length,
      enCours: this.tickets.filter(
        (t) => t.statut === 'EN_COURS' || t.statut === 'EN_ATTENTE_PIECE'
      ).length,
      resolus: this.tickets.filter(
        (t) => t.statut === 'RESOLU' || t.statut === 'CLOTURE'
      ).length,
      urgent: this.tickets.filter((t) => t.priorite === 'URGENTE').length,
    };
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.selectedStatut = '';
    this.selectedType = '';
    this.selectedPriorite = '';
    this.applyFilters();
  }

  addTicket(): void {
    this.router.navigate(['/sav/add']);
  }

  editTicket(id?: number): void {
    if (id) this.router.navigate(['/sav/edit', id]);
  }

  openDetail(id?: number): void {
    if (id) this.router.navigate(['/sav/detail', id]);
  }

  deleteTicket(id?: number): void {
    if (!id) return;
    this.notification.confirm('Supprimer ce ticket SAV ?').then((ok) => {
      if (!ok) return;
      this.savService.delete(id).subscribe({
        next: () => {
          this.notification.success('✅ Ticket supprimé.');
          this.load();
        },
        error: (err) => {
          const msg = err?.error?.message || 'Erreur lors de la suppression';
          this.notification.error('❌ ' + msg);
        },
      });
    });
  }

  quickTransition(t: Sav, statut: string, label: string): void {
    if (!t.id) return;
    this.notification.confirm(`Passer ce ticket au statut "${label}" ?`).then((ok) => {
      if (!ok) return;
      this.savService.changeStatut(t.id!, statut).subscribe({
        next: () => {
          this.notification.success(`Statut mis à jour : ${label}`);
          this.load();
        },
        error: (err) => {
          const msg = err?.error?.message || 'Erreur lors du changement de statut';
          this.notification.error('❌ ' + msg);
        },
      });
    });
  }

  canStart(s?: string): boolean {
    return s === 'NOUVEAU';
  }
  canResolve(s?: string): boolean {
    return s === 'EN_COURS' || s === 'EN_ATTENTE_PIECE';
  }
  canClose(s?: string): boolean {
    return s === 'RESOLU';
  }
  canCancel(s?: string): boolean {
    return s !== 'CLOTURE' && s !== 'ANNULE';
  }
  isFinal(s?: string): boolean {
    return s === 'CLOTURE' || s === 'ANNULE';
  }

  // Label / class helpers
  statutLabel(s?: string): string {
    switch (s) {
      case 'NOUVEAU':
        return 'Nouveau';
      case 'EN_COURS':
        return 'En cours';
      case 'EN_ATTENTE_PIECE':
        return 'En attente pièce';
      case 'RESOLU':
        return 'Résolu';
      case 'CLOTURE':
        return 'Clôturé';
      case 'ANNULE':
        return 'Annulé';
      default:
        return s || '-';
    }
  }

  statutClass(s?: string): string {
    switch (s) {
      case 'NOUVEAU':
        return 'status-nouveau';
      case 'EN_COURS':
        return 'status-en-cours';
      case 'EN_ATTENTE_PIECE':
        return 'status-attente';
      case 'RESOLU':
        return 'status-resolu';
      case 'CLOTURE':
        return 'status-cloture';
      case 'ANNULE':
        return 'status-annule';
      default:
        return 'status-default';
    }
  }

  typeLabel(t?: string): string {
    switch (t) {
      case 'GARANTIE':
        return 'Garantie';
      case 'REPARATION':
        return 'Réparation';
      case 'RETOUR':
        return 'Retour';
      case 'ECHANGE':
        return 'Échange';
      case 'RECLAMATION':
        return 'Réclamation';
      default:
        return t || '-';
    }
  }

  typeIcon(t?: string): string {
    switch (t) {
      case 'GARANTIE':
        return 'shield';
      case 'REPARATION':
        return 'tool';
      case 'RETOUR':
        return 'rotate-ccw';
      case 'ECHANGE':
        return 'refresh-cw';
      case 'RECLAMATION':
        return 'alert-triangle';
      default:
        return 'tag';
    }
  }

  prioriteLabel(p?: string): string {
    switch (p) {
      case 'BASSE':
        return 'Basse';
      case 'NORMALE':
        return 'Normale';
      case 'HAUTE':
        return 'Haute';
      case 'URGENTE':
        return 'Urgente';
      default:
        return p || '-';
    }
  }

  prioriteClass(p?: string): string {
    switch (p) {
      case 'BASSE':
        return 'priority-basse';
      case 'NORMALE':
        return 'priority-normale';
      case 'HAUTE':
        return 'priority-haute';
      case 'URGENTE':
        return 'priority-urgente';
      default:
        return 'priority-default';
    }
  }
}
