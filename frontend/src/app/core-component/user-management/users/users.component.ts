import {Component, OnInit} from '@angular/core';
import {UserService} from "../../../auth/service/user/user.service";
import {UserDTO} from "../../../model/userDTO.model";
import {Router} from "@angular/router";
import {RoleService} from "../../../core/service/role/role.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Role} from "../../../model/role.model";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
})
export class UsersComponent implements OnInit{
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
   permissionUsers!: PermissionDto;

  constructor(private userService: UserService,
              private router : Router,
              private roleService: RoleService,
              private fb: FormBuilder,

  ) {}

  ngOnInit(): void {
    this.loadUsers();
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
        console.log(this.allUsers);
      },
      error: (err) => console.error('Erreur chargement utilisateurs', err)
    });
  }

  searchData(query: string): void {
    const lower = query.toLowerCase();
    this.tableData = this.allUsers.filter(user =>
      user.username?.toLowerCase().includes(lower) ||
      user.email?.toLowerCase().includes(lower)
    );
  }

  sortData(event: any): void {
    const field = event.active;
    const direction = event.direction;

    this.tableData.sort((a: any, b: any) => {
      const aField = a[field] || '';
      const bField = b[field] || '';
      const comparison = aField.localeCompare(bField);
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

  confirmColor(): void {
    alert('Fonction de suppression à implémenter');
  }

  togglePassword(index: number): void {
    this.password[index] = !this.password[index];
  }

  addUsers() {
    this.router.navigate(['/user-management/addUsers'])
  }

  viewUsers(id:any) {
    this.router.navigate(['/user-management/detaisUsers',id])
  }



  idUserAssigner!: number;
  roles: Role[] = [];
  assignRoleForm!: FormGroup;


  userAssigner(id: any) {
    this.idUserAssigner = id;
    console.log("ID user sélectionné :", this.idUserAssigner);



    this.roleService.getAllRoles().subscribe(data => {
      this.roles = data;
      console.log("Rôles récupérés :", this.roles);
    });
  }

  onSubmitAssignRole() {
    const roleId = this.assignRoleForm.value.roleId;

    if (this.idUserAssigner && roleId) {
      this.roleService.assignRoleToUser(this.idUserAssigner, roleId).subscribe(()=>{
        this.loadUsers();
      }
      );
    }
  }
  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle.includes("gestion users")){
          this.permissionUsers = per ;
          console.log("permission Users" ,this.permissionUsers);
        }
      })
    }
  }




}
