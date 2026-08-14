import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../auth/service/user/user.service';
import { ModuleService } from '../../../core/service/Module/module.service';
import { EntrepriseService } from '../../../core/service/entreprise/entreprise.service';
import { Module } from '../../../model/module.model';
import { Entreprise } from '../../../model/entreprise.model';
import { NotificationService } from '../../../shared/notification/notification.service';

interface ModulePermissionFlags {
  create: boolean;
  update: boolean;
  view: boolean;
  delete: boolean;
}

interface ModulePermissionPayload extends ModulePermissionFlags {
  moduleId: number;
}

interface CreateEmployeeRequest {
  username: string;
  email: string;
  password: string;
  phone?: string;
  enterpriseId?: number | null;
  moduleIds: number[];
  modulePermissions: ModulePermissionPayload[];
}

/** Extends the base Entreprise model with fields that may be included in API list responses */
interface EntrepriseListItem extends Entreprise {
  active?: boolean;
  userCount?: number;
}

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './add-user.component.html',
  styleUrl: './add-user.component.css'
})
export class AddUserComponent implements OnInit {
  addUserForm!: FormGroup;
  isSubmitting = false;

  isSuperAdmin = false;
  currentEnterpriseId: number | null = null;

  modules: Module[] = [];
  /** Per-module CRUD flags keyed by moduleId. A module is "selected" iff at least one flag is true. */
  modulePermissions: Map<number, ModulePermissionFlags> = new Map();
  readonly permissionTypes: (keyof ModulePermissionFlags)[] = ['create', 'view', 'update', 'delete'];

  enterprises: EntrepriseListItem[] = [];
  selectedEnterpriseId: number | null = null;
  selectedEnterpriseInfo: EntrepriseListItem | null = null;
  enterprisesLoading = false;

  constructor(private fb: FormBuilder,
    private userService: UserService,
    private moduleService: ModuleService,
    private entrepriseService: EntrepriseService,
    private router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.loadSessionData();
    this.initializeForm();
    this.loadModules();
    if (this.isSuperAdmin) {
      this.loadEnterprises();
    }
  }

  loadSessionData(): void {
    try {
      const raw = localStorage.getItem('myLSkey');
      if (!raw) return;
      const session = JSON.parse(decodeURIComponent(escape(atob(raw))));
      const roles: string[] = Array.isArray(session.roles) ? session.roles : [];
      this.isSuperAdmin = roles.includes('SUPER_ADMIN');
      if (session.entrepriseId) {
        this.currentEnterpriseId = Number(session.entrepriseId);
      }
    } catch {
      // defaults apply
    }
  }

  initializeForm(): void {
    this.addUserForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phone: ['']
    });
  }

  // Titles excluded from the module permissions list (mirrors sidebar service logic)
  private readonly excludedModuleTitles = [
    'bon reception', 'bon de reception', 'bons reception', 'bons de reception',
    'bon livraison', 'bon de livraison', 'bons livraison', 'bons de livraison',
    'categorie depense',
    'select zone',
    'departement', 'departemen',
    'module titre', 'module', 'sous module',
    'entreprise', 'gestion entreprise', 'gestion des entreprises',
    'gestion utilisateurs', 'gestion des utilisateurs', 'gestion users', 'users', 'utilisateurs',
  ];

  private readonly excludedKeywords = [
    'departement', 'departemen',
    'bon de reception', 'bon reception', 'bons de reception', 'bons reception',
    'bon de livraison', 'bon livraison', 'bons de livraison', 'bons livraison',
    'entreprise',
    'utilisateur', 'utilisateurs', 'user', 'users',
  ];

  private readonly moduleDisplayNames: { [key: string]: string } = {
    'gestion de Client': 'Clients',
    'gestion de Categorie Client': 'Categorys Clients',
    'gestion de Fournisseur': 'Fournisseurs',
    'gestion de Categorie Fournisseur': 'Categorys Fournisseurs',
    'gestion Article Achat': 'Purchase Products',
    'gestion Article Vente': 'Sales Products',
    'gestion Stock': 'Stock',
    'Commande Achat': 'Commandes Achat',
    'Commande Vente': 'Commandes Vente',
  };

  private normalizeTitle(s: string): string {
    return s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  }

  private isModuleAllowed(module: Module): boolean {
    const t = this.normalizeTitle(module.title ?? '');
    if (this.excludedModuleTitles.some(excluded => t === this.normalizeTitle(excluded))) return false;
    if (this.excludedKeywords.some(kw => t.includes(this.normalizeTitle(kw)))) return false;
    return true;
  }

  getModuleDisplayTitle(module: Module): string {
    return this.moduleDisplayNames[module.title] || module.title;
  }

  loadModules(): void {
    // Module permissions are only relevant for ADMIN-created employees; skip for SUPER_ADMIN
    if (this.isSuperAdmin) {
      return;
    }
    this.moduleService.getAllModules().subscribe({
      next: (modules) => {
        this.modules = modules.filter(m => this.isModuleAllowed(m));
      },
      error: (err: any) => {
        console.error('Error loading modules:', err);
      }
    });
  }

  toggleModule(moduleId: number): void {
    if (this.isModuleSelected(moduleId)) {
      // Module currently has at least one flag -> clear all (deselect).
      this.modulePermissions.delete(moduleId);
    } else {
      // Default to full CRUD when the module is enabled.
      this.modulePermissions.set(moduleId, { create: true, update: true, view: true, delete: true });
    }
  }

  isModuleSelected(moduleId: number): boolean {
    const flags = this.modulePermissions.get(moduleId);
    return !!flags && (flags.create || flags.update || flags.view || flags.delete);
  }

  getModuleFlags(moduleId: number): ModulePermissionFlags {
    return this.modulePermissions.get(moduleId) ?? { create: false, update: false, view: false, delete: false };
  }

  hasPermission(moduleId: number, type: keyof ModulePermissionFlags): boolean {
    return !!this.modulePermissions.get(moduleId)?.[type];
  }

  togglePermission(moduleId: number, type: keyof ModulePermissionFlags): void {
    const current = this.modulePermissions.get(moduleId)
      ?? { create: false, update: false, view: false, delete: false };
    const next: ModulePermissionFlags = { ...current, [type]: !current[type] };
    if (!next.create && !next.update && !next.view && !next.delete) {
      this.modulePermissions.delete(moduleId);
    } else {
      this.modulePermissions.set(moduleId, next);
    }
  }

  setModuleFullAccess(moduleId: number): void {
    this.modulePermissions.set(moduleId, { create: true, update: true, view: true, delete: true });
  }

  setModuleReadOnly(moduleId: number): void {
    this.modulePermissions.set(moduleId, { create: false, update: false, view: true, delete: false });
  }

  isModuleFullAccess(moduleId: number): boolean {
    const f = this.modulePermissions.get(moduleId);
    return !!f && f.create && f.update && f.view && f.delete;
  }

  isModuleReadOnly(moduleId: number): boolean {
    const f = this.modulePermissions.get(moduleId);
    return !!f && !f.create && !f.update && f.view && !f.delete;
  }

  loadEnterprises(): void {
    this.enterprisesLoading = true;
    this.entrepriseService.getAllEntreprises().subscribe({
      next: (data: EntrepriseListItem[]) => {
        this.enterprises = data;
        this.enterprisesLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading enterprises:', err);
        this.enterprisesLoading = false;
      }
    });
  }

  onEnterpriseSelected(enterpriseId: number): void {
    this.selectedEnterpriseId = enterpriseId;
    this.selectedEnterpriseInfo = this.enterprises.find(e => e.id === enterpriseId) ?? null;
  }

  formatDate(date: string): string {
    if (!date) return '-';
    try {
      return new Date(date).toLocaleDateString('fr-FR', { year: 'numeric', month: 'short', day: '2-digit' });
    } catch {
      return date;
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.addUserForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  onSubmitAddUser(): void {
    if (!this.addUserForm.valid) {
      this.notification.warning('Veuillez remplir tous les champs requis');
      return;
    }

    if (this.isSuperAdmin && !this.selectedEnterpriseId) {
      this.notification.error('❌ Veuillez sélectionner une entreprise à laquelle affecter l\'utilisateur.');
      return;
    }

    if (!this.isSuperAdmin && this.currentEnterpriseId === null) {
      this.notification.error('❌ Le contexte d\'entreprise est manquant. Veuillez vous déconnecter puis vous reconnecter.');
      return;
    }

    this.isSubmitting = true;
    const formValue = this.addUserForm.value;

    const modulePermissionsList: ModulePermissionPayload[] = [];
    this.modulePermissions.forEach((flags, moduleId) => {
      if (flags.create || flags.update || flags.view || flags.delete) {
        modulePermissionsList.push({ moduleId, ...flags });
      }
    });

    const request: CreateEmployeeRequest = {
      username: formValue.username,
      email: formValue.email,
      password: formValue.password,
      phone: formValue.phone || undefined,
      enterpriseId: this.isSuperAdmin ? this.selectedEnterpriseId! : this.currentEnterpriseId,
      moduleIds: modulePermissionsList.map(mp => mp.moduleId),
      modulePermissions: modulePermissionsList
    };

    this.userService.createEmployeeUser(request).subscribe({
      next: (response: any) => {
        console.log('Employee created successfully:', response);
        this.notification.success('✓ Employé créé avec succès');
        this.router.navigate(['/user-management/users']);
        this.isSubmitting = false;
      },
      error: (err: any) => {
        console.error('Error creating employee:', err);
        this.notification.error('✗ Error: ' + (err.error?.message || err.error || err.message));
        this.isSubmitting = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/user-management/users']);
  }
}
