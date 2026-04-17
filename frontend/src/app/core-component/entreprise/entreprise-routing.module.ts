import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {EntrepriseComponent} from "./entreprise.component";
import {DetaisEntrepriseComponent} from "./detais-entreprise/detais-entreprise.component";
import {AddEntrepriseComponent} from "./add-entreprise/add-entreprise.component";
import { RoleGuard } from '../../core/guard/role/role.guard';

const routes: Routes = [
  { path: '', redirectTo: 'listEntreprise', pathMatch: 'full' },
  {
    path: 'listEntreprise',
    component: EntrepriseComponent,
  },
  {
    path: 'detaisEntreprise/:id',
    component: DetaisEntrepriseComponent,
    canActivate: [RoleGuard],
    data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
  },  {
    path: 'addEntreprise',
    component: AddEntrepriseComponent,
    canActivate: [RoleGuard],
    data: { requiredRoles: ['SUPER_ADMIN'] },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EntrepriseRoutingModule { }
