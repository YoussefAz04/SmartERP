import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StockRoutingModule } from './stock-routing.module';
import { StockComponent } from './stock.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ManageStocksComponent } from './manage-stocks/manage-stocks.component';
import { StockTransferComponent } from './stock-transfer/stock-transfer.component';
import { StockAdjustmentComponent } from './stock-adjustment/stock-adjustment.component';
import { ListStockDashboardComponent } from './list-stock-dashboard/list-stock-dashboard.component';
import { ListStockComponent } from './list-stock/list-stock.component';
import { ViewStockComponent } from './view-stock/view-stock.component';
import { AdjustStockComponent } from './adjust-stock/adjust-stock.component';
import { ListStockMovementsComponent } from './list-stock-movements/list-stock-movements.component';


@NgModule({
  declarations: [
    StockComponent,
    ManageStocksComponent,
    StockTransferComponent,
    StockAdjustmentComponent
  ],
  imports: [
    CommonModule,
    StockRoutingModule,
    SharedModule,
    ListStockDashboardComponent,
    ListStockComponent,
    ViewStockComponent,
    AdjustStockComponent,
    ListStockMovementsComponent
  ]
})
export class StockModule { }
