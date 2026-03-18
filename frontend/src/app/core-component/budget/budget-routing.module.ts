import {NgModule} from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import {BudgetComponent} from "./budget.component";
import {AddBudgetComponent} from "./add-budget/add-budget.component";
import {DataisBudgetComponent} from "./datais-budget/datais-budget.component";
import {DepenseComponent} from "./depense/depense.component";
import {AddDepenseComponent} from "./add-depense/add-depense.component";
import {DetaisDepenseComponent} from "./detais-depense/detais-depense.component";
import {BudgetPerformanceComponent} from "./budget-performance/budget-performance.component";
import {TauxUtilisationComponent} from "./taux-utilisation/taux-utilisation.component";

const routes: Routes = [
  { path: '', redirectTo: 'listBudget', pathMatch: 'full' },
  {
    path: 'listBudget',
    component: BudgetComponent,
  }, {
    path: 'addBudget',
    component: AddBudgetComponent,
  },{
    path: 'detaisBudget/:id',
    component: DataisBudgetComponent,
  },{
    path: 'listDepense',
    component: DepenseComponent,
  },{
    path: 'addDepense',
    component: AddDepenseComponent,
  },{
    path: 'detaisDepense/:id',
    component: DetaisDepenseComponent,
  },{
    path: 'budgetPerfermance',
    component: BudgetPerformanceComponent,
  },{
    path: 'tauxUtilisation/:id',
    component: TauxUtilisationComponent,
  },
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BudgetRoutingModule {

}
