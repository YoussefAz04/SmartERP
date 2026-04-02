import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StockComponent } from './stock.component';
import { ManageStocksComponent } from './manage-stocks/manage-stocks.component';
import { StockAdjustmentComponent } from './stock-adjustment/stock-adjustment.component';
import { StockTransferComponent } from './stock-transfer/stock-transfer.component';
import { ListStockDashboardComponent } from './list-stock-dashboard/list-stock-dashboard.component';
import { ListStockComponent } from './list-stock/list-stock.component';
import { ViewStockComponent } from './view-stock/view-stock.component';
import { AdjustStockComponent } from './adjust-stock/adjust-stock.component';
import { ListStockMovementsComponent } from './list-stock-movements/list-stock-movements.component';

const routes: Routes = [
  {
    path: '',
    component: StockComponent,
    children: [
      {
        path: '',
        redirectTo: 'listStockDashboard',
        pathMatch: 'full',
      },
      {
        path: 'listStockDashboard',
        component: ListStockDashboardComponent,
      },
      {
        path: 'list',
        component: ListStockComponent,
      },
      {
        path: 'view/:id',
        component: ViewStockComponent,
      },
      {
        path: 'adjust',
        component: AdjustStockComponent,
      },
      {
        path: 'adjust/:id',
        component: AdjustStockComponent,
      },
      {
        path: 'movements',
        component: ListStockMovementsComponent,
      },
      {
        path: 'manage-stocks',
        component: ManageStocksComponent,
      },
      {
        path: 'stock-adjustment',
        component: StockAdjustmentComponent,
      },
      {
        path: 'stock-transfer',
        component: StockTransferComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class StockRoutingModule {}
