import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SavService } from '../../../core/service/sav/sav.service';
import { ClientService } from '../../../core/service/client/client.service';
import { ArticleVenteService } from '../../../core/service/article-vente/article-vente.service';
import { Client } from '../../../model/client.model';
import { ArticleVente } from '../../../model/article-vente.model';
import { NotificationService } from '../../../shared/notification/notification.service';
import { Sav } from '../../../model/sav.model';

@Component({
  selector: 'app-add-sav',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './add-sav.component.html',
  styleUrl: './add-sav.component.scss',
})
export class AddSavComponent implements OnInit {
  form!: FormGroup;
  isEditMode = false;
  isLoading = false;
  submitted = false;
  savId: number | null = null;

  clients: Client[] = [];
  articles: ArticleVente[] = [];

  types = [
    { value: 'GARANTIE', label: 'Garantie' },
    { value: 'REPARATION', label: 'Réparation' },
    { value: 'RETOUR', label: 'Retour produit' },
    { value: 'ECHANGE', label: 'Échange' },
    { value: 'RECLAMATION', label: 'Réclamation' },
  ];

  priorites = [
    { value: 'BASSE', label: 'Basse' },
    { value: 'NORMALE', label: 'Normale' },
    { value: 'HAUTE', label: 'Haute' },
    { value: 'URGENTE', label: 'Urgente' },
  ];

  statuts = [
    { value: 'NOUVEAU', label: 'Nouveau' },
    { value: 'EN_COURS', label: 'En cours' },
    { value: 'EN_ATTENTE_PIECE', label: 'En attente pièce' },
    { value: 'RESOLU', label: 'Résolu' },
    { value: 'CLOTURE', label: 'Clôturé' },
    { value: 'ANNULE', label: 'Annulé' },
  ];

  constructor(
    private fb: FormBuilder,
    private savService: SavService,
    private clientService: ClientService,
    private articleService: ArticleVenteService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.loadClients();
    this.loadArticles();

    const idParam = this.route.snapshot.params['id'];
    if (idParam) {
      this.isEditMode = true;
      this.savId = Number(idParam);
      this.loadExisting(this.savId);
    } else {
      this.form.patchValue({
        dateOuverture: new Date().toISOString().substring(0, 10),
      });
    }
  }

  buildForm(): void {
    this.form = this.fb.group({
      reference: [''],
      dateOuverture: ['', Validators.required],
      type: ['RECLAMATION', Validators.required],
      priorite: ['NORMALE', Validators.required],
      statut: ['NOUVEAU', Validators.required],
      sujet: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.required, Validators.maxLength(2000)]],
      resolution: [''],
      coutEstime: [null],
      coutReel: [null],
      clientId: [null, Validators.required],
      articleVenteId: [null],
    });
  }

  loadClients(): void {
    this.clientService.getAllClients().subscribe({
      next: (data) => (this.clients = data || []),
      error: () => (this.clients = []),
    });
  }

  loadArticles(): void {
    this.articleService.getAllArticlesVente().subscribe({
      next: (data) => (this.articles = data || []),
      error: () => (this.articles = []),
    });
  }

  loadExisting(id: number): void {
    this.isLoading = true;
    this.savService.getById(id).subscribe({
      next: (s) => {
        this.form.patchValue({
          reference: s.reference,
          dateOuverture: s.dateOuverture,
          type: s.type,
          priorite: s.priorite,
          statut: s.statut,
          sujet: s.sujet,
          description: s.description,
          resolution: s.resolution,
          coutEstime: s.coutEstime,
          coutReel: s.coutReel,
          clientId: s.client?.id,
          articleVenteId: s.articleVente?.id,
        });
        this.isLoading = false;
      },
      error: () => {
        this.notification.error('Impossible de charger le ticket');
        this.isLoading = false;
      },
    });
  }

  submit(): void {
    this.submitted = true;
    if (this.form.invalid) {
      this.notification.warning('Veuillez remplir les champs obligatoires');
      return;
    }

    const payload: Sav = { ...this.form.value };

    // Normalize empty strings to null for optional numeric/id fields
    if (!payload.articleVenteId) delete payload.articleVenteId;
    if (payload.coutEstime == null || (payload.coutEstime as unknown as string) === '') {
      delete payload.coutEstime;
    }
    if (payload.coutReel == null || (payload.coutReel as unknown as string) === '') {
      delete payload.coutReel;
    }
    if (!payload.reference) delete payload.reference;

    this.isLoading = true;
    const req = this.isEditMode && this.savId
      ? this.savService.update(this.savId, payload)
      : this.savService.create(payload);

    req.subscribe({
      next: () => {
        this.notification.success(
          this.isEditMode ? '✅ Ticket SAV mis à jour' : '✅ Ticket SAV créé'
        );
        this.router.navigate(['/sav/list']);
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de l\'enregistrement';
        this.notification.error('❌ ' + msg);
        this.isLoading = false;
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/sav/list']);
  }

  get f() {
    return this.form.controls;
  }
}
