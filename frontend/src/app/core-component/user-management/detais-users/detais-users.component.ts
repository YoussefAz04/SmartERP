import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { UserService } from '../../../auth/service/user/user.service';
import { UserDTO } from '../../../model/userDTO.model';
import { ResponseDto } from '../../../model/reponseDto.model';
import { PermissionDto } from '../../../model/permissionDto.model';
import { NotificationService } from '../../../shared/notification/notification.service';
import { RoleService } from '../../../core/service/role/role.service';
import { Role } from '../../../model/role.model';
import { ModuleService } from '../../../core/service/Module/module.service';
import { Module } from '../../../model/module.model';
import { PermissionService, ModulePermissionRequest } from '../../../core/service/permission/permission.service';

interface ModulePermissionFlags {
  create: boolean;
  update: boolean;
  view: boolean;
  delete: boolean;
}

@Component({
  selector: 'app-detais-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detais-users.component.html',
  styleUrl: './detais-users.component.scss'
})
export class DetaisUsersComponent implements OnInit {
  user: UserDTO | null = null;
  isLoading = true;
  userId!: number;
  isSubmitting = false;

  // Roles
  selectedRoleNames: string[] = [];
  availableRoles: Role[] = [];
  rolesLoading = true;
  rolesLoadError = false;
  isSuperAdmin = false;
  isAdmin = false;

  // Module permissions (only for ADMIN, not SUPER_ADMIN)
  modules: Module[] = [];
  modulePermissions: Map<number, ModulePermissionFlags> = new Map();
  readonly permissionTypes: (keyof ModulePermissionFlags)[] = ['create', 'view', 'update', 'delete'];

  permissionUsers: PermissionDto = {
    create: false,
    view: false,
    update: false,
    delete: false,
    moduleTitle: ''
  } as PermissionDto;

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

  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: NotificationService,
    private roleService: RoleService,
    private moduleService: ModuleService,
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.params['id'];
    this.getUserAuthData();
    this.loadRoles();
    this.loadUserData();
  }

  // ----------------------------------------------------------------
  // Data loading
  // ----------------------------------------------------------------

  loadUserData(): void {
    this.isLoading = true;
    if (this.isSuperAdmin) {
      // SUPER_ADMIN: only load user details (no module permissions needed)
      this.userService.getUserById(this.userId).subscribe({
        next: (data) => {
          this.user = data;
          this.selectedRoleNames = data.roles ? [...data.roles] : [];
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error loading user:', err);
          this.notification.error('Erreur lors du chargement de l\'utilisateur');
          this.isLoading = false;
        }
      });
    } else {
      // ADMIN: load user + modules + permissions in parallel
      forkJoin({
        user: this.userService.getUserById(this.userId),
        modules: this.moduleService.getAllModules(),
        permissions: this.permissionService.getPermissionsByUser(this.userId)
      }).subscribe({
        next: ({ user, modules, permissions }) => {
          this.user = user;
          this.selectedRoleNames = user.roles ? [...user.roles] : [];
          this.modules = modules.filter(m => this.isModuleAllowed(m));
          this.buildModulePermissions(permissions);
          this.isLoading = false;
        },
        error: (err: any) => {
          console.error('Error loading user data:', err);
          this.notification.error('Erreur lors du chargement des données');
          this.isLoading = false;
        }
      });
    }
  }

  private buildModulePermissions(permissions: PermissionDto[]): void {
    this.modulePermissions = new Map();
    const allowedModuleIds = new Set(this.modules.map(module => module.id));
    for (const p of permissions) {
      if (p.moduleId && allowedModuleIds.has(p.moduleId)) {
        this.modulePermissions.set(p.moduleId, {
          create: p.create,
          update: p.update,
          view: p.view,
          delete: p.delete
        });
      }
    }
  }

  loadRoles(): void {
    this.rolesLoading = true;
    this.rolesLoadError = false;
    this.roleService.getAllRoles().subscribe({
      next: (roles) => {
        this.availableRoles = roles.filter(r =>
          r.roleType === 'EMPLOYEE' || r.roleType === 'ADMIN'
        );
        this.rolesLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading roles:', err);
        this.rolesLoading = false;
        this.rolesLoadError = true;
      }
    });
  }

  // ----------------------------------------------------------------
  // Module permission helpers (same as add-user)
  // ----------------------------------------------------------------

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

  isModuleSelected(moduleId: number): boolean {
    const flags = this.modulePermissions.get(moduleId);
    return !!flags && (flags.create || flags.update || flags.view || flags.delete);
  }

  toggleModule(moduleId: number): void {
    if (this.isModuleSelected(moduleId)) {
      this.modulePermissions.delete(moduleId);
    } else {
      this.modulePermissions.set(moduleId, { create: true, update: true, view: true, delete: true });
    }
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

  // ----------------------------------------------------------------
  // Role helpers
  // ----------------------------------------------------------------

  isRoleSelected(roleName: string): boolean {
    return this.selectedRoleNames.includes(roleName);
  }

  toggleRole(roleName: string): void {
    if (!this.availableRoles.some(r => r.name === roleName)) {
      return;
    }
    const idx = this.selectedRoleNames.indexOf(roleName);
    if (idx >= 0) {
      this.selectedRoleNames.splice(idx, 1);
    } else {
      this.selectedRoleNames.push(roleName);
    }
  }

  // ----------------------------------------------------------------
  // Submit
  // ----------------------------------------------------------------

  onSubmit(): void {
    if (!this.user || !this.userId) {
      this.notification.error('❌ Données utilisateur manquantes');
      return;
    }

    if (this.isSubmitting) {
      this.notification.info('Operation in progress...');
      return;
    }

    this.isSubmitting = true;
    this.user.roles = [...this.selectedRoleNames];

    // Build the module permissions payload
    const modulePerms: ModulePermissionRequest[] = [];
    this.modulePermissions.forEach((flags, moduleId) => {
      if (flags.create || flags.update || flags.view || flags.delete) {
        modulePerms.push({ moduleId, ...flags });
      }
    });

    if (this.isSuperAdmin) {
      // SUPER_ADMIN: only update user info (no module permissions)
      this.userService.updateUser(this.userId, this.user).subscribe({
        next: () => {
          this.isSubmitting = false;
          this.notification.success('✓ User mis à jour avec succès');
          this.router.navigate(['/user-management/users']);
        },
        error: (err: any) => {
          this.isSubmitting = false;
          const errorMsg = err.error?.message || err.message || 'Unknown error';
          this.notification.error('✗ Erreur lors de la mise à jour: ' + errorMsg);
        }
      });
    } else {
      // ADMIN: update user info AND permissions
      forkJoin({
        user: this.userService.updateUser(this.userId, this.user),
        permissions: this.permissionService.updateUserPermissions(this.userId, modulePerms)
      }).subscribe({
        next: () => {
          this.isSubmitting = false;
          this.notification.success('✓ User mis à jour avec succès');
          this.router.navigate(['/user-management/users']);
        },
        error: (err: any) => {
          this.isSubmitting = false;
          const errorMsg = err.error?.message || err.message || 'Unknown error';
          this.notification.error('✗ Erreur lors de la mise à jour: ' + errorMsg);
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/user-management/users']);
  }

  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');
    
    if (item) {
      try {
        const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));

        const roles: string[] = Array.isArray(currentUser?.roles) ? currentUser.roles : [];
        this.isSuperAdmin = roles.includes('SUPER_ADMIN');
        this.isAdmin = roles.includes('ADMIN');

        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          currentUser.permission.forEach(per => {
            if (per.moduleTitle.toLowerCase().includes("gestion users") || 
                per.moduleTitle.toLowerCase().includes("users")) {
              this.permissionUsers = per;
            }
          });
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
  }
}
