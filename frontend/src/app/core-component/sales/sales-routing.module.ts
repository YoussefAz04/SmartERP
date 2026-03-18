import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SalesComponent } from './sales.component';
import { SalesListComponent } from './sales-list/sales-list.component';
import { SalesReturnsComponent } from './sales-returns/sales-returns.component';
import { QuotationListComponent } from './quotation-list/quotation-list.component';
import { PosComponent } from './pos/pos.component';

const routes: Routes = [{ path: '', component: SalesComponent,
children: [
  {
    path: 'sales-list',
    component: SalesListComponent
  },
  {
    path: 'sales-return',
    component: SalesReturnsComponent
  },
  {
    path: 'quotation-list',
    component: QuotationListComponent
  },
  {
    path: 'pos',
    component: PosComponent
  },

]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SalesRoutingModule { }
