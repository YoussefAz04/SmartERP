import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {DepartementComponent} from "./departement.component";
import {DetaisDepartementComponent} from "./detais-departement/detais-departement.component";
import {AddDepartementComponent} from "./add-departement/add-departement.component";

const routes: Routes = [
  { path: '', redirectTo: 'listDepartement', pathMatch: 'full' },
  {
    path: 'listDepartement',
    component: DepartementComponent,
  }, {
    path: 'detaisDepartement/:id',
    component: DetaisDepartementComponent,
  },{
    path: 'addDepartement',
    component: AddDepartementComponent,
  },
 ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DepartementRoutingModule { }
