import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ProjetComponent} from "./projet.component";
import {AddProjetComponent} from "./add-projet/add-projet.component";
import {AddCategorieProjetComponent} from "./add-categorie-projet/add-categorie-projet.component";
import {ListCategorieProjetComponent} from "./list-categorie-projet/list-categorie-projet.component";
import {ListTachesProjetComponent} from "./list-taches-projet/list-taches-projet.component";
import {AddTacheComponent} from "./add-tache/add-tache.component";
import {ListcategorieTacheComponent} from "./listcategorie-tache/listcategorie-tache.component";
import {AddcategorieTacheComponent} from "./addcategorie-tache/addcategorie-tache.component";
import {DetaisProjetComponent} from "./detais-projet/detais-projet.component";
import {GanttComponent} from "./gantt/gantt.component";
import {ListSousTacheComponent} from "./list-sous-tache/list-sous-tache.component";
import {AddSousTacheComponent} from "./add-sous-tache/add-sous-tache.component";
import {DetaisTacheComponent} from "./detais-tache/detais-tache.component";
import {KanbanTacheComponent} from "./kanban-tache/kanban-tache.component";
import {StatistiqueProjetComponent} from "./statistique-projet/statistique-projet.component";
import {StatistiqueTacheComponent} from "./statistique-tache/statistique-tache.component";
import {UpadateProjetComponent} from "./upadate-projet/upadate-projet.component";
import {UpadateTacheComponent} from "./upadate-tache/upadate-tache.component";
import {DetaisSousTacheComponent} from "./detais-sous-tache/detais-sous-tache.component";

const routes: Routes = [
  { path: '', redirectTo: 'listProjet', pathMatch: 'full' },
  {
    path: 'listProjet',
    component: ProjetComponent,
  },
  {
    path: 'addProjet',
    component: AddProjetComponent,
  },{
    path: 'addCategorieProjet',
    component: AddCategorieProjetComponent,
  },{
    path: 'listCategorieProjet',
    component: ListCategorieProjetComponent,
  },{
    path: 'listTachesProjet',
    component: ListTachesProjetComponent,
  },{
    path: 'addTache/:id',
    component: AddTacheComponent,
  },{
    path: 'listCategorieTache',
    component: ListcategorieTacheComponent,
  },{
    path: 'addCategorieTache',
    component: AddcategorieTacheComponent,
  },{
    path: 'detaisProjet/:id',
    component: DetaisProjetComponent,
  },{
    path: 'ganttProjet/:id',
    component: GanttComponent,
  },{
    path: 'listSousTache',
    component: ListSousTacheComponent,
  },{
    path: 'addSousTache',
    component: AddSousTacheComponent,
  },{
    path: 'detaisTache/:id',
    component: DetaisTacheComponent,
  },{
    path: 'KanbanTache',
    component: KanbanTacheComponent,
  },{
    path: 'statistiqueProjet',
    component: StatistiqueProjetComponent,
  },{
    path: 'statistiqueTache',
    component: StatistiqueTacheComponent,
  },{
    path: 'updateProjet/:id',
    component: UpadateProjetComponent,
  },{
    path: 'updateTache/:id',
    component: UpadateTacheComponent,
  },{
    path: 'detaisSousTache/:id',
    component: DetaisSousTacheComponent,
  },
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProjetRoutingModule { }
