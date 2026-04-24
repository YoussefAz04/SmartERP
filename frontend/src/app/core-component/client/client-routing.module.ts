import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import {ClientComponent} from "./client.component";
import {ListCategorieClientComponent} from "./list-categorie-client/list-categorie-client.component";
import {AddClientComponent} from "./add-client/add-client.component";
import {AddCategorieClientComponent} from "./add-categorie-client/add-categorie-client.component";
import {EditClientComponent} from "./edit-client/edit-client.component";
import {ViewClientComponent} from "./view-client/view-client.component";
import { RoleGuard } from '../../core/guard/role/role.guard';

const routes: Routes = [
  { path: '', redirectTo: 'listClient', pathMatch: 'full' },
  {
    path: 'listClient',
    component: ClientComponent,
  }, {
    path: 'listCategorieClient',
    component: ListCategorieClientComponent,
    canActivate: [RoleGuard],
    data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
  },{
    path: 'addClient',
    component: AddClientComponent,
    canActivate: [RoleGuard],
    data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
  },{
    path: 'addCategorieClient',
    component: AddCategorieClientComponent,
    canActivate: [RoleGuard],
    data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
  },{
    path: 'editClient/:id',
    component: EditClientComponent,
    canActivate: [RoleGuard],
    data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
  },{
    path: 'viewClient/:id',
    component: ViewClientComponent,
  },
  
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClientRoutingModule { }
