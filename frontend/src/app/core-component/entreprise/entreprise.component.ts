import {Component, OnInit} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import { routes } from 'src/app/core/helpers/routes';
import {EntrepriseService} from "../../core/service/entreprise/entreprise.service";
import {Entreprise} from "../../model/entreprise.model";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {MatTooltip} from "@angular/material/tooltip";
import {PermissionDto} from "../../model/permissionDto.model";
import {AuthResponse} from "../../auth/service/auth.service";
import {ResponseDto} from "../../model/reponseDto.model";


@Component({
  selector: 'app-entreprise',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    NgForOf,
    MatTooltip,
    NgIf
  ],
  templateUrl: './entreprise.component.html',
  styleUrl: './entreprise.component.scss'
})
export class EntrepriseComponent implements OnInit{

  permissionEntreprise!: PermissionDto;
  public routes = routes;
  entreprises: Entreprise[] = [];
  isLoading: boolean = true;  // ✅ ADD THIS
  errorMessage: string = '';  // ✅ ADD THIS

  constructor(
    private entrepriseService: EntrepriseService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.getUserAuthData();
    this.loadEntreprises();  // ✅ ADD THIS
  }

  // ✅ ADD THIS METHOD
  loadEntreprises(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    console.log('🔄 Loading entreprises...');
    this.entrepriseService.getAllEntreprises().subscribe({
      next: (data) => {
        console.log("✅ Entreprises loaded:", data);
        this.entreprises = data;
        this.isLoading = false;
        
        if (data.length === 0) {
          console.warn("⚠️ No entreprises found");
        }
      },
      error: (err) => {
        console.error("❌ Error loading entreprises:", err);
        this.isLoading = false;
        this.errorMessage = "Erreur lors du chargement des entreprises";
      }
    });
  }

  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      try {
        currentUser = JSON.parse(atob(item));
        console.log('✅ User connected:', currentUser?.name);
        console.log('Token:', currentUser?.token);
        console.log('Permissions:', currentUser?.permission);

        currentUser?.permission.map(per => {
          if (per.moduleTitle.includes("gestion entreprise")) {
            this.permissionEntreprise = per;
            console.log("✅ Permission Entreprise:", this.permissionEntreprise);
          }
        });
        console.log('Entreprise:', currentUser?.entreprise.nom);
      } catch (error) {
        console.error('❌ Error parsing user data:', error);
      }
    } else {
      console.warn('⚠️ No user data found in localStorage');
    }
  }

  editEntreprise(id: number | undefined): void {
    if (id) {
      console.log('📝 Editing entreprise:', id);
      this.router.navigate(['/entreprise/detaisEntreprise', id]);
    }
  }

  deleteEntreprise(id: number | undefined): void {
    if (!id) {
      alert("ID entreprise invalide");
      return;
    }

    if (confirm("Voulez-vous vraiment supprimer cette entreprise ?")) {
      console.log('🗑️ Deleting entreprise:', id);
      this.entrepriseService.deleteEntreprise(id).subscribe({
        next: () => {
          console.log('✅ Entreprise deleted successfully');
          alert("Entreprise supprimée avec succès !");
          this.loadEntreprises(); // ✅ Reload the list after delete
        },
        error: (err) => {
          console.error('❌ Error deleting entreprise:', err);
          alert("Erreur : impossible de supprimer l'entreprise.");
        }
      });
    }
  }

  addEntreprise(): void {
    console.log('➕ Adding new entreprise');
    this.router.navigate(['/entreprise/addEntreprise']);
  }
}