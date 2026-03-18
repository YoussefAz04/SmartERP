import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportsComponent } from './reports.component';
import { CustomerReportComponent } from './customer-report/customer-report.component';
import { ExpenseReportComponent } from './expense-report/expense-report.component';
import { IncomeReportComponent } from './income-report/income-report.component';
import { InvoiceReportComponent } from './invoice-report/invoice-report.component';
import { ProfitAndLossComponent } from './profit-and-loss/profit-and-loss.component';
import { PurchaseReportComponent } from './purchase-report/purchase-report.component';
import { SalesReportComponent } from './sales-report/sales-report.component';
import { SupplierReportComponent } from './supplier-report/supplier-report.component';
import { TaxReportComponent } from './tax-report/tax-report.component';
import { InventoryReportComponent } from './inventory-report/inventory-report.component';

const routes: Routes = [{ path: '', component: ReportsComponent,
children: [
  {
    path: 'customer-report',
    component: CustomerReportComponent
  },
  {
    path: 'expense-report',
    component: ExpenseReportComponent
  },
  {
    path: 'income-report',
    component: IncomeReportComponent
  },
  {
    path: 'inventory-report',
    component: InventoryReportComponent
  },
  {
    path: 'invoice-report',
    component: InvoiceReportComponent
  },
  {
    path: 'profit-and-loss',
    component: ProfitAndLossComponent
  },
  {
    path: 'purchase-report',
    component: PurchaseReportComponent
  },
  {
    path: 'sales-report',
    component: SalesReportComponent
  },
  {
    path: 'supplier-report',
    component: SupplierReportComponent
  },
  {
    path: 'tax-report',
    component: TaxReportComponent
  }
]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportsRoutingModule { }
