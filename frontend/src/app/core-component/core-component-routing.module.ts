import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CoreComponentComponent } from './core-component.component';

const routes: Routes = [
  {
    path: '',
    component: CoreComponentComponent,
    children: [
      {
        path: 'charts',
        loadChildren: () =>
          import('./charts/charts.module').then((m) => m.ChartsModule),
      },
      {
        path: 'icons',
        loadChildren: () =>
          import('./icons/icons.module').then((m) => m.IconsModule),
      },
      {
        path: 'forms',
        loadChildren: () =>
          import('./forms/forms.module').then((m) => m.FormsModule),
      },
      {
        path: 'table',
        loadChildren: () =>
          import('./table/table.module').then((m) => m.TableModule),
      },
      {
        path: 'application',
        loadChildren: () =>
          import('././main/application/application.module').then(
            (m) => m.ApplicationModule
          ),
      },
      {
        path: 'base-ui',
        loadChildren: () =>
          import('./base-ui/base-ui.module').then((m) => m.BaseUiModule),
      },
      {
        path: 'advanced-ui',
        loadChildren: () =>
          import('./advanced-ui/advanced-ui.module').then(
            (m) => m.AdvancedUiModule
          ),
      },
      {
        path: 'hrm',
        loadChildren: () => import('./hrm/hrm.module').then((m) => m.HrmModule),
      },
      {
        path: 'promo',
        loadChildren: () =>
          import('./promo/promo.module').then((m) => m.PromoModule),
      },
      {
        path: 'inventory',
        loadChildren: () =>
          import('./inventory/inventory.module').then((m) => m.InventoryModule),
      },
      {
        path: 'settings',
        loadChildren: () =>
          import('./settings/settings.module').then((m) => m.SettingsModule),
      },
      {
        path: 'pages',
        loadChildren: () =>
          import('./pages/pages.module').then((m) => m.PagesModule),
      },
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./main/dashboard/dashboard.module').then(
            (m) => m.DashboardModule
          ),
      },
      {
        path: 'entreprise',
        loadChildren: () =>
          import('./entreprise/entreprise.module').then(
            (m) => m.EntrepriseModule
          ),
      }, {
        path: 'departement',
        loadChildren: () =>
          import('./departement/departement.module').then(
            (m) => m.DepartementModule
          ),
      },{
        path: 'facture',
        loadChildren: () =>
          import('./facture/facture-routing.module').then(
            (m) => m.FactureRoutingModule
          ),
      },
      {
        path: 'module',
        loadChildren: () =>
          import('./module/module-routing.module').then(
            (m) => m.ModuleRoutingModule
          ),
      },{
        path: 'projet',
        loadChildren: () =>
          import('./projet/projet-routing.module').then(
            (m) => m.ProjetRoutingModule
          ),
      },{
        path: 'client',
        loadChildren: () =>
          import('./client/client-routing.module').then(
            (m) => m.ClientRoutingModule
          ),
      },
      {
        path: 'equipe',
        loadChildren: () =>
          import('./equipe/equipe-routing.module').then(
            (m) => m.EquipeRoutingModule
          ),
      },

      {
        path: 'user-management',
        loadChildren: () =>
          import('./user-management/user-management.module').then(
            (m) => m.UserManagementModule
          ),
      },
      {
        path: 'sales',
        loadChildren: () =>
          import('./sales/sales.module').then((m) => m.SalesModule),
      },
      {
        path: 'stock',
        loadChildren: () =>
          import('./stock/stock.module').then((m) => m.StockModule),
      },
      {
        path: 'purchase',
        loadChildren: () =>
          import('./purchase/purchase.module').then((m) => m.PurchaseModule),
      },
      {
        path: 'expense',
        loadChildren: () =>
          import('./expense/expense.module').then((m) => m.ExpenseModule),
      },
      {
        path: 'people',
        loadChildren: () =>
          import('./people/people.module').then((m) => m.PeopleModule),
      },
      {
        path: 'reports',
        loadChildren: () =>
          import('./reports/reports.module').then((m) => m.ReportsModule),
      },{
        path: 'documentation',
        loadChildren: () =>
          import('./documentation/documentation-routing.module').then((m) => m.DocumentationRoutingModule),
      },{
        path: 'budget',
        loadChildren: () =>
          import('./budget/budget-routing.module').then((m) => m.BudgetRoutingModule),
      },


    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CoreComponentRoutingModule {}
