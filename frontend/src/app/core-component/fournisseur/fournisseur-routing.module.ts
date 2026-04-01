import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FournisseurComponent } from './fournisseur.component';
import { ListCategorieFournisseurComponent } from './list-categorie-fournisseur/list-categorie-fournisseur.component';
import { AddFournisseurComponent } from './add-fournisseur/add-fournisseur.component';
import { AddCategorieFournisseurComponent } from './add-categorie-fournisseur/add-categorie-fournisseur.component';

const routes: Routes = [
  { path: '', redirectTo: 'listFournisseur', pathMatch: 'full' },
  { path: 'listFournisseur', component: FournisseurComponent },
  { path: 'listCategorieFournisseur', component: ListCategorieFournisseurComponent },
  { path: 'addFournisseur', component: AddFournisseurComponent },
  { path: 'addCategorieFournisseur', component: AddCategorieFournisseurComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FournisseurRoutingModule {}
