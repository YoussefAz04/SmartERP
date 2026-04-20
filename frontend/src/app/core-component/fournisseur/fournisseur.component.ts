import { Component, OnInit, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { FournisseurService } from '../../core/service/fournisseur/fournisseur.service';
import { Router, RouterOutlet } from '@angular/router';
import { Fournisseur } from '../../model/fournisseur.model';
import { CategorieFournisseur } from '../../model/categorieFournisseur.model';
import { NgForOf, NgIf, CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ResponseDto } from '../../model/reponseDto.model';
import { PermissionDto } from '../../model/permissionDto.model';
import { NotificationService } from '../../shared/notification/notification.service';

@Component({
  selector: 'app-fournisseur',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    CommonModule,
    DecimalPipe,
    RouterOutlet,
    FormsModule
  ],
  templateUrl: './fournisseur.component.html',
  styleUrl: './fournisseur.component.scss'
})
export class FournisseurComponent implements OnInit, AfterViewInit {
  fournisseurList: Fournisseur[] = [];
  categories: CategorieFournisseur[] = [];
  isLoading = false;
  isDeleting = false;
  searchTerm: string = '';
  filterCategorie: string = '';

  canCreateFournisseur = false;
  canEditFournisseur = false;
  canDeleteFournisseur = false;

  get filteredFournisseurs(): Fournisseur[] {
    let list = this.fournisseurList;
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      list = list.filter(f =>
        (f.nom?.toLowerCase().includes(term)) ||
        (f.email?.toLowerCase().includes(term)) ||
        (f.telephone?.toLowerCase().includes(term)) ||
        (f.adresse?.toLowerCase().includes(term))
      );
    }
    if (this.filterCategorie) {
      list = list.filter(f => f.categorieFournisseur?.nom === this.filterCategorie);
    }
    return list;
  }

  get totalCount(): number { return this.fournisseurList.length; }
  get withEmail(): number { return this.fournisseurList.filter(f => f.email).length; }
  get withPhone(): number { return this.fournisseurList.filter(f => f.telephone).length; }
  get uniqueCategories(): number {
    return new Set(this.fournisseurList.map(f => f.categorieFournisseur?.nom).filter(Boolean)).size;
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.filterCategorie = '';
  }

  permissionFournisseur: PermissionDto = {
    create: false,
    view: false,
    update: false,
    delete: false,
    moduleTitle: ''
  } as PermissionDto;

  constructor(private fournisseurService: FournisseurService, private router: Router, private cdr: ChangeDetectorRef,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.getUserAuthData();
    this.loadFournisseurs();
    this.loadCategories();
  }

  ngAfterViewInit(): void {
    console.log('Final permissionFournisseur:', this.permissionFournisseur);
    console.log('Create permission:', this.permissionFournisseur?.create);
  }

  loadFournisseurs(): void {
    this.isLoading = true;
    this.fournisseurService.getAllFournisseurs().subscribe({
      next: (data) => {
        this.fournisseurList = data;
        this.isLoading = false;
        console.log('Fournisseurs loaded:', this.fournisseurList);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error loading fournisseurs:', err);
        this.notification.error('Erreur lors du chargement des fournisseurs');
      }
    });
  }

  loadCategories(): void {
    this.fournisseurService.getAllCategorieFournisseurs().subscribe({
      next: (data) => { this.categories = data; },
      error: (err) => { console.error('Error loading categories:', err); }
    });
  }

  addFournisseur(): void {
    this.router.navigate(['/fournisseur/addFournisseur']);
  }

  viewFournisseur(id: number | undefined): void {
    if (id) {
      console.log('Viewing fournisseur:', id);
      this.router.navigate(['/fournisseur/viewFournisseur', id]);
    } else {
      this.notification.error('ID fournisseur invalide');
    }
  }

  editFournisseur(id: number | undefined): void {
    if (id) {
      console.log('Editing fournisseur:', id);
      this.router.navigate(['/fournisseur/editFournisseur', id]);
    } else {
      this.notification.error('ID fournisseur invalide');
    }
  }

  deleteFournisseur(id: number | undefined): void {
    if (!id) {
      this.notification.error('ID fournisseur invalide');
      return;
    }

    this.notification.confirm('Êtes-vous sûr de vouloir supprimer ce fournisseur ?').then(confirmed => {
      if (confirmed) {
        this.isDeleting = true;
        console.log('Suppression du fournisseur:', id);

        this.fournisseurService.deleteFournisseur(id).subscribe({
          next: (response) => {
            console.log('Fournisseur supprimé:', response);
            this.fournisseurList = this.fournisseurList.filter(f => f.id !== id);
            this.isDeleting = false;
            this.notification.success('✓ Fournisseur supprimé avec succès');
            this.loadFournisseurs();
          },
          error: (err) => {
            this.isDeleting = false;
            console.error('Erreur lors de la suppression:', err);
            const errorMsg = err.error?.message || err.message || 'Erreur inconnue';
            this.notification.error('✗ Erreur: ' + errorMsg);
          }
        });
      } else {
        console.log('Suppression annulée');
      }
    });
  }

  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      try {
        currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
        console.log('User connecté:', currentUser?.name);
        console.log('Permissions:', currentUser?.permission);

        const roles: string[] = Array.isArray(currentUser?.roles) ? (currentUser.roles as string[]) : [];
        const isSuperAdmin = roles.includes('SUPER_ADMIN');
        const isAdmin = roles.includes('ADMIN');
        const isEmployee = roles.includes('EMPLOYEE');
        const canByRole = isSuperAdmin || isAdmin;

        // EMPLOYEE always has read-only access regardless of DB permissions
        if (isEmployee && !canByRole) {
          this.canCreateFournisseur = false;
          this.canEditFournisseur = false;
          this.canDeleteFournisseur = false;
          this.permissionFournisseur = { create: false, view: true, update: false, delete: false, moduleTitle: 'gestion Fournisseur' } as PermissionDto;
          this.cdr.detectChanges();
          return;
        }

        let fournisseurPermissionFound = false;
        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          const fournisseurPerm = currentUser.permission.find((p: any) =>
            p.moduleTitle?.toLowerCase().includes('fournisseur')
          );
          if (fournisseurPerm) {
            fournisseurPermissionFound = true;
            this.permissionFournisseur = fournisseurPerm;
            this.canCreateFournisseur = fournisseurPerm.create === true;
            this.canEditFournisseur = fournisseurPerm.update === true;
            this.canDeleteFournisseur = fournisseurPerm.delete === true;
            console.log('Permission Fournisseur:', this.permissionFournisseur);
          }
        }

        // Fallback: if no DB permission record found, derive access from role
        if (!fournisseurPermissionFound) {
          this.canCreateFournisseur = canByRole;
          this.canEditFournisseur = canByRole;
          this.canDeleteFournisseur = canByRole;
          this.permissionFournisseur = {
            create: canByRole,
            view: canByRole || isEmployee,
            update: canByRole,
            delete: canByRole,
            moduleTitle: 'gestion Fournisseur'
          } as PermissionDto;
        }

        console.log('canCreateFournisseur:', this.canCreateFournisseur, 'canEditFournisseur:', this.canEditFournisseur, 'canDeleteFournisseur:', this.canDeleteFournisseur);
        console.log('Entreprise:', currentUser?.entreprise?.nom);
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    } else {
      console.warn('No user found in localStorage');
    }
    this.cdr.detectChanges();
  }
}