import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {DepartementComponent} from "../departement/departement.component";
import {DetaisDepartementComponent} from "../departement/detais-departement/detais-departement.component";
import {AddDepartementComponent} from "../departement/add-departement/add-departement.component";
import {ClientComponent} from "./client.component";
import {ListCategorieClientComponent} from "./list-categorie-client/list-categorie-client.component";
import {AddClientComponent} from "./add-client/add-client.component";
import {AddCategorieClientComponent} from "./add-categorie-client/add-categorie-client.component";

const routes: Routes = [
  { path: '', redirectTo: 'listClient', pathMatch: 'full' },
  {
    path: 'listClient',
    component: ClientComponent,
  }, {
    path: 'listCategorieClient',
    component: ListCategorieClientComponent,
  },{
    path: 'addClient',
    component: AddClientComponent,
  },{
    path: 'addCategorie',
    component: AddCategorieClientComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClientRoutingModule { }
