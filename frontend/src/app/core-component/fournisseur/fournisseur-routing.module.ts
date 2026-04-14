import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FournisseurComponent } from './fournisseur.component';
import { ListCategorieFournisseurComponent } from './list-categorie-fournisseur/list-categorie-fournisseur.component';
import { AddFournisseurComponent } from './add-fournisseur/add-fournisseur.component';
import { AddCategorieFournisseurComponent } from './add-categorie-fournisseur/add-categorie-fournisseur.component';
import { ViewFournisseurComponent } from './view-fournisseur/view-fournisseur.component';
import { EditFournisseurComponent } from './edit-fournisseur/edit-fournisseur.component';
import { EditCategorieFournisseurComponent } from './edit-categorie-fournisseur/edit-categorie-fournisseur.component';
import { RoleGuard } from '../../core/guard/role/role.guard';

const routes: Routes = [
  { path: '', redirectTo: 'listFournisseur', pathMatch: 'full' },
  { path: 'listFournisseur', component: FournisseurComponent },
  { path: 'listCategorieFournisseur', component: ListCategorieFournisseurComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
  { path: 'addFournisseur', component: AddFournisseurComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
  { path: 'addCategorieFournisseur', component: AddCategorieFournisseurComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
  { path: 'viewFournisseur/:id', component: ViewFournisseurComponent },
  { path: 'editFournisseur/:id', component: EditFournisseurComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
  { path: 'editCategorieFournisseur/:id', component: EditCategorieFournisseurComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FournisseurRoutingModule {}
