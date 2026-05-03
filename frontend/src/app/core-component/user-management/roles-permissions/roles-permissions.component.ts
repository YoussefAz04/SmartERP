import {Component, OnInit} from '@angular/core';
import { Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import {
  DataService,

  routes,
} from 'src/app/core/core.index';
import { SidebarService } from 'src/app/core/service/sidebar/sidebar.service';
import { PaginationService } from 'src/app/shared/shared.index';
import Swal from 'sweetalert2';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RoleService} from "../../../core/service/role/role.service";
import {Role} from "../../../model/role.model";
import {UserService} from "../../../auth/service/user/user.service";
import {UserDTO} from "../../../model/userDTO.model";
import {forkJoin} from "rxjs";
import {AuthService} from "../../../auth/service/auth.service";
import {NotificationService} from "../../../shared/notification/notification.service";
interface data {
  value: string;
}

@Component({
  selector: 'app-roles-permissions',
  templateUrl: './roles-permissions.component.html',
  styleUrl: './roles-permissions.component.scss',
})
export class RolesPermissionsComponent implements OnInit{
  initChecked = false;
  public routes = routes;
  isCollapsed: boolean = false;
  toggleCollapse() {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }

  public selectedValue1 = '';
  public selectedValue2 = '';

  selectedList1: data[] = [
    { value: 'Sort by Date' },
    { value: 'Newest' },
    { value: 'Oldest' },
  ];
  selectedList2: data[] = [
    { value: 'Choose Role' },
    { value: 'Admin' },
    { value: 'Shop Owner' },
  ];
  // pagination variables
  public tableData: Array<Role> = [];
  public pageSize = 10;
  public serialNumberArray: Array<number> = [];
  public totalData = 0;
  showFilter = false;
  dataSource!: MatTableDataSource<Role>;
  public searchDataValue = '';
  roleForm!: FormGroup;
  editRoleForm!: FormGroup;
  currentRoleId!: number;

  // Manage Users
  allUsers: UserDTO[] = [];
  selectedUserIds: Set<number> = new Set<number>();
  currentRoleForManage: Role | null = null;
  manageUsersLoading = false;

  // Role icon & color map
  private roleIconMap: Record<string, string> = {
    SUPER_ADMIN: 'icon-award',
    ADMIN: 'icon-shield',
    EMPLOYEE: 'icon-briefcase',
  };

  private roleColorMap: Record<string, string> = {
    SUPER_ADMIN: 'role-super-admin',
    ADMIN: 'role-admin',
    EMPLOYEE: 'role-employee',
  };

  getRoleIcon(role: Role): string {
    return this.roleIconMap[role.roleType || ''] || 'icon-tag';
  }

  getRoleColorClass(role: Role): string {
    return this.roleColorMap[role.roleType || ''] || 'role-custom';
  }

  //** / pagination variables

  constructor(
    private data: DataService,
    private pagination: PaginationService,
    private router: Router,
    private sidebar: SidebarService,
    private fb: FormBuilder,
    private roleService : RoleService,
    private userService: UserService,
    private authService: AuthService,
    private notify: NotificationService
  ) {
  }

  isSuperAdmin(): boolean {
    return this.authService.getRoles().includes('SUPER_ADMIN');
  }


  public sortData(sort: Sort) {
    const data = this.tableData.slice();
    if (!sort.active || sort.direction === '') {
      this.tableData = data;
    } else {
      this.tableData = data.sort((a, b) => {
        const aValue = (a as never)[sort.active];
        const bValue = (b as never)[sort.active];
        return (aValue < bValue ? -1 : 1) * (sort.direction === 'asc' ? 1 : -1);
      });
    }
  }

  public searchData(value: string): void {
    this.dataSource.filter = value.trim().toLowerCase();
    this.tableData = this.dataSource.filteredData;
  }

  confirmColor(roleId : any) {
    const swalWithBootstrapButtons = Swal.mixin({
      customClass: {
        confirmButton: ' btn btn-success',
        cancelButton: 'me-2 btn btn-danger',
      },
      buttonsStyling: false,
    });

    swalWithBootstrapButtons
      .fire({
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        confirmButtonText: 'Yes, delete it!',
        showCancelButton: true,
        cancelButtonText: 'Cancel',
        reverseButtons: true,
      })
      .then((result) => {
        if (result.isConfirmed) {
          // 🔥 Appel réel au backend pour supprimer
          this.roleService.deleteRole(roleId).subscribe({
            next: () => {
              swalWithBootstrapButtons.fire(
                'Deleted!',
                'The role has been deleted.',
                'success'
              );

              this.refreshRoles();
            },
            error: (err) => {
              console.error('Erreur lors de la suppression', err);
              swalWithBootstrapButtons.fire(
                'Error',
                'Erreur lors de la suppression du rôle.',
                'error'
              );
            },
          });
        } else if (result.dismiss === Swal.DismissReason.cancel) {
          swalWithBootstrapButtons.fire(
            'Cancelled',
            'Your imaginary file is safe :)',
            'error'
          );
        }
      });
  }
  public filter = false;
  openFilter() {
    this.filter = !this.filter;
  }



  ////////////////////////////////

  openEditModal(role: Role): void {
    console.log(
      role
    );
    this.currentRoleId = role.id!;
    this.editRoleForm.patchValue({
      name: role.name
    });
  }

  manageUsersForRole(role: Role): void {
    this.currentRoleForManage = role;
    this.manageUsersLoading = true;
    this.userService.getAllUsersDTO().subscribe({
      next: (users) => {
        this.allUsers = users;
        this.selectedUserIds = new Set(
          users
            .filter(u => u.roles && u.roles.includes(role.name!))
            .map(u => u.id)
        );
        this.manageUsersLoading = false;
      },
      error: () => {
        this.manageUsersLoading = false;
      }
    });
  }

  isUserSelected(userId: number): boolean {
    return this.selectedUserIds.has(userId);
  }

  toggleUserSelection(userId: number): void {
    if (this.selectedUserIds.has(userId)) {
      this.selectedUserIds.delete(userId);
    } else {
      this.selectedUserIds.add(userId);
    }
  }

  assignUsersToRole(): void {
    if (!this.currentRoleForManage) return;
    const roleId = this.currentRoleForManage.id!;

    const usersWithRole = this.allUsers.filter(
      u => u.roles && u.roles.includes(this.currentRoleForManage!.name!)
    );
    const currentUserIds = new Set(usersWithRole.map(u => u.id));

    const toAdd = this.allUsers.filter(
      u => this.selectedUserIds.has(u.id) && !currentUserIds.has(u.id)
    );
    const toRemove = this.allUsers.filter(
      u => !this.selectedUserIds.has(u.id) && currentUserIds.has(u.id)
    );

    const addRequests = toAdd.map(u => this.roleService.assignRoleToUser(u.id, roleId));
    const removeRequests = toRemove.map(u => this.roleService.removeRoleFromUser(u.id, roleId));

    const allRequests = [...addRequests, ...removeRequests];

    if (allRequests.length === 0) {
      this.notify.info('No changes were made.');
      this.closeManageUsersModal();
      return;
    }

    forkJoin(allRequests).subscribe({
      next: () => {
        this.closeManageUsersModal();
        this.notify.success('User assignments updated successfully.');
      },
      error: () => {
        this.notify.error('An error occurred while updating user assignments.');
      }
    });
  }

  closeManageUsersModal(): void {
    const modalEl = document.getElementById('manage-users-modal');
    if (modalEl) {
      const modal = (window as any).bootstrap?.Modal?.getInstance(modalEl);
      if (modal) {
        modal.hide();
      }
    }
  }

  ngOnInit(): void {
    this.roleForm = this.fb.group({
      name: ['', Validators.required]
    });
    this.editRoleForm = this.fb.group({
      name: ['', Validators.required]
    });
    this.refreshRoles();
  }

  onSubmit() {
   this.roleService.createRole(this.roleForm.value).subscribe(data => {
     console.log(data);
     this.refreshRoles();
   })
  }

  onUpdateRole() {
     this.roleService.updateRole(this.currentRoleId,this.editRoleForm.value).subscribe(data => {
       console.log(data);
       this.refreshRoles();
     })
  }

  private refreshRoles(): void {
    this.roleService.getAllRoles().subscribe(data => {
      this.tableData = data.filter(r => r.name !== 'Manager');
      this.dataSource = new MatTableDataSource(this.tableData);
    });
  }
}
