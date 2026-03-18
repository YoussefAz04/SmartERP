import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {EquipeComponent} from "./equipe.component";
import {MembreListComponent} from "./membre-list/membre-list.component";
import {AffectationUserToMembreComponent} from "./affectation-user-to-membre/affectation-user-to-membre.component";
import {AddMembreComponent} from "./add-membre/add-membre.component";
import {AddEquipeComponent} from "./add-equipe/add-equipe.component";
import {DetailsEquipeComponent} from "./details-equipe/details-equipe.component";
import {DetailsMembreComponent} from "./details-membre/details-membre.component";
import {AffecterMembreToEquipeComponent} from "./affecter-membre-to-equipe/affecter-membre-to-equipe.component";

const routes: Routes = [
  { path: '', redirectTo: 'listEquipe', pathMatch: 'full' },
  {
    path: 'listEquipe',
    component: EquipeComponent,
  },
  {
    path: 'listMembre',
    component: MembreListComponent,
  },
  {
    path: 'affectedUserToMembre/:id',
    component: AffectationUserToMembreComponent,
  },
  {
    path: 'addMembre',
    component: AddMembreComponent,
  },{
    path: 'addEquipe',
    component: AddEquipeComponent,
  },{
    path: 'detailsEquipe/:id',
    component: DetailsEquipeComponent,
  },{
    path: 'detailsMembre/:id',
    component: DetailsMembreComponent,
  },{
    path: 'affectedMembreToEquipe/:id',
    component: AffecterMembreToEquipeComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EquipeRoutingModule {

}
