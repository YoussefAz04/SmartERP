import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../../auth/service/user/user.service';
import { UserDTO } from '../../../model/userDTO.model';
import { ResponseDto } from '../../../model/reponseDto.model';
import { PermissionDto } from '../../../model/permissionDto.model';
import { NotificationService } from '../../../shared/notification/notification.service';
import { RoleService } from '../../../core/service/role/role.service';
import { Role } from '../../../model/role.model';

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
  selectedRoleNames: string[] = [];
  availableRoles: Role[] = [];
  rolesLoading = true;
  rolesLoadError = false;
  isSuperAdmin = false;
  isAdmin = false;

  permissionUsers: PermissionDto = {
    create: false,
    view: false,
    update: false,
    delete: false,
    moduleTitle: ''
  } as PermissionDto;

  constructor(private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: NotificationService,
    private roleService: RoleService) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.params['id'];
    this.getUserAuthData();
    this.loadRoles();
    this.loadUserDetails();
  }

  loadUserDetails(): void {
    this.isLoading = true;
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
  }

  loadRoles(): void {
    this.rolesLoading = true;
    this.rolesLoadError = false;
    this.roleService.getAllRoles().subscribe({
      next: (roles) => {
        if (this.isAdmin && !this.isSuperAdmin) {
          this.availableRoles = roles.filter(r =>
            r.roleType === 'EMPLOYEE' || r.roleType === 'ADMIN'
          );
        } else {
          this.availableRoles = roles;
        }
        this.rolesLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading roles:', err);
        this.rolesLoading = false;
        this.rolesLoadError = true;
      }
    });
  }

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

  onSubmit(): void {
    if (!this.user || !this.userId) {
      this.notification.error('❌ Données utilisateur manquantes');
      return;
    }

    if (this.isSubmitting) {
      this.notification.info('Opération en cours...');
      return;
    }

    this.isSubmitting = true;

    this.user.roles = [...this.selectedRoleNames];

    this.userService.updateUser(this.userId, this.user).subscribe({
      next: (response: any) => {
        this.isSubmitting = false;
        this.notification.success('✓ Utilisateur mis à jour avec succès');
        this.router.navigate(['/user-management/users']);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        const errorMsg = err.error?.message || err.message || 'Erreur inconnue';
        this.notification.error('✗ Erreur lors de la mise à jour: ' + errorMsg);
      }
    });
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