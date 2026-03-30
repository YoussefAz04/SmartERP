import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserManagementRoutingModule } from './user-management-routing.module';
import { UserManagementComponent } from './user-management.component';
import { DeleteAccountComponent } from './delete-account/delete-account.component';
import { RolesPermissionsComponent } from './roles-permissions/roles-permissions.component';
import { PermissionsComponent } from './permissions/permissions.component';
import { UsersComponent } from './users/users.component';
import { DetaisUsersComponent } from './detais-users/detais-users.component';
import { AddUserComponent } from './add-user/add-user.component';
import { sharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [
    UserManagementComponent,
    DeleteAccountComponent,
    RolesPermissionsComponent,
    PermissionsComponent,
  ],
  imports: [
    CommonModule,
    UserManagementRoutingModule,
    sharedModule,
    UsersComponent,
    DetaisUsersComponent,
    AddUserComponent,
  ]
})
export class UserManagementModule { }