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
        // Platform settings: accessible by SUPER_ADMIN and ADMIN
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
        // Business dashboard: SUPER_ADMIN is excluded (no business data)
        path: 'dashboard',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./main/dashboard/dashboard.module').then(
            (m) => m.DashboardModule
          ),
      },
      {
        // Enterprise management: SUPER_ADMIN sees all enterprises; ADMIN sees their own
        path: 'entreprise',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./entreprise/entreprise.module').then(
            (m) => m.EntrepriseModule
          ),
      },
      {
        // Invoices/factures: business data – no SUPER_ADMIN
        path: 'facture',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./facture/facture.module').then(
            (m) => m.FactureModule
          ),
      },
      {
        // Module/permission configuration: ADMIN manages their own; SUPER_ADMIN excluded
        path: 'module',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN'] },
        loadChildren: () =>
          import('./module/module.module').then(
            (m) => m.ModuleModule
          ),
      },
      {
        // Projects: business data – no SUPER_ADMIN
        path: 'projet',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./projet/projet.module').then(
            (m) => m.ProjetModule
          ),
      },
      {
        // Clients: business data – no SUPER_ADMIN
        path: 'client',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./client/client.module').then(
            (m) => m.ClientModule
          ),
      },
      {
        // Fournisseurs: business data – no SUPER_ADMIN
        path: 'fournisseur',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
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
        // User management: platform-level – both SUPER_ADMIN and ADMIN
        path: 'user-management',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
        loadChildren: () =>
          import('./user-management/user-management.module').then(
            (m) => m.UserManagementModule
          ),
      },
      {
        // Sales: business data – no SUPER_ADMIN
        path: 'sales',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN'] },
        loadChildren: () =>
          import('./sales/sales.module').then((m) => m.SalesModule),
      },
      {
        // Stock: business data – no SUPER_ADMIN
        path: 'stock',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./stock/stock.module').then((m) => m.StockModule),
      },
      {
        // Purchases: business data – no SUPER_ADMIN
        path: 'purchase',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN'] },
        loadChildren: () =>
          import('./purchase/purchase.module').then((m) => m.PurchaseModule),
      },
      {
        // Expenses: business data – no SUPER_ADMIN
        path: 'expense',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN'] },
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
      },
      {
        path: 'documentation',
        loadChildren: () =>
          import('./documentation/documentation.module').then((m) => m.DocumentationModule),
      },
      {
        // Budget: business data – no SUPER_ADMIN
        path: 'budget',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN'] },
        loadChildren: () =>
          import('./budget/budget.module').then((m) => m.BudgetModule),
      },
      {
        // Articles achat: business data – no SUPER_ADMIN
        path: 'article-achat',
        canActivate: [RoleGuard],
        canActivateChild: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./article-achat/article-achat.module').then((m) => m.ArticleAchatModule),
      },
      {
        // Articles vente: business data – no SUPER_ADMIN
        path: 'article-vente',
        canActivate: [RoleGuard],
        canActivateChild: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./article-vente/article-vente.module').then((m) => m.ArticleVenteModule),
      },
      {
        // Purchase orders: business data – no SUPER_ADMIN
        path: 'commande-achat',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./commande-achat/commande-achat.module').then((m) => m.CommandeAchatModule),
      },
      {
        // Sales orders: business data – no SUPER_ADMIN
        path: 'commande-vente',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./commande-vente/commande-vente.module').then((m) => m.CommandeVenteModule),
      },
      {
        // AI analytics: enterprise business data – no SUPER_ADMIN
        path: 'ai-intelligence',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN'] },
        loadChildren: () =>
          import('./ai-intelligence/ai-intelligence.module').then((m) => m.AiIntelligenceModule),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CoreComponentRoutingModule {}
