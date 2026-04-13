import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CoreComponentComponent } from './core-component.component';
import { RoleGuard } from '../core/guard/role/role.guard';

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
          import('./main/application/application.module').then(
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
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
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
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./facture/facture.module').then(
            (m) => m.FactureModule
          ),
      },
      {
        path: 'module',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./module/module.module').then(
            (m) => m.ModuleModule
          ),
      },{
        path: 'projet',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./projet/projet.module').then(
            (m) => m.ProjetModule
          ),
      },{
        path: 'client',
        loadChildren: () =>
          import('./client/client.module').then(
            (m) => m.ClientModule
          ),
      },
      {
        path: 'fournisseur',
        loadChildren: () =>
          import('./fournisseur/fournisseur.module').then(
            (m) => m.FournisseurModule
          ),
      },
      {
        path: 'equipe',
        loadChildren: () =>
          import('./equipe/equipe.module').then(
            (m) => m.EquipeModule
          ),
      },

      {
        path: 'user-management',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./user-management/user-management.module').then(
            (m) => m.UserManagementModule
          ),
      },
      {
        path: 'sales',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./sales/sales.module').then((m) => m.SalesModule),
      },
      {
        path: 'stock',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./stock/stock.module').then((m) => m.StockModule),
      },
      {
        path: 'purchase',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./purchase/purchase.module').then((m) => m.PurchaseModule),
      },
      {
        path: 'expense',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
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
          import('./documentation/documentation.module').then((m) => m.DocumentationModule),
      },{
        path: 'budget',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./budget/budget.module').then((m) => m.BudgetModule),
      },{
        path: 'article-achat',
        canActivate: [RoleGuard],
        canActivateChild: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./article-achat/article-achat.module').then((m) => m.ArticleAchatModule),
      },{
        path: 'article-vente',
        canActivate: [RoleGuard],
        canActivateChild: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./article-vente/article-vente.module').then((m) => m.ArticleVenteModule),
      },{
        path: 'commande-achat',
        loadChildren: () =>
          import('./commande-achat/commande-achat.module').then((m) => m.CommandeAchatModule),
      },{
        path: 'commande-vente',
        loadChildren: () =>
          import('./commande-vente/commande-vente.module').then((m) => m.CommandeVenteModule),
      },


    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CoreComponentRoutingModule {}
