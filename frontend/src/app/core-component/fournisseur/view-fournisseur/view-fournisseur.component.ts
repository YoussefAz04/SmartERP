import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { Fournisseur } from '../../../model/fournisseur.model';
import { FactureService } from '../../../core/service/facture/facture.service';
import { FactureAchat } from '../../../model/FactureAchat.model';

@Component({
  selector: 'app-view-fournisseur',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './view-fournisseur.component.html',
  styleUrl: './view-fournisseur.component.scss'
})
export class ViewFournisseurComponent implements OnInit {
  fournisseur: Fournisseur | null = null;
  isLoading = false;
  errorMessage = '';
  canEditFournisseur = false;

  facturesHistory: FactureAchat[] = [];
  isLoadingHistory = false;
  historyError = '';

  constructor(
    private fournisseurService: FournisseurService,
    private factureService: FactureService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRolePermissions();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadFournisseur(id);
      this.loadHistory(id);
    } else {
      this.errorMessage = 'ID fournisseur invalide';
    }
  }

  loadRolePermissions(): void {
    try {
      const raw = localStorage.getItem('myLSkey');
      if (!raw) return;
      const session = JSON.parse(decodeURIComponent(escape(atob(raw))));
      const roles: string[] = Array.isArray(session?.roles) ? session.roles : [];
      this.canEditFournisseur = roles.includes('SUPER_ADMIN') || roles.includes('ADMIN');
    } catch {
      this.canEditFournisseur = false;
    }
  }

  loadFournisseur(id: number): void {
    this.isLoading = true;
    this.fournisseurService.getFournisseurById(id).subscribe({
      next: (data) => {
        this.fournisseur = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement du fournisseur:', err);
        this.errorMessage = 'Erreur lors du chargement du fournisseur';
        this.isLoading = false;
      }
    });
  }

  loadHistory(id: number): void {
    this.isLoadingHistory = true;
    this.historyError = '';
    this.factureService.getFacturesByFournisseur(id).subscribe({
      next: (data) => {
        this.facturesHistory = Array.isArray(data) ? data : [];
        this.isLoadingHistory = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement de l\'historique des achats:', err);
        this.historyError = 'Erreur lors du chargement de l\'historique des achats';
        this.isLoadingHistory = false;
      }
    });
  }

  totalAchatsTTC(): number {
    return (this.facturesHistory || []).reduce((sum, f) => sum + (Number(f.montantTTC) || 0), 0);
  }

  editFournisseur(): void {
    if (this.fournisseur?.id) {
      this.router.navigate(['/fournisseur/editFournisseur', this.fournisseur.id]);
    }
  }

  goBack(): void {
    this.router.navigate(['/fournisseur/listFournisseur']);
  }
}
