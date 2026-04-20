import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SavService } from '../../../core/service/sav/sav.service';
import { Sav, SavIntervention } from '../../../model/sav.model';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-sav-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './sav-detail.component.html',
  styleUrl: './sav-detail.component.scss',
})
export class SavDetailComponent implements OnInit {
  id!: number;
  ticket: Sav | null = null;
  interventions: SavIntervention[] = [];
  isLoading = false;

  // New intervention form
  newComment = '';
  commentLoading = false;

  // Status change dialog
  showStatusDialog = false;
  statusTarget = '';
  statusComment = '';
  statusLoading = false;

  allStatuts = [
    { value: 'NOUVEAU', label: 'Nouveau' },
    { value: 'EN_COURS', label: 'En cours' },
    { value: 'EN_ATTENTE_PIECE', label: 'En attente pièce' },
    { value: 'RESOLU', label: 'Résolu' },
    { value: 'CLOTURE', label: 'Clôturé' },
    { value: 'ANNULE', label: 'Annulé' },
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private savService: SavService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.params['id']);
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.savService.getById(this.id).subscribe({
      next: (t) => {
        this.ticket = t;
        this.loadInterventions();
      },
      error: () => {
        this.notification.error('Ticket introuvable');
        this.isLoading = false;
        this.router.navigate(['/sav/list']);
      },
    });
  }

  loadInterventions(): void {
    this.savService.getInterventions(this.id).subscribe({
      next: (list) => {
        this.interventions = list || [];
        this.isLoading = false;
      },
      error: () => {
        this.interventions = [];
        this.isLoading = false;
      },
    });
  }

  addComment(): void {
    const comment = this.newComment.trim();
    if (!comment) {
      this.notification.warning('Saisissez un commentaire');
      return;
    }
    this.commentLoading = true;
    this.savService
      .addIntervention(this.id, { type: 'COMMENTAIRE', commentaire: comment })
      .subscribe({
        next: () => {
          this.newComment = '';
          this.commentLoading = false;
          this.notification.success('Commentaire ajouté');
          this.loadInterventions();
        },
        error: (err) => {
          const msg = err?.error?.message || 'Erreur lors de l\'ajout du commentaire';
          this.notification.error('❌ ' + msg);
          this.commentLoading = false;
        },
      });
  }

  openStatusDialog(target: string): void {
    this.statusTarget = target;
    this.statusComment = '';
    this.showStatusDialog = true;
  }

  closeStatusDialog(): void {
    this.showStatusDialog = false;
    this.statusTarget = '';
    this.statusComment = '';
  }

  confirmStatusChange(): void {
    if (!this.statusTarget) return;
    this.statusLoading = true;
    this.savService.changeStatut(this.id, this.statusTarget, this.statusComment).subscribe({
      next: () => {
        this.notification.success('Statut mis à jour');
        this.statusLoading = false;
        this.showStatusDialog = false;
        this.load();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors du changement de statut';
        this.notification.error('❌ ' + msg);
        this.statusLoading = false;
      },
    });
  }

  edit(): void {
    this.router.navigate(['/sav/edit', this.id]);
  }

  back(): void {
    this.router.navigate(['/sav/list']);
  }

  // --- Helpers ---

  statutLabel(s?: string): string {
    return this.allStatuts.find((x) => x.value === s)?.label || s || '-';
  }

  statutClass(s?: string): string {
    switch (s) {
      case 'NOUVEAU':          return 'status-nouveau';
      case 'EN_COURS':         return 'status-en-cours';
      case 'EN_ATTENTE_PIECE': return 'status-attente';
      case 'RESOLU':           return 'status-resolu';
      case 'CLOTURE':          return 'status-cloture';
      case 'ANNULE':           return 'status-annule';
      default:                 return 'status-default';
    }
  }

  typeLabel(t?: string): string {
    switch (t) {
      case 'GARANTIE':    return 'Garantie';
      case 'REPARATION':  return 'Réparation';
      case 'RETOUR':      return 'Retour';
      case 'ECHANGE':     return 'Échange';
      case 'RECLAMATION': return 'Réclamation';
      default:            return t || '-';
    }
  }

  typeIcon(t?: string): string {
    switch (t) {
      case 'GARANTIE':    return 'shield';
      case 'REPARATION':  return 'tool';
      case 'RETOUR':      return 'rotate-ccw';
      case 'ECHANGE':     return 'refresh-cw';
      case 'RECLAMATION': return 'alert-triangle';
      default:            return 'tag';
    }
  }

  prioriteLabel(p?: string): string {
    switch (p) {
      case 'BASSE':    return 'Basse';
      case 'NORMALE':  return 'Normale';
      case 'HAUTE':    return 'Haute';
      case 'URGENTE':  return 'Urgente';
      default:         return p || '-';
    }
  }

  prioriteClass(p?: string): string {
    switch (p) {
      case 'BASSE':    return 'priority-basse';
      case 'NORMALE':  return 'priority-normale';
      case 'HAUTE':    return 'priority-haute';
      case 'URGENTE':  return 'priority-urgente';
      default:         return 'priority-default';
    }
  }

  interventionIcon(type?: string): string {
    switch (type) {
      case 'CHANGEMENT_STATUT': return 'activity';
      case 'INTERVENTION':      return 'tool';
      case 'PIECE':             return 'package';
      case 'CLOTURE':           return 'lock';
      case 'COMMENTAIRE':
      default:                  return 'message-square';
    }
  }

  interventionLabel(type?: string): string {
    switch (type) {
      case 'CHANGEMENT_STATUT': return 'Changement de statut';
      case 'INTERVENTION':      return 'Intervention';
      case 'PIECE':             return 'Pièce';
      case 'CLOTURE':           return 'Clôture';
      case 'COMMENTAIRE':       return 'Commentaire';
      default:                  return type || 'Événement';
    }
  }

  canTransitionTo(target: string): boolean {
    const current = this.ticket?.statut;
    if (!current) return false;
    if (current === target) return false;
    if (current === 'CLOTURE') return false; // final
    if (current === 'ANNULE' && target !== 'NOUVEAU') return false;
    return true;
  }

  trackInt(_: number, it: SavIntervention): number {
    return it.id || 0;
  }
}
