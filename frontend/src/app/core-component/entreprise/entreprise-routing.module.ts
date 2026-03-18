import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {EntrepriseComponent} from "./entreprise.component";
import {DetaisEntrepriseComponent} from "./detais-entreprise/detais-entreprise.component";
import {AddEntrepriseComponent} from "./add-entreprise/add-entreprise.component";

const routes: Routes = [
  { path: '', redirectTo: 'listEntreprise', pathMatch: 'full' },
  {
    path: 'listEntreprise',
    component: EntrepriseComponent,
  },
  {
    path: 'detaisEntreprise/:id',
    component: DetaisEntrepriseComponent,
  },  {
    path: 'addEntreprise',
    component: AddEntrepriseComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EntrepriseRoutingModule { }
