import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ModuleComponent} from "./module.component";
import {AddModuleComponent} from "./add-module/add-module.component";
import {AddModuleTittleComponent} from "./add-module-tittle/add-module-tittle.component";
import {ListModuleAffecterComponent} from "./list-module-affecter/list-module-affecter.component";
import {ListModuleComponent} from "./list-module/list-module.component";
import {ListSousModuleComponent} from "./list-sous-module/list-sous-module.component";
import {AddSousModuleComponent} from "./add-sous-module/add-sous-module.component";

const routes: Routes = [
  { path: '', redirectTo: 'listModule', pathMatch: 'full' },
  {
    path: 'listModuleTitre',
    component: ModuleComponent
  },  {
    path: 'addModule',
    component: AddModuleComponent
  }, {
    path: 'addSousModule',
    component: AddSousModuleComponent
  },{
    path: 'addModuleTittle',
    component: AddModuleTittleComponent
  },
{
    path: 'listModuleForAffecter/:id',
    component: ListModuleAffecterComponent
  },{
    path: 'listModule',
    component: ListModuleComponent
  },{
    path: 'listSousModule',
    component: ListSousModuleComponent
  },



];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ModuleRoutingModule { }
