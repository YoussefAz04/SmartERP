import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { catchError, forkJoin, Observable, of } from 'rxjs';
import { ArticleVenteService } from '../../core/service/article-vente/article-vente.service';
import { ClientService } from '../../core/service/client/client.service';
import { SavService } from '../../core/service/sav/sav.service';
import { ArticleVente } from '../../model/article-vente.model';
import { Client } from '../../model/client.model';
import {
  SavAgent,
  SavApprovalStatus,
  SavHistory,
  SavIntervention,
  SavInterventionRequest,
  SavPriority,
  SavStats,
  SavStatus,
  SavTicket,
  SavTicketRequest,
  SavType,
  SavResolutionType,
  SavSaleItem
} from '../../model/sav-ticket.model';

@Component({
  selector: 'app-sav',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './sav.component.html',
  styleUrl: './sav.component.scss'
})
export class SavComponent implements OnInit {
  tickets: SavTicket[] = [];
  filteredTickets: SavTicket[] = [];
  clients: Client[] = [];
  articlesVente: ArticleVente[] = [];
  agents: SavAgent[] = [];
  history: SavHistory[] = [];
  interventions: SavIntervention[] = [];
  eligibleSales: SavSaleItem[] = [];

  isLoading = true;
  isSaving = false;
  isHistoryLoading = false;
  isAddingNote = false;
  isAddingIntervention = false;
  isSalesLoading = false;
  errorMessage = '';
  searchText = '';
  selectedStatus = '';
  selectedPriority = '';
  selectedType = '';
  selectedAgent = '';
  showForm = false;
  editingTicket: SavTicket | null = null;
  selectedTicket: SavTicket | null = null;
  noteText = '';
  selectedSaleKey = '';

  stats: SavStats = this.emptyStats();

  readonly statuses: SavStatus[] = [
    'OUVERT', 'QUALIFICATION', 'EN_ATTENTE_PRODUIT', 'DIAGNOSTIC', 'EN_ATTENTE_ACCORD',
    'EN_REPARATION', 'PRET_RESTITUTION', 'EN_ATTENTE_CLIENT', 'RESOLU', 'FERME', 'ANNULE'
  ];
  readonly priorities: SavPriority[] = ['BASSE', 'NORMALE', 'HAUTE', 'URGENTE'];
  readonly types: SavType[] = ['GARANTIE', 'REPARATION', 'RETOUR', 'ECHANGE', 'SUPPORT', 'AUTRE'];
  readonly contactChannels = ['PHONE', 'EMAIL', 'STORE', 'WEB'];
  readonly resolutionTypes: SavResolutionType[] = [
    'REPARATION', 'ECHANGE', 'REMBOURSEMENT', 'AVOIR', 'RETOUR_SANS_ACTION', 'REFUS_GARANTIE',
    'ASSISTANCE_RESOLUE', 'AUCUN_DEFAUT'
  ];

  interventionForm: SavInterventionRequest = this.createEmptyIntervention();

  form: SavTicketRequest = this.createEmptyForm();

  constructor(
    private savService: SavService,
    private clientService: ClientService,
    private articleVenteService: ArticleVenteService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';
    let hasPartialFailure = false;
    const safe = <T>(source: Observable<T>, fallback: T): Observable<T> => source.pipe(
      catchError(() => {
        hasPartialFailure = true;
        return of(fallback);
      })
    );
    forkJoin({
      tickets: safe(this.savService.getTickets(), [] as SavTicket[]),
      stats: safe(this.savService.getStats(), this.emptyStats()),
      agents: safe(this.savService.getAgents(), [] as SavAgent[]),
      clients: safe(this.clientService.getAllClients(), [] as Client[]),
      articlesVente: safe(this.articleVenteService.getAllArticlesVente(), [] as ArticleVente[])
    }).subscribe({
      next: ({ tickets, stats, agents, clients, articlesVente }) => {
        this.tickets = Array.isArray(tickets) ? tickets : [];
        this.stats = stats || this.emptyStats();
        this.agents = Array.isArray(agents) ? agents : [];
        this.clients = Array.isArray(clients) ? clients : [];
        this.articlesVente = Array.isArray(articlesVente) ? articlesVente : [];
        this.applyFilters();
        if (hasPartialFailure) this.errorMessage = 'sav.errors.partialLoad';
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'sav.errors.load';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    const query = this.searchText.trim().toLowerCase();
    this.filteredTickets = this.tickets.filter(ticket => {
      const matchesStatus = !this.selectedStatus || ticket.status === this.selectedStatus;
      const matchesPriority = !this.selectedPriority || ticket.priority === this.selectedPriority;
      const matchesType = !this.selectedType || ticket.type === this.selectedType;
      const matchesAgent = !this.selectedAgent
        || (this.selectedAgent === 'UNASSIGNED' ? !ticket.assignedToUserId : ticket.assignedToUserId === Number(this.selectedAgent));
      const searchable = [
        ticket.reference,
        ticket.subject,
        ticket.clientName,
        ticket.articleVenteName,
        ticket.serialNumber,
        ticket.assignedToUsername,
        ticket.description
      ].filter(Boolean).join(' ').toLowerCase();
      return matchesStatus && matchesPriority && matchesType && matchesAgent && (!query || searchable.includes(query));
    });
  }

  resetFilters(): void {
    this.searchText = '';
    this.selectedStatus = '';
    this.selectedPriority = '';
    this.selectedType = '';
    this.selectedAgent = '';
    this.applyFilters();
  }

  openCreateForm(): void {
    this.editingTicket = null;
    this.form = this.createEmptyForm();
    this.showForm = true;
  }

  openEditForm(ticket: SavTicket): void {
    this.editingTicket = ticket;
    this.form = {
      subject: ticket.subject,
      description: ticket.description,
      diagnostic: ticket.diagnostic,
      resolution: ticket.resolution,
      status: ticket.status,
      priority: ticket.priority,
      type: ticket.type,
      estimatedCost: ticket.estimatedCost,
      actualCost: ticket.actualCost,
      serialNumber: ticket.serialNumber,
      purchaseDate: ticket.purchaseDate,
      warrantyMonths: ticket.warrantyMonths,
      contactChannel: ticket.contactChannel || 'PHONE',
      approvalStatus: ticket.approvalStatus,
      resolutionType: ticket.resolutionType,
      dueDate: ticket.dueDate,
      clientId: ticket.clientId || null,
      articleVenteId: ticket.articleVenteId || null,
      commandeVenteId: ticket.commandeVenteId || null,
      assignedToUserId: ticket.assignedToUserId || null
    };
    this.selectedSaleKey = ticket.commandeVenteId && ticket.articleVenteId
      ? `${ticket.commandeVenteId}:${ticket.articleVenteId}` : '';
    if (ticket.clientId) this.loadEligibleSales(ticket.clientId);
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingTicket = null;
    this.form = this.createEmptyForm();
    this.eligibleSales = [];
    this.selectedSaleKey = '';
  }

  saveTicket(): void {
    if (!this.form.subject?.trim()) {
      this.errorMessage = 'sav.errors.subjectRequired';
      return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    const request = this.normalizeRequest(this.form);
    const save$ = this.editingTicket?.id
      ? this.savService.updateTicket(this.editingTicket.id, request)
      : this.savService.createTicket(request);

    save$.subscribe({
      next: () => {
        this.isSaving = false;
        this.closeForm();
        this.loadData();
      },
      error: error => {
        this.handleApiError(error, 'sav.errors.save');
        this.isSaving = false;
      }
    });
  }

  changeStatus(ticket: SavTicket, status: SavStatus): void {
    if (!ticket.id || ticket.status === status) return;
    this.savService.updateStatus(ticket.id, status).subscribe({
      next: updated => {
        this.replaceTicket(updated);
        if (this.selectedTicket?.id === updated.id) {
          this.selectedTicket = updated;
          this.loadOperationalData(updated.id!);
        }
        this.refreshStats();
      },
      error: error => this.handleApiError(error, 'sav.errors.transition')
    });
  }

  openDetails(ticket: SavTicket): void {
    this.selectedTicket = ticket;
    this.noteText = '';
    this.interventionForm = this.createEmptyIntervention(ticket.assignedToUserId);
    if (ticket.id) this.loadOperationalData(ticket.id);
  }

  closeDetails(): void {
    this.selectedTicket = null;
    this.history = [];
    this.interventions = [];
    this.noteText = '';
  }

  loadHistory(ticketId: number): void {
    this.isHistoryLoading = true;
    this.savService.getHistory(ticketId).subscribe({
      next: history => {
        this.history = Array.isArray(history) ? history : [];
        this.isHistoryLoading = false;
      },
      error: () => {
        this.errorMessage = 'sav.errors.history';
        this.isHistoryLoading = false;
      }
    });
  }

  loadOperationalData(ticketId: number): void {
    this.isHistoryLoading = true;
    forkJoin({
      history: this.savService.getHistory(ticketId).pipe(catchError(() => of([] as SavHistory[]))),
      interventions: this.savService.getInterventions(ticketId).pipe(catchError(() => of([] as SavIntervention[])))
    }).subscribe({
      next: ({ history, interventions }) => {
        this.history = Array.isArray(history) ? history : [];
        this.interventions = Array.isArray(interventions) ? interventions : [];
        this.isHistoryLoading = false;
      },
      error: () => {
        this.errorMessage = 'sav.errors.history';
        this.isHistoryLoading = false;
      }
    });
  }

  addIntervention(): void {
    if (!this.selectedTicket?.id || !this.interventionForm.description.trim()
        || this.interventionForm.durationMinutes <= 0) return;
    this.isAddingIntervention = true;
    this.savService.addIntervention(this.selectedTicket.id, {
      ...this.interventionForm,
      durationMinutes: Number(this.interventionForm.durationMinutes),
      laborCost: Number(this.interventionForm.laborCost || 0),
      partsCost: Number(this.interventionForm.partsCost || 0),
      technicianUserId: this.interventionForm.technicianUserId
        ? Number(this.interventionForm.technicianUserId) : null
    }).subscribe({
      next: intervention => {
        this.interventions = [intervention, ...this.interventions];
        this.interventionForm = this.createEmptyIntervention(this.selectedTicket?.assignedToUserId);
        this.isAddingIntervention = false;
        this.refreshSelectedTicket();
        this.loadHistory(this.selectedTicket!.id!);
      },
      error: error => {
        this.handleApiError(error, 'sav.errors.intervention');
        this.isAddingIntervention = false;
      }
    });
  }

  decideApproval(approvalStatus: SavApprovalStatus): void {
    if (!this.selectedTicket?.id) return;
    this.savService.updateApproval(this.selectedTicket.id, approvalStatus).subscribe({
      next: ticket => {
        this.replaceTicket(ticket);
        this.selectedTicket = ticket;
        this.loadOperationalData(ticket.id!);
        this.refreshStats();
      },
      error: error => this.handleApiError(error, 'sav.errors.approval')
    });
  }

  addNote(): void {
    if (!this.selectedTicket?.id || !this.noteText.trim()) return;
    this.isAddingNote = true;
    this.savService.addNote(this.selectedTicket.id, this.noteText.trim()).subscribe({
      next: note => {
        this.history = [note, ...this.history];
        this.noteText = '';
        this.isAddingNote = false;
      },
      error: error => {
        this.handleApiError(error, 'sav.errors.note');
        this.isAddingNote = false;
      }
    });
  }

  deleteTicket(ticket: SavTicket): void {
    if (!ticket.id) return;
    const message = this.translate.instant('sav.confirmDelete', { reference: ticket.reference || '' });
    if (!confirm(message)) return;
    this.savService.deleteTicket(ticket.id).subscribe({
      next: () => {
        this.tickets = this.tickets.filter(item => item.id !== ticket.id);
        if (this.selectedTicket?.id === ticket.id) this.closeDetails();
        this.applyFilters();
        this.refreshStats();
      },
      error: error => this.handleApiError(error, 'sav.errors.delete')
    });
  }

  refreshStats(): void {
    this.savService.getStats().subscribe({ next: stats => this.stats = stats });
  }

  getAvailableStatuses(ticket: SavTicket): SavStatus[] {
    const transitions: Record<SavStatus, SavStatus[]> = {
      OUVERT: ['OUVERT', 'QUALIFICATION', 'ANNULE'],
      QUALIFICATION: ['QUALIFICATION', 'EN_ATTENTE_PRODUIT', 'DIAGNOSTIC', 'EN_ATTENTE_CLIENT', 'ANNULE'],
      EN_ATTENTE_PRODUIT: ['EN_ATTENTE_PRODUIT', 'DIAGNOSTIC', 'ANNULE'],
      DIAGNOSTIC: ['DIAGNOSTIC', 'EN_ATTENTE_ACCORD', 'EN_REPARATION', 'EN_ATTENTE_CLIENT', 'RESOLU', 'ANNULE'],
      EN_ATTENTE_ACCORD: ['EN_ATTENTE_ACCORD', 'EN_REPARATION', 'ANNULE'],
      EN_REPARATION: ['EN_REPARATION', 'PRET_RESTITUTION', 'EN_ATTENTE_CLIENT', 'ANNULE'],
      PRET_RESTITUTION: ['PRET_RESTITUTION', 'RESOLU', 'EN_REPARATION'],
      EN_COURS: ['EN_COURS', 'DIAGNOSTIC', 'EN_REPARATION', 'EN_ATTENTE_CLIENT', 'RESOLU', 'ANNULE'],
      EN_ATTENTE_CLIENT: ['EN_ATTENTE_CLIENT', 'QUALIFICATION', 'DIAGNOSTIC', 'EN_REPARATION', 'RESOLU', 'ANNULE'],
      RESOLU: ['RESOLU', 'FERME', 'DIAGNOSTIC'],
      FERME: ['FERME', 'DIAGNOSTIC'],
      ANNULE: ['ANNULE', 'OUVERT']
    };
    return transitions[ticket.status];
  }

  get filteredArticles(): ArticleVente[] {
    if (!this.form.clientId) return this.articlesVente;
    const clientId = Number(this.form.clientId);
    return this.articlesVente.filter(article => !article.client?.id || article.client.id === clientId);
  }

  onPriorityChange(): void {
    if (!this.editingTicket && !this.form.dueDate) {
      const days: Record<SavPriority, number> = { URGENTE: 1, HAUTE: 2, NORMALE: 5, BASSE: 10 };
      const date = new Date();
      date.setDate(date.getDate() + days[this.form.priority || 'NORMALE']);
      this.form.dueDate = date.toISOString().slice(0, 10);
    }
  }

  onClientChange(): void {
    this.form.articleVenteId = null;
    this.form.commandeVenteId = null;
    this.selectedSaleKey = '';
    this.eligibleSales = [];
    if (this.form.clientId) this.loadEligibleSales(Number(this.form.clientId));
  }

  loadEligibleSales(clientId: number): void {
    this.isSalesLoading = true;
    this.savService.getEligibleSales(clientId).subscribe({
      next: sales => {
        this.eligibleSales = Array.isArray(sales) ? sales : [];
        this.isSalesLoading = false;
      },
      error: () => {
        this.eligibleSales = [];
        this.isSalesLoading = false;
      }
    });
  }

  onSaleChange(): void {
    const [orderId, articleId] = this.selectedSaleKey.split(':').map(Number);
    const sale = this.eligibleSales.find(item => item.orderId === orderId && item.articleVenteId === articleId);
    this.form.commandeVenteId = sale?.orderId || null;
    this.form.articleVenteId = sale?.articleVenteId || null;
    if (sale?.orderDate) this.form.purchaseDate = sale.orderDate;
  }

  getPrimaryActions(ticket: SavTicket): { status: SavStatus; key: string; icon: string }[] {
    switch (ticket.status) {
      case 'OUVERT': return [{ status: 'QUALIFICATION', key: 'qualify', icon: 'check-square' }];
      case 'QUALIFICATION':
        return ticket.type === 'SUPPORT' || ticket.type === 'AUTRE'
          ? [{ status: 'DIAGNOSTIC', key: 'startDiagnostic', icon: 'search' }]
          : [{ status: 'EN_ATTENTE_PRODUIT', key: 'awaitProduct', icon: 'package' }];
      case 'EN_ATTENTE_PRODUIT': return [{ status: 'DIAGNOSTIC', key: 'productReceived', icon: 'download' }];
      case 'DIAGNOSTIC':
        if (ticket.type === 'SUPPORT') return [{ status: 'RESOLU', key: 'resolve', icon: 'check-circle' }];
        return ticket.underWarranty
          ? [{ status: 'EN_REPARATION', key: 'startRepair', icon: 'tool' }]
          : [{ status: 'EN_ATTENTE_ACCORD', key: 'requestApproval', icon: 'send' }];
      case 'EN_REPARATION': return [{ status: 'PRET_RESTITUTION', key: 'readyForReturn', icon: 'package' }];
      case 'PRET_RESTITUTION': return [{ status: 'RESOLU', key: 'returnProduct', icon: 'corner-up-left' }];
      case 'RESOLU': return [{ status: 'FERME', key: 'closeTicket', icon: 'archive' }];
      case 'EN_ATTENTE_CLIENT': return [{ status: 'QUALIFICATION', key: 'customerReplied', icon: 'message-circle' }];
      default: return [];
    }
  }

  getActionRequirement(ticket: SavTicket, target: SavStatus): string | null {
    if (target === 'QUALIFICATION' && (!ticket.clientId || !ticket.description?.trim())) {
      return 'sav.workflow.requirements.customerRequest';
    }
    if (target === 'DIAGNOSTIC' && !ticket.assignedToUserId) {
      return 'sav.workflow.requirements.technician';
    }
    if (target === 'EN_ATTENTE_ACCORD' && (!ticket.diagnostic?.trim() || ticket.estimatedCost == null)) {
      return 'sav.workflow.requirements.diagnosticEstimate';
    }
    if (target === 'EN_REPARATION' && !ticket.diagnostic?.trim()) {
      return 'sav.workflow.requirements.diagnostic';
    }
    if (target === 'PRET_RESTITUTION') {
      if (!ticket.resolution?.trim() || !ticket.resolutionType) return 'sav.workflow.requirements.resolution';
      if (ticket.type === 'REPARATION' && this.interventions.length === 0) return 'sav.workflow.requirements.intervention';
    }
    if (target === 'RESOLU' && ticket.status !== 'PRET_RESTITUTION'
        && (!ticket.resolution?.trim() || !ticket.resolutionType)) {
      return 'sav.workflow.requirements.resolution';
    }
    return null;
  }

  isInterventionStage(ticket: SavTicket): boolean {
    return ['DIAGNOSTIC', 'EN_REPARATION', 'EN_COURS'].includes(ticket.status);
  }

  getPriorityClass(priority: SavPriority): string {
    return `sav-priority sav-priority--${priority.toLowerCase()}`;
  }

  getStatusClass(status: SavStatus): string {
    return `sav-status sav-status--${status.toLowerCase().replaceAll('_', '-')}`;
  }

  getHistoryIcon(type: string): string {
    return ({ CREATED: 'plus', UPDATED: 'edit-2', STATUS_CHANGED: 'repeat', ASSIGNED: 'user-check',
      NOTE: 'message-square', SALE_VERIFIED: 'shopping-bag', APPROVAL: 'check-square', INTERVENTION: 'tool' } as Record<string, string>)[type] || 'circle';
  }

  getClientName(client: Client): string {
    return `${client.prenom || ''} ${client.nom || ''}`.trim() || client.email;
  }

  trackById(_: number, ticket: SavTicket): number | undefined {
    return ticket.id;
  }

  private replaceTicket(updated: SavTicket): void {
    this.tickets = this.tickets.map(item => item.id === updated.id ? updated : item);
    this.applyFilters();
  }

  private createEmptyForm(): SavTicketRequest {
    return {
      subject: '',
      description: '',
      diagnostic: '',
      resolution: '',
      status: 'OUVERT',
      priority: 'NORMALE',
      type: 'SUPPORT',
      estimatedCost: undefined,
      actualCost: undefined,
      serialNumber: '',
      purchaseDate: '',
      warrantyMonths: 12,
      contactChannel: 'PHONE',
      approvalStatus: 'NON_REQUIS',
      resolutionType: undefined,
      dueDate: '',
      clientId: null,
      articleVenteId: null,
      commandeVenteId: null,
      assignedToUserId: null
    };
  }

  private normalizeRequest(request: SavTicketRequest): SavTicketRequest {
    return {
      ...request,
      subject: request.subject.trim(),
      clientId: request.clientId ? Number(request.clientId) : null,
      articleVenteId: request.articleVenteId ? Number(request.articleVenteId) : null,
      commandeVenteId: request.commandeVenteId ? Number(request.commandeVenteId) : null,
      assignedToUserId: request.assignedToUserId ? Number(request.assignedToUserId) : null,
      estimatedCost: request.estimatedCost != null ? Number(request.estimatedCost) : undefined,
      actualCost: undefined,
      approvalStatus: undefined,
      warrantyMonths: request.warrantyMonths != null ? Number(request.warrantyMonths) : undefined,
      purchaseDate: request.purchaseDate || undefined,
      dueDate: request.dueDate || undefined
    };
  }

  private emptyStats(): SavStats {
    return {
      total: 0,
      open: 0,
      inProgress: 0,
      waitingCustomer: 0,
      resolved: 0,
      closed: 0,
      cancelled: 0,
      overdue: 0,
      unassigned: 0,
      actualCostTotal: 0
    };
  }

  private createEmptyIntervention(technicianUserId?: number): SavInterventionRequest {
    return {
      description: '',
      durationMinutes: 30,
      laborCost: 0,
      partsCost: 0,
      technicianUserId: technicianUserId || null
    };
  }

  private refreshSelectedTicket(): void {
    if (!this.selectedTicket?.id) return;
    this.savService.getTicket(this.selectedTicket.id).subscribe({
      next: ticket => {
        this.selectedTicket = ticket;
        this.replaceTicket(ticket);
        this.refreshStats();
      }
    });
  }

  private handleApiError(error: unknown, fallbackKey: string): void {
    if (error instanceof HttpErrorResponse) {
      const payload = error.error;
      const message = typeof payload === 'string'
        ? payload
        : payload && typeof payload.message === 'string' ? payload.message : '';
      this.errorMessage = message.trim() || fallbackKey;
      return;
    }
    this.errorMessage = fallbackKey;
  }
}
