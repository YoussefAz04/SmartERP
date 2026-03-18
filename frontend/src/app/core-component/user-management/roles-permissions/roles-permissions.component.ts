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
import {FormBuilder, FormGroup} from "@angular/forms";
import {RoleService} from "../../../core/service/role/role.service";
import {Validators} from "ngx-editor";
import {Role} from "../../../model/role.model";
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


  //** / pagination variables

  constructor(
    private data: DataService,
    private pagination: PaginationService,
    private router: Router,
    private sidebar: SidebarService,
    private fb: FormBuilder,
    private roleService : RoleService
  ) {
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

              this.roleService.getAllRoles().subscribe(data =>{
                this.tableData = data ;
                console.log(data);
              })
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


  ngOnInit(): void {
    this.roleForm = this.fb.group({
      name: ['', Validators.required]
    });
    this.editRoleForm = this.fb.group({
      name: ['', Validators.required]
    });
    this.roleService.getAllRoles().subscribe(data =>{
      this.tableData = data ;
      console.log(data);
    })
  }

  onSubmit() {
   this.roleService.createRole(this.roleForm.value).subscribe(data => {
     console.log(data);
     this.roleService.getAllRoles().subscribe(data =>{
       this.tableData = data ;
       console.log(data);
     })
   })
  }

  onUpdateRole() {
     this.roleService.updateRole(this.currentRoleId,this.editRoleForm.value).subscribe(data => {
       console.log(data);
       this.roleService.getAllRoles().subscribe(data =>{
         this.tableData = data ;
         console.log(data);
       })

     })
  }
}
