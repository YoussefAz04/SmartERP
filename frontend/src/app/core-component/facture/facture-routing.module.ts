import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ListfacturAchatComponent} from "./listfactur-achat/listfactur-achat.component";
import {DetaisfacturAchatComponent} from "./detaisfactur-achat/detaisfactur-achat.component";
import {ListFactureDeVenteComponent} from "./list-facture-de-vente/list-facture-de-vente.component";
import {DetaisFactureDeVenteComponent} from "./detais-facture-de-vente/detais-facture-de-vente.component";
import {AddFactureDeVenteComponent} from "./add-facture-de-vente/add-facture-de-vente.component";

const routes: Routes = [
  { path: '', redirectTo: 'listFactureAchat', pathMatch: 'full' },
  {
    path: 'listFactureAchat',
    component: ListfacturAchatComponent,
  },{
    path: 'ajouterFactureDeVente',
    component: AddFactureDeVenteComponent,
  },{
    path: 'detaisFactureAchat/:id',
    component: DetaisfacturAchatComponent,
  },{
    path: 'detaisFactureVente/:id',
    component: DetaisFactureDeVenteComponent,
  },{
    path: 'listFactureVente',
    component: ListFactureDeVenteComponent,
  },

];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FactureRoutingModule { }
