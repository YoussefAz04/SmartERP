import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FournisseurComponent } from './fournisseur.component';
import { ListCategorieFournisseurComponent } from './list-categorie-fournisseur/list-categorie-fournisseur.component';
import { AddFournisseurComponent } from './add-fournisseur/add-fournisseur.component';
import { AddCategorieFournisseurComponent } from './add-categorie-fournisseur/add-categorie-fournisseur.component';
import { ViewFournisseurComponent } from './view-fournisseur/view-fournisseur.component';
import { EditFournisseurComponent } from './edit-fournisseur/edit-fournisseur.component';
import { EditCategorieFournisseurComponent } from './edit-categorie-fournisseur/edit-categorie-fournisseur.component';

const routes: Routes = [
  { path: '', redirectTo: 'listFournisseur', pathMatch: 'full' },
  { path: 'listFournisseur', component: FournisseurComponent },
  { path: 'listCategorieFournisseur', component: ListCategorieFournisseurComponent },
  { path: 'addFournisseur', component: AddFournisseurComponent },
  { path: 'addCategorieFournisseur', component: AddCategorieFournisseurComponent },
  { path: 'viewFournisseur/:id', component: ViewFournisseurComponent },
  { path: 'editFournisseur/:id', component: EditFournisseurComponent },
  { path: 'editCategorieFournisseur/:id', component: EditCategorieFournisseurComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FournisseurRoutingModule {}
