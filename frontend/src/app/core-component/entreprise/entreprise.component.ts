import {Component, OnInit} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {EntrepriseService} from "../../core/service/entreprise/entreprise.service";
import {Entreprise} from "../../model/entreprise.model";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {MatTooltip} from "@angular/material/tooltip";
import {PermissionDto} from "../../model/permissionDto.model";
import {ResponseDto} from "../../model/reponseDto.model";
import { NotificationService } from '../../shared/notification/notification.service';


@Component({
  selector: 'app-entreprise',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    NgForOf,
    MatTooltip,
    NgIf,
    FormsModule
  ],
  templateUrl: './entreprise.component.html',
  styleUrl: './entreprise.component.css'
})
export class EntrepriseComponent implements OnInit{

  permissionEntreprise!: PermissionDto;
  entreprises: Entreprise[] = [];
  currentEntreprise: Entreprise | null = null;
  currentEntrepriseId: number | null = null;
  isLoading: boolean = true;
  errorMessage: string = '';
  searchTerm: string = '';

  roleId: number = 0;
  isSuperAdmin: boolean = false;
  isAdmin: boolean = false;
  isEmployee: boolean = false;

  get filteredEntreprises(): Entreprise[] {
    if (!this.searchTerm.trim()) return this.entreprises;
    const term = this.searchTerm.toLowerCase();
    return this.entreprises.filter(e =>
      (e.nom?.toLowerCase().includes(term)) ||
      (e.raisonSociale?.toLowerCase().includes(term)) ||
      (e.email?.toLowerCase().includes(term)) ||
      (e.secteurActivite?.toLowerCase().includes(term)) ||
      (e.adresse?.toLowerCase().includes(term))
    );
  }

  get totalCount(): number { return this.entreprises.filter(e => e.active === true).length; }
  get uniqueSectors(): number { return new Set(this.entreprises.map(e => e.secteurActivite).filter(Boolean)).size; }

  resetFilters(): void { this.searchTerm = ''; }

  constructor(private entrepriseService: EntrepriseService,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.getUserAuthData();
  }

  loadEntreprises(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.entrepriseService.getAllEntreprisesWithInactive().subscribe({
      next: (data) => {
        this.entreprises = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error("❌ Error loading entreprises:", err);
        this.isLoading = false;
        this.errorMessage = "Erreur lors du chargement des entreprises";
      }
    });
  }

  loadCurrentEntreprise(entrepriseId: number): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.entrepriseService.getEntrepriseById(entrepriseId).subscribe({
      next: (data) => {
        this.currentEntreprise = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error("❌ Error loading entreprise:", err);
        this.isLoading = false;
        this.errorMessage = "Erreur lors du chargement de votre entreprise";
      }
    });
  }

  refreshData(): void {
    if (this.isSuperAdmin) {
      this.loadEntreprises();
    } else if (this.currentEntrepriseId) {
      this.loadCurrentEntreprise(this.currentEntrepriseId);
    }
  }

  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      try {
        currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));

        currentUser?.permission?.forEach(per => {
          if (per.moduleTitle.includes("gestion entreprise")) {
            this.permissionEntreprise = per;
          }
        });

        this.roleId = currentUser?.roleId ?? 0;
        this.isSuperAdmin = this.roleId === 0;
        this.isAdmin = this.roleId === 1;
        this.isEmployee = this.roleId >= 2;

        if (this.isSuperAdmin) {
          this.loadEntreprises();
        } else {
          const entrepriseId = currentUser?.entreprise?.id ?? currentUser?.entrepriseId ?? null;
          if (entrepriseId) {
            this.currentEntrepriseId = entrepriseId;
            this.loadCurrentEntreprise(entrepriseId);
          } else {
            this.isLoading = false;
            this.errorMessage = "Aucune entreprise associée à votre compte";
          }
        }
      } catch (error) {
        console.error('❌ Error parsing user data:', error);
        this.isLoading = false;
      }
    } else {
      console.warn('⚠️ No user data found in localStorage');
      this.isLoading = false;
    }
  }

  editEntreprise(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/entreprise/detaisEntreprise', id]);
    }
  }

  disableEntreprise(id: number | undefined): void {
    if (!id) {
      this.notification.error("ID entreprise invalide");
      return;
    }

    this.notification.confirm("Voulez-vous vraiment désactivate cette entreprise et tous ses utilisateurs ?").then(ok => {
      if (ok) {
        this.entrepriseService.disableEntreprise(id).subscribe({
          next: () => {
            this.notification.success("Entreprise désactivée avec succès !");
            this.loadEntreprises();
          },
          error: (err) => {
            console.error('❌ Error disabling entreprise:', err);
            this.notification.error("Error: impossible de désactivate l'entreprise.");
          }
        });
      }
    });
  }

  enableEntreprise(id: number | undefined): void {
    if (!id) {
      this.notification.error("ID entreprise invalide");
      return;
    }

    this.notification.confirm("Voulez-vous vraiment réactivate cette entreprise et tous ses utilisateurs ?").then(ok => {
      if (ok) {
        this.entrepriseService.enableEntreprise(id).subscribe({
          next: () => {
            this.notification.success("Entreprise réactivée avec succès !");
            this.loadEntreprises();
          },
          error: (err) => {
            console.error('❌ Error enabling entreprise:', err);
            this.notification.error("Error: impossible de réactivate l'entreprise.");
          }
        });
      }
    });
  }

  addEntreprise(): void {
    this.router.navigate(['/entreprise/addEntreprise']);
  }
}
