import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';

import { Module } from '../../../model/module.model';
import { Permission } from '../../../model/permission.model';
import { ModuleService } from '../../../core/service/Module/module.service';
import { PermissionService } from '../../../core/service/permission/permission.service';
import { PermissionDto } from "../../../model/permissionDto.model";
import { SidebarService } from "../../../core/service/sidebar/sidebar.service";
import {AuthService} from "../../../auth/service/auth.service";

type PermissionType = 'create' | 'update' | 'view' | 'delete';

interface ModulePermissionItem {
  moduleId: number;
  moduleTitle: string;
  permissionType: PermissionType;
}

interface ModulePermissions {
  moduleId: number;
  moduleTitle: string;
  permissions: ModulePermissionItem[];
  allSelected?: boolean;
}

@Component({
  selector: 'app-permissions',
  templateUrl: './permissions.component.html',
  styleUrls: ['./permissions.component.scss']
})
export class PermissionsComponent implements OnInit {

  modulelist: Module[] = [];
  idRole!: number;
  permissionDto!: PermissionDto;

  // Données organisées par module
  availableModules: ModulePermissions[] = [];
  assignedModules: ModulePermissions[] = [];
  initialAssignedModules: ModulePermissions[] = [];

  // Filtres de recherche
  availableSearchTerm: string = '';
  assignedSearchTerm: string = '';

  // Types de permissions disponibles
  permissionTypes: PermissionType[] = ['create', 'update', 'view', 'delete'];
  private idUser: any;

  constructor(
    private moduleService: ModuleService,
    private permissionService: PermissionService,
    private route: ActivatedRoute,
    private sidebar: SidebarService,
    private autheService : AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.idRole = +this.route.snapshot.params['id'];
    this.loadModulesAndPermissions();
  }

  loadModulesAndPermissions() {
    this.moduleService.getAllModules().subscribe({
      next: (modules) => {
        this.modulelist = modules;
        this.loadRolePermissions();
      },
      error: (err) => {
        console.error("Erreur lors du chargement des modules", err);
      }
    });
  }

  private loadRolePermissions() {
    this.permissionService.getPermissionsByRole(this.idRole).subscribe({
      next: (permissions: PermissionDto[]) => {
        this.processPermissions(permissions);
        console.log("Erreur lors du chargement des permissions", permissions);
      },
      error: err => {
        console.error("Erreur lors de la récupération des permissions", err);
      }
    });
  }

  private processPermissions(permissions: PermissionDto[]) {
    // Créer un map des permissions assignées
    const assignedPermissionsMap = new Map<number, PermissionDto>();
    permissions.forEach(permission => {
      assignedPermissionsMap.set(permission.moduleId, permission);
    });

    // Organiser les modules avec leurs permissions
    this.assignedModules = [];
    this.availableModules = [];

    this.modulelist.forEach(module => {
      const assignedPermission = assignedPermissionsMap.get(module.id);
      const modulePermissions: ModulePermissions = {
        moduleId: module.id,
        moduleTitle: module.title,
        permissions: []
      };

      if (assignedPermission) {
        // Module avec permissions assignées
        this.permissionTypes.forEach(type => {
          if (assignedPermission[type]) {
            modulePermissions.permissions.push({
              moduleId: module.id,
              moduleTitle: module.title,
              permissionType: type
            });
          }
        });

        if (modulePermissions.permissions.length > 0) {
          this.assignedModules.push(modulePermissions);
        }
      }

      // Créer le module disponible avec les permissions non assignées
      const availableModulePermissions: ModulePermissions = {
        moduleId: module.id,
        moduleTitle: module.title,
        permissions: []
      };

      this.permissionTypes.forEach(type => {
        if (!assignedPermission || !assignedPermission[type]) {
          availableModulePermissions.permissions.push({
            moduleId: module.id,
            moduleTitle: module.title,
            permissionType: type
          });
        }
      });

      if (availableModulePermissions.permissions.length > 0) {
        this.availableModules.push(availableModulePermissions);
      }
    });

    // Sauvegarder l'état initial
    this.initialAssignedModules = JSON.parse(JSON.stringify(this.assignedModules));
    this.updateAllSelectedStates();
  }

  // Gestion du drag & drop entre conteneurs
  onDropContainer(event: CdkDragDrop<ModulePermissions[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const movedModule = event.previousContainer.data[event.previousIndex];
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
      this.updateAllSelectedStates();
    }
  }

  // Déplacer une permission spécifique
  movePermission(permission: ModulePermissionItem, toAssigned: boolean) {
    if (toAssigned) {
      // Déplacer vers assigné
      this.removePermissionFromAvailable(permission);
      this.addPermissionToAssigned(permission);
    } else {
      // Déplacer vers disponible
      this.removePermissionFromAssigned(permission);
      this.addPermissionToAvailable(permission);
    }
    this.updateAllSelectedStates();
  }

  // Sélectionner/désélectionner toutes les permissions d'un module
  toggleAllModulePermissions(moduleId: number, fromAssigned: boolean) {
    const sourceArray = fromAssigned ? this.assignedModules : this.availableModules;
    const targetArray = fromAssigned ? this.availableModules : this.assignedModules;

    const moduleIndex = sourceArray.findIndex(m => m.moduleId === moduleId);
    if (moduleIndex === -1) return;

    const module = sourceArray[moduleIndex];
    const permissions = [...module.permissions];

    // Supprimer le module source
    sourceArray.splice(moduleIndex, 1);

    // Ajouter ou fusionner dans le module cible
    const targetModuleIndex = targetArray.findIndex(m => m.moduleId === moduleId);
    if (targetModuleIndex >= 0) {
      targetArray[targetModuleIndex].permissions.push(...permissions);
    } else {
      targetArray.push({
        moduleId: moduleId,
        moduleTitle: module.moduleTitle,
        permissions: permissions
      });
    }

    this.updateAllSelectedStates();
  }

  // Méthodes utilitaires
  private removePermissionFromAvailable(permission: ModulePermissionItem) {
    const moduleIndex = this.availableModules.findIndex(m => m.moduleId === permission.moduleId);
    if (moduleIndex >= 0) {
      const permissionIndex = this.availableModules[moduleIndex].permissions.findIndex(
        p => p.permissionType === permission.permissionType
      );
      if (permissionIndex >= 0) {
        this.availableModules[moduleIndex].permissions.splice(permissionIndex, 1);
        if (this.availableModules[moduleIndex].permissions.length === 0) {
          this.availableModules.splice(moduleIndex, 1);
        }
      }
    }
  }

  private removePermissionFromAssigned(permission: ModulePermissionItem) {
    const moduleIndex = this.assignedModules.findIndex(m => m.moduleId === permission.moduleId);
    if (moduleIndex >= 0) {
      const permissionIndex = this.assignedModules[moduleIndex].permissions.findIndex(
        p => p.permissionType === permission.permissionType
      );
      if (permissionIndex >= 0) {
        this.assignedModules[moduleIndex].permissions.splice(permissionIndex, 1);
        if (this.assignedModules[moduleIndex].permissions.length === 0) {
          this.assignedModules.splice(moduleIndex, 1);
        }
      }
    }
  }

  private addPermissionToAvailable(permission: ModulePermissionItem) {
    const moduleIndex = this.availableModules.findIndex(m => m.moduleId === permission.moduleId);
    if (moduleIndex >= 0) {
      this.availableModules[moduleIndex].permissions.push(permission);
    } else {
      this.availableModules.push({
        moduleId: permission.moduleId,
        moduleTitle: permission.moduleTitle,
        permissions: [permission]
      });
    }
  }

  private addPermissionToAssigned(permission: ModulePermissionItem) {
    const moduleIndex = this.assignedModules.findIndex(m => m.moduleId === permission.moduleId);
    if (moduleIndex >= 0) {
      this.assignedModules[moduleIndex].permissions.push(permission);
    } else {
      this.assignedModules.push({
        moduleId: permission.moduleId,
        moduleTitle: permission.moduleTitle,
        permissions: [permission]
      });
    }
  }

  private cleanEmptyModules() {
    this.availableModules = this.availableModules.filter(module => module.permissions.length > 0);
    this.assignedModules = this.assignedModules.filter(module => module.permissions.length > 0);
  }

  private updateAllSelectedStates() {
    this.availableModules.forEach(module => {
      module.allSelected = module.permissions.length === this.permissionTypes.length;
    });
    this.assignedModules.forEach(module => {
      module.allSelected = module.permissions.length === this.permissionTypes.length;
    });
  }

  // Filtrage
  get filteredAvailableModules(): ModulePermissions[] {
    if (!this.availableSearchTerm) return this.availableModules;

    return this.availableModules.map(module => ({
      ...module,
      permissions: module.permissions.filter(permission =>
        module.moduleTitle.toLowerCase().includes(this.availableSearchTerm.toLowerCase()) ||
        permission.permissionType.toLowerCase().includes(this.availableSearchTerm.toLowerCase())
      )
    })).filter(module => module.permissions.length > 0);
  }

  get filteredAssignedModules(): ModulePermissions[] {
    if (!this.assignedSearchTerm) return this.assignedModules;

    return this.assignedModules.map(module => ({
      ...module,
      permissions: module.permissions.filter(permission =>
        module.moduleTitle.toLowerCase().includes(this.assignedSearchTerm.toLowerCase()) ||
        permission.permissionType.toLowerCase().includes(this.assignedSearchTerm.toLowerCase())
      )
    })).filter(module => module.permissions.length > 0);
  }

  // Classes CSS pour les badges de permissions
  getPermissionClass(type: PermissionType): string {
    return {
      create: 'badge-create',
      update: 'badge-update',
      view: 'badge-view',
      delete: 'badge-delete'
    }[type] || 'badge-secondary';
  }

  // Icônes pour les types de permissions
  getPermissionIcon(type: PermissionType): string {
    return {
      create: 'fas fa-plus',
      update: 'fas fa-edit',
      view: 'fas fa-eye',
      delete: 'fas fa-trash'
    }[type] || 'fas fa-question';
  }

  // Actions principales
  resetPermissions() {
    this.assignedModules = JSON.parse(JSON.stringify(this.initialAssignedModules));
    this.processPermissions([]);
    this.loadRolePermissions();
  }

  refreshPermissions() {
    this.loadRolePermissions();
  }

  savePermissions() {
    const grouped: { [moduleId: number]: Permission } = {};

    // Grouper les permissions par module
    this.assignedModules.forEach(module => {
      if (!grouped[module.moduleId]) {
        grouped[module.moduleId] = {
          create: false,
          update: false,
          delete: false,
          view: false
        };
      }

      module.permissions.forEach(permission => {
        grouped[module.moduleId][permission.permissionType] = true;
      });
    });

    // Sauvegarder chaque module
    const savePromises = Object.entries(grouped).map(([moduleIdStr, permission]) => {
      const moduleId = +moduleIdStr;

      return this.permissionService.addPermission(permission, this.idRole, moduleId).toPromise()
        .then(data => {
          this.updateLocalStoragePermission(data, moduleId);

          // Mettre à jour le sidebar si c'est l'utilisateur actuel
          const item = localStorage.getItem('myLSkey');
          if (item) {
            let currentUser: any = JSON.parse(atob(item));
            if (this.idRole == currentUser.idRole) {
              return this.moduleService.updateStatusByRole(this.idRole).toPromise()
                .then(() => {
                  return this.sidebar.getAllModuleTitles().toPromise();
                })
                .then(modules => {
                  if (modules) {
                    this.sidebar.generateSidebarData(modules);
                  }
                  return Promise.resolve();
                });
            }
          }
          return Promise.resolve();
        });
    });

    Promise.all(savePromises)
      .then(() => {
        console.log('Toutes les permissions ont été sauvegardées avec succès');
        this.loadRolePermissions();
        this.updatePermission();
        // ✅ Navigate after save is complete
        this.router.navigate(['/user-management/roles-permissions']);
      })
      .catch(err => {
        console.error('Erreur lors de la sauvegarde des permissions:', err);
      });
  }

  private updateLocalStoragePermission(data: any, moduleId: number) {
    if (!this.permissionDto) {
      this.permissionDto = {} as PermissionDto;
    }

    this.permissionDto.id = data.id ?? 0;
    this.permissionDto.update = data.update;
    this.permissionDto.create = data.create;
    this.permissionDto.delete = data.delete;
    this.permissionDto.view = data.view;
    this.permissionDto.moduleId = moduleId;
    this.permissionDto.roleId = this.idRole;

    this.moduleService.getModuleById(moduleId).subscribe(moduleData => {
      this.permissionDto.moduleTitle = moduleData.title;

      const item = localStorage.getItem('myLSkey');
      if (item) {
        let currentUser: any = JSON.parse(atob(item));

        if (!Array.isArray(currentUser.permission)) {
          currentUser.permission = [];
        }

        const existingIndex = currentUser.permission.findIndex(
          (p: any) => p.moduleId === moduleId
        );

        if (existingIndex !== -1) {
          currentUser.permission[existingIndex] = this.permissionDto;
        } else {
          currentUser.permission.push(this.permissionDto);
        }

        localStorage.setItem('myLSkey', btoa(JSON.stringify(currentUser)));
      }
    });
  }

  get availableCount(): number {
    return this.availableModules.reduce((sum, module) => sum + module.permissions.length, 0);
  }

  get assignedCount(): number {
    return this.assignedModules.reduce((sum, module) => sum + module.permissions.length, 0);
  }

  // TrackBy functions for performance optimization
  trackByModuleId(index: number, item: ModulePermissions): number {
    return item.moduleId;
  }

  trackByPermission(index: number, item: ModulePermissionItem): string {
    return `${item.moduleId}-${item.permissionType}`;
  }

  updatePermission() {
    const item = localStorage.getItem('myLSkey');

    if (item) {
      let currentUser: any = JSON.parse(atob(item));

      this.idUser = currentUser.userId;
      console.log("idUser pour update", this.idUser);

      this.autheService.refreshPermissionDtoList(this.idUser).subscribe({
        next: (response) => {
          currentUser.permission = response;

          localStorage.setItem('myLSkey', btoa(JSON.stringify(currentUser)));

          console.log("les permissions", response);
        },
        error: (err) => {
          console.error("Erreur lors de l'update des permissions:", err);
        }
      });
    } else {
      console.warn("Aucun utilisateur trouvé dans localStorage");
    }
  }

  // ✅ UPDATED: Simplified navigation method
  saveAndNavigate() {
    this.savePermissions();
  }
}