import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListCommandeAchatComponent } from './list-commande-achat/list-commande-achat.component';
import { AddCommandeAchatComponent } from './add-commande-achat/add-commande-achat.component';

const routes: Routes = [
  { path: '', redirectTo: 'list', pathMatch: 'full' },
  { path: 'list', component: ListCommandeAchatComponent },
  { path: 'add', component: AddCommandeAchatComponent },
  { path: 'edit/:id', component: AddCommandeAchatComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommandeAchatRoutingModule {}
