import { Component, OnInit, AfterViewInit } from '@angular/core';
import { FournisseurService } from '../../core/service/fournisseur/fournisseur.service';
import { Router, RouterOutlet } from '@angular/router';
import { Fournisseur } from '../../model/fournisseur.model';
import { CategorieFournisseur } from '../../model/categorieFournisseur.model';
import { NgForOf, NgIf, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ResponseDto } from '../../model/reponseDto.model';
import { PermissionDto } from '../../model/permissionDto.model';

@Component({
  selector: 'app-fournisseur',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    CommonModule,
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

  constructor(private fournisseurService: FournisseurService, private router: Router) {}

  ngOnInit(): void {
    this.loadFournisseurs();
    this.loadCategories();
    this.getUserAuthData();
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
        alert('Erreur lors du chargement des fournisseurs');
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
      alert('ID fournisseur invalide');
    }
  }

  editFournisseur(id: number | undefined): void {
    if (id) {
      console.log('Editing fournisseur:', id);
      this.router.navigate(['/fournisseur/editFournisseur', id]);
    } else {
      alert('ID fournisseur invalide');
    }
  }

  deleteFournisseur(id: number | undefined): void {
    if (!id) {
      alert('ID fournisseur invalide');
      return;
    }

    const confirmed = confirm('Êtes-vous sûr de vouloir supprimer ce fournisseur ?');

    if (confirmed) {
      this.isDeleting = true;
      console.log('Suppression du fournisseur:', id);

      this.fournisseurService.deleteFournisseur(id).subscribe({
        next: (response) => {
          console.log('Fournisseur supprimé:', response);
          this.fournisseurList = this.fournisseurList.filter(f => f.id !== id);
          this.isDeleting = false;
          alert('✓ Fournisseur supprimé avec succès');
          this.loadFournisseurs();
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
        console.log('Permissions:', currentUser?.permission);

        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          currentUser.permission.forEach((per: any) => {
            if (per.moduleTitle?.toLowerCase().includes('gestion de fournisseur') ||
                per.moduleTitle?.toLowerCase().includes('gestion fournisseur')) {
              this.permissionFournisseur = per;
              console.log('Permission Fournisseur:', this.permissionFournisseur);
            }
          });
        }

        console.log('Entreprise:', currentUser?.entreprise?.nom);
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    } else {
      console.warn('No user found in localStorage');
    }
  }
}