import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserManagementComponent } from './user-management.component';
import { RolesPermissionsComponent } from './roles-permissions/roles-permissions.component';
import { DeleteAccountComponent } from './delete-account/delete-account.component';
import { UsersComponent } from './users/users.component';
import { PermissionsComponent } from './permissions/permissions.component';
import {AddUserComponent} from "./add-user/add-user.component";
import {DetaisUsersComponent} from "./detais-users/detais-users.component";

const routes: Routes = [
  {
    path: '',
    component: UserManagementComponent,
    children: [
      {
        path: 'roles-permissions',
        component: RolesPermissionsComponent,
      },
      {
        path: 'delete-account',
        component: DeleteAccountComponent,
      },
      {
        path: 'users',
        component: UsersComponent,
      },
      {
        path: 'permissions/:id',
        component: PermissionsComponent,
      },{
        path: 'addUsers',
        component: AddUserComponent,
      },{
        path: 'detaisUsers/:id',
        component: DetaisUsersComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UserManagementRoutingModule {}
