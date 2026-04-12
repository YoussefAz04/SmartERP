import { Component, OnInit, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { ClientService } from "../../core/service/client/client.service";
import { Router, RouterOutlet } from "@angular/router";
import { Client } from "../../model/client.model";
import { MatTooltip } from "@angular/material/tooltip";
import { NgForOf, NgIf, CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ResponseDto } from "../../model/reponseDto.model";
import { PermissionDto } from "../../model/permissionDto.model";

@Component({
  selector: 'app-client',
  standalone: true,
  imports: [
    MatTooltip,
    NgForOf,
    NgIf,
    CommonModule,
    RouterOutlet,
    FormsModule
  ],
  templateUrl: './client.component.html',
  styleUrl: './client.component.scss'
})
export class ClientComponent implements OnInit, AfterViewInit {
  clientList: Client[] = [];
  isDeleting = false;
  searchTerm: string = '';

  canCreateClient = false;
  canEditClient = false;
  canDeleteClient = false;

  get filteredClients(): Client[] {
    if (!this.searchTerm.trim()) return this.clientList;
    const term = this.searchTerm.toLowerCase();
    return this.clientList.filter(c =>
      (c.nom?.toLowerCase().includes(term)) ||
      (c.prenom?.toLowerCase().includes(term)) ||
      (c.email?.toLowerCase().includes(term)) ||
      (c.telephone?.toLowerCase().includes(term)) ||
      (c.secteurActivite?.toLowerCase().includes(term))
    );
  }

  get totalCount(): number { return this.clientList.length; }
  get uniqueSectors(): number { return new Set(this.clientList.map(c => c.secteurActivite).filter(Boolean)).size; }
  get withEmail(): number { return this.clientList.filter(c => c.email).length; }

  resetFilters(): void { this.searchTerm = ''; }
  
  permissionClient: PermissionDto = {
    create: false,
    view: false,
    update: false,
    delete: false,
    moduleTitle: ''
  } as PermissionDto;

  constructor(private Clientservise: ClientService, private router: Router, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.getUserAuthData();
    this.loadClients();
  }

  ngAfterViewInit(): void {
    console.log('Final permissionClient:', this.permissionClient);
    console.log('Create permission:', this.permissionClient?.create);
  }

  loadClients(): void {
    this.Clientservise.getAllClients().subscribe({
      next: (data) => {
        this.clientList = data;
        console.log('Clients loaded:', this.clientList);
      },
      error: (err) => {
        console.error('Error loading clients:', err);
        alert('Erreur lors du chargement des clients');
      }
    });
  }

  addClient(): void {
    this.router.navigate(['/client/addClient']);
  }

  viewClient(id: number | undefined): void {
    if (id) {
      console.log('Viewing client:', id);
      this.router.navigate(['/client/viewClient', id]);
    } else {
      alert('ID client invalide');
    }
  }

  deleteClient(id: number | undefined): void {
    if (!id) {
      alert('ID client invalide');
      return;
    }

    // Show confirmation dialog
    const confirmed = confirm('Êtes-vous sûr de vouloir supprimer ce client ?');
    
    if (confirmed) {
      this.isDeleting = true;
      console.log('Suppression du client:', id);

      this.Clientservise.deleteClient(id).subscribe({
        next: (response) => {
          console.log('Client supprimé:', response);
          
          // Remove from list
          this.clientList = this.clientList.filter(c => c.id !== id);
          this.isDeleting = false;
          
          // Show success message
          alert('✓ Client supprimé avec succès');
          
          // Reload to ensure sync
          this.loadClients();
        },
        error: (err) => {
          this.isDeleting = false;
          console.error('Erreur lors de la suppression:', err);
          
          const errorMsg = err.error?.message || err.message || 'Erreur inconnue';
          alert('✗ Erreur: ' + errorMsg);
        }
      });
    } else {
      console.log('Suppression annulée');
    }
  }

  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      try {
        currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
        console.log('User connecté:', currentUser?.name);
        console.log('Token:', currentUser?.token);
        console.log('Permissions:', currentUser?.permission);

        const roles: string[] = Array.isArray(currentUser?.roles) ? (currentUser.roles as string[]) : [];
        const isSuperAdmin = roles.includes('SUPER_ADMIN');
        const isAdmin = roles.includes('ADMIN');
        const isEmployee = roles.includes('EMPLOYEE');
        this.canCreateClient = isSuperAdmin || isAdmin;
        this.canEditClient = isSuperAdmin || isAdmin;
        this.canDeleteClient = isSuperAdmin || isAdmin;

        let clientPermissionFound = false;
        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          currentUser.permission.forEach((per: any) => {
            if (per.moduleTitle?.toLowerCase().includes("gestion de client") ||
                per.moduleTitle?.toLowerCase().includes("gestion client")) {
              this.permissionClient = per;
              clientPermissionFound = true;
              console.log("Permission Client:", this.permissionClient);
            }
          });
        }

        // Fallback: if no DB permission record found, derive access from role
        if (!clientPermissionFound) {
          this.permissionClient = {
            create: isSuperAdmin || isAdmin,
            view: isSuperAdmin || isAdmin || isEmployee,
            update: isSuperAdmin || isAdmin,
            delete: isSuperAdmin || isAdmin,
            moduleTitle: 'gestion de Client'
          } as PermissionDto;
        }

        console.log('Entreprise:', currentUser?.entreprise?.nom);
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    } else {
      console.warn("No user found in localStorage");
    }
    this.cdr.detectChanges();
  }

  editClient(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/client/editClient', id]);
    } else {
      alert('ID client invalide');
    }
  }
}