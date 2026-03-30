import {Component, OnInit} from '@angular/core';
import {UserService} from "../../../auth/service/user/user.service";
import {UserDTO} from "../../../model/userDTO.model";
import {Router} from "@angular/router";
import {RoleService} from "../../../core/service/role/role.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Role} from "../../../model/role.model";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ReactiveFormsModule } from '@angular/forms';
import { CustomPaginationComponent } from '../custom-pagination/custom-pagination.component';

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
  
  permissionUsers: PermissionDto = {
    create: false,
    view: false,
    update: false,
    delete: false,
    moduleTitle: ''
  } as PermissionDto;
  
  roles: Role[] = [];
  idUserAssigner!: number;
  assignRoleForm!: FormGroup;

  constructor(
    private userService: UserService,
    private router: Router,
    private roleService: RoleService,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadRoles();
    this.assignRoleForm = this.fb.group({
      roleId: ['']
    });
    this.getUserAuthData();
  }

  loadUsers(): void {
    this.userService.getAllUsersDTO().subscribe({
      next: (users) => {
        this.allUsers = users;
        this.tableData = [...users];
        console.log('Users loaded:', this.allUsers);
      },
      error: (err: any) => {
        console.error('Erreur chargement utilisateurs', err);
        alert('Erreur lors du chargement des utilisateurs');
      }
    });
  }

  loadRoles(): void {
    this.roleService.getAllRoles().subscribe({
      next: (roles) => {
        this.roles = roles;
        console.log('Roles loaded:', this.roles);
      },
      error: (err: any) => {
        console.error('Error loading roles:', err);
        alert('Erreur lors du chargement des rôles');
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
      alert('❌ ID utilisateur invalide');
      return;
    }

    if (this.isDeleting) {
      alert('Opération en cours...');
      return;
    }

    const confirmed = confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible.');
    
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
          
          alert('✓ Utilisateur supprimé avec succès');
          this.loadUsers();
        },
        error: (err: any) => {
          this.isDeleting = false;
          this.deletingUserId = null;
          
          const errorMsg = err.error?.message || err.message || 'Erreur inconnue';
          console.error('Erreur suppression:', err);
          alert('✗ Erreur lors de la suppression: ' + errorMsg);
        }
      });
    }
  }

  togglePassword(index: number): void {
    this.password[index] = !this.password[index];
  }

  addUsers(): void {
    this.router.navigate(['/user-management/addUsers']);
  }

  viewUsers(id: any): void {
    this.router.navigate(['/user-management/detaisUsers', id]);
  }

  userAssigner(id: any): void {
    this.idUserAssigner = id;
    console.log("ID user sélectionné:", this.idUserAssigner);
    
    this.roleService.getAllRoles().subscribe({
      next: (data) => {
        this.roles = data;
        console.log("Rôles récupérés:", this.roles);
      },
      error: (err: any) => {
        console.error('Error loading roles:', err);
        alert('Erreur lors du chargement des rôles');
      }
    });
  }

  onSubmitAssignRole(): void {
    const roleId = this.assignRoleForm.value.roleId;
    
    if (!this.idUserAssigner) {
      alert('Veuillez sélectionner un utilisateur');
      return;
    }

    if (!roleId) {
      alert('Veuillez sélectionner un rôle');
      return;
    }

    this.roleService.assignRoleToUser(this.idUserAssigner, roleId).subscribe({
      next: () => {
        console.log('Role assigned successfully');
        alert('✓ Rôle assigné avec succès');
        this.assignRoleForm.reset();
        this.loadUsers();
      },
      error: (err: any) => {
        console.error('Error assigning role:', err);
        alert('✗ Erreur lors de l\'assignation du rôle');
      }
    });
  }

  getUserAuthData(): void {
    const item = localStorage.getItem('myLSkey');
    
    if (item) {
      try {
        const currentUser: ResponseDto = JSON.parse(atob(item));
        console.log('User connecté:', currentUser?.name);
        console.log('Token:', currentUser?.token);
        console.log('Permissions:', currentUser?.permission);

        if (currentUser?.permission && Array.isArray(currentUser.permission)) {
          currentUser.permission.forEach(per => {
            if (per.moduleTitle.toLowerCase().includes("gestion users") || 
                per.moduleTitle.toLowerCase().includes("users")) {
              this.permissionUsers = per;
              console.log("Permission Users:", this.permissionUsers);
            }
          });
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    } else {
      console.warn('No user found in localStorage');
    }
  }
}