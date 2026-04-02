import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListCommandeVenteComponent } from './list-commande-vente/list-commande-vente.component';
import { AddCommandeVenteComponent } from './add-commande-vente/add-commande-vente.component';

const routes: Routes = [
  { path: '', redirectTo: 'list', pathMatch: 'full' },
  { path: 'list', component: ListCommandeVenteComponent },
  { path: 'add', component: AddCommandeVenteComponent },
  { path: 'edit/:id', component: AddCommandeVenteComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommandeVenteRoutingModule {}
