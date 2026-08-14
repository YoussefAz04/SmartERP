import {Component, OnInit} from '@angular/core';
import {UserService} from "../../../auth/service/user/user.service";
import {UserDTO} from "../../../model/userDTO.model";
import {Router} from "@angular/router";
import {RoleService} from "../../../core/service/role/role.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Role} from "../../../model/role.model";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CustomPaginationComponent } from '../custom-pagination/custom-pagination.component';
import { AssignRoleDialogComponent } from './assign-role-dialog/assign-role-dialog.component';
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatSortModule,
    MatSelectModule,
    MatTooltipModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    CustomPaginationComponent
  ],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
})
export class UsersComponent implements OnInit {
  tableData: any[] = [];
  allUsers: UserDTO[] = [];
  initChecked: boolean = false;
  searchDataValue: string = '';
  filter: boolean = false;
  selectedValue2 = '';
  selectedValue3 = '';
  selectedValue4 = '';
  selectedValue5 = '';
  selectedValue6 = '';
  selectedValue7 = '';
  isCollapsed = false;
  password: boolean[] = [];
  isDeleting = false;
  deletingUserId: number | null = null;
  isTogglingStatus = false;
  togglingUserId: number | null = null;

  isSuperAdmin = false;
  isAdmin = false;
  currentEnterpriseId: number | null = null;
  currentEnterpriseName: string = '';

  permissionUsers: PermissionDto = {
    create: false,
    view: false,
    update: false,
    delete: false,
    moduleTitle: ''
  } as PermissionDto;

  roles: Role[] = [];
  assignRoleForm!: FormGroup;

  constructor(private userService: UserService,
    private router: Router,
    private roleService: RoleService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.getUserAuthData();
    this.loadUsers();
    this.loadRoles();
    this.assignRoleForm = this.fb.group({
      roleId: ['', Validators.required]
    });
  }

  loadUsers(): void {
    // Backend already filters by enterprise based on the JWT role,
    // so getAllUsersDTO() returns the correct set for both SUPER_ADMIN and ADMIN.
    this.userService.getAllUsersDTO().subscribe({
      next: (users) => {
        this.allUsers = users;
        this.tableData = [...users];
        console.log('Users loaded:', this.allUsers);
      },
      error: (err: any) => {
        console.error('Erreur chargement utilisateurs', err);
        this.notification.error('Erreur lors du chargement des utilisateurs');
      }
    });
  }

  loadRoles(): void {
    this.roleService.getAllRoles().subscribe({
      next: (roles) => {
        if (!this.isSuperAdmin) {
          this.roles = roles.filter(r =>
            r.roleType === 'EMPLOYEE' || r.roleType === 'ADMIN'
          );
        } else {
          this.roles = roles;
        }
        console.log('Roles loaded:', this.roles);
      },
      error: (err: any) => {
        console.error('Error loading roles:', err);
        this.notification.error('Error while loading roles');
      }
    });
  }

  filterByRole(roleName: string): void {
    if (!roleName || roleName === '') {
      this.tableData = [...this.allUsers];
      console.log('Showing all users');
    } else {
      this.tableData = this.allUsers.filter(user =>
        user.roles && user.roles.includes(roleName)
      );
      console.log(`Showing users with role: ${roleName}`, this.tableData);
    }
  }

  searchData(query: string): void {
    const lower = query.toLowerCase();
    this.tableData = this.allUsers.filter(user =>
      user.username?.toLowerCase().includes(lower) ||
      user.email?.toLowerCase().includes(lower)
    );
  }

  sortData(event: Sort): void {
    const field = event.active;
    const direction = event.direction;

    if (!direction) {
      this.tableData = [...this.allUsers];
      return;
    }

    this.tableData.sort((a: any, b: any) => {
      let aField = a[field];
      let bField = b[field];

      if (aField == null) aField = '';
      if (bField == null) bField = '';

      if (typeof aField === 'string') aField = aField.toLowerCase();
      if (typeof bField === 'string') bField = bField.toLowerCase();

      let comparison = 0;
      if (aField > bField) {
        comparison = 1;
      } else if (aField < bField) {
        comparison = -1;
      }

      return direction === 'asc' ? comparison : -comparison;
    });
  }

  toggleCollapse(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  openFilter(): void {
    this.filter = !this.filter;
  }

  selectAll(state: boolean): void {
    this.tableData.forEach(user => user.isSelected = state);
  }

  deleteUser(userId: number): void {
    if (!userId) {
      this.notification.error('❌ ID utilisateur invalide');
      return;
    }

    if (this.isDeleting) {
      this.notification.info('Operation in progress...');
      return;
    }

    this.notification.confirm('Are you sure you want to delete this user? This action cannot be undone.').then(confirmed => {
      if (confirmed) {
        this.isDeleting = true;
        this.deletingUserId = userId;

        this.userService.deleteUser(userId).subscribe({
          next: (response: any) => {
            console.log('User deleted successfully:', response);

            this.allUsers = this.allUsers.filter(u => u.id !== userId);
            this.tableData = this.tableData.filter(u => u.id !== userId);

            this.isDeleting = false;
            this.deletingUserId = null;

            this.notification.success('✓ User supprimé avec succès');
            this.loadUsers();
          },
          error: (err: any) => {
            this.isDeleting = false;
            this.deletingUserId = null;

            const errorMsg = err.error?.message || err.message || 'Unknown error';
            console.error('Erreur suppression:', err);
            this.notification.error('✗ Error during deletion: ' + errorMsg);
          }
        });
      }
    });
  }

  togglePassword(index: number): void {
    this.password[index] = !this.password[index];
  }

  toggleUserStatus(user: UserDTO): void {
    if (!user || user.id == null) {
      this.notification.error('❌ ID utilisateur invalide');
      return;
    }

    if (this.isTogglingStatus) {
      this.notification.info('Operation in progress...');
      return;
    }

    const newEnabled = !user.enabled;
    const actionLabel = newEnabled ? 'activate' : 'désactivate';
    const message = newEnabled
      ? `Êtes-vous sûr de vouloir activate le compte de "${user.username}" ?`
      : `Êtes-vous sûr de vouloir désactivate le compte de "${user.username}" ? The user will no longer be able to sign in.`;

    this.notification.confirm(message).then(confirmed => {
      if (!confirmed) {
        return;
      }

      this.isTogglingStatus = true;
      this.togglingUserId = user.id;

      this.userService.setUserStatus(user.id, newEnabled).subscribe({
        next: () => {
          user.enabled = newEnabled;
          const idxAll = this.allUsers.findIndex(u => u.id === user.id);
          if (idxAll !== -1) {
            this.allUsers[idxAll].enabled = newEnabled;
          }
          this.isTogglingStatus = false;
          this.togglingUserId = null;
          this.notification.success(
            newEnabled
              ? '✓ User activated successfully'
              : '✓ User deactivated successfully'
          );
        },
        error: (err: any) => {
          this.isTogglingStatus = false;
          this.togglingUserId = null;
          const errorMsg = err.error?.error || err.error?.message || err.message || 'Unknown error';
          console.error(`Erreur ${actionLabel} utilisateur:`, err);
          this.notification.error(`Error during operation: ${errorMsg}`);
        }
      });
    });
  }

  addUsers(): void {
    this.router.navigate(['/user-management/addUsers']);
  }

  viewUsers(id: any): void {
    this.router.navigate(['/user-management/detaisUsers', id]);
  }

  userAssigner(id: any): void {
    console.log("Opening assign role dialog for user:", id);

    this.assignRoleForm.reset();

    const dialogRef = this.dialog.open(AssignRoleDialogComponent, {
      width: '400px',
      data: {
        userId: id,
        roles: this.roles,
        currentEnterpriseId: this.currentEnterpriseId,
        isSuperAdmin: this.isSuperAdmin,
        isAdmin: this.isAdmin
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && result.roleId) {
        this.assignRoleToUser(id, result.roleId);
      }
    });
  }

  private assignRoleToUser(userId: number, roleId: number): void {
    this.roleService.assignRoleToUser(userId, roleId).subscribe({
      next: () => {
        console.log('Role assigned successfully');
        this.notification.success('Role assigned successfully');
        this.assignRoleForm.reset();
        this.loadUsers();
      },
      error: (err: any) => {
        console.error('Error assigning role:', err);
        const errorMsg = err.error?.message || 'Unknown error';
        this.notification.error('✗ Erreur lors de l\'assignation du rôle: ' + errorMsg);
      }
    });
  }

  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');

    if (item) {
      try {
        const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));
        console.log('Connected user:', currentUser?.name);

        const roles: string[] = Array.isArray(currentUser?.roles) ? currentUser.roles : [];
        this.isSuperAdmin = roles.includes('SUPER_ADMIN');
        this.isAdmin = roles.includes('ADMIN');

        if (currentUser?.entrepriseId) {
          this.currentEnterpriseId = Number(currentUser.entrepriseId);
        }
        if (currentUser?.entrepriseName) {
          this.currentEnterpriseName = currentUser.entrepriseName;
        } else if (currentUser?.entreprise?.nom) {
          this.currentEnterpriseName = currentUser.entreprise.nom;
        }

        if (currentUser?.permission && Array.isArray(currentUser.permission) && currentUser.permission.length > 0) {
          const usersPerm = currentUser.permission.find(
            (p: any) => p.moduleTitle?.toLowerCase().includes('user')
          );
          this.permissionUsers = usersPerm ?? currentUser.permission[0];
          console.log("Permission Users:", this.permissionUsers);
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    } else {
      console.warn('No user found in localStorage');
    }
  }
}