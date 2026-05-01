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
  styleUrl: './add-user.component.scss'
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

  loadModules(): void {
    this.moduleService.getAllModules().subscribe({
      next: (modules) => {
        this.modules = modules;
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
      this.notification.warning('Please fill all required fields');
      return;
    }

    if (this.isSuperAdmin && !this.selectedEnterpriseId) {
      this.notification.error('❌ Please select an enterprise to assign the user to.');
      return;
    }

    if (!this.isSuperAdmin && this.currentEnterpriseId === null) {
      this.notification.error('❌ Enterprise context is missing. Please log out and log in again.');
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
        this.notification.success('✓ Employee created successfully');
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
