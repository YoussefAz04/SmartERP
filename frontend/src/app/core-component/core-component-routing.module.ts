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
        // User profile: any authenticated user can view their own profile
        path: 'profile',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN', 'EMPLOYEE'] },
        loadComponent: () =>
          import('./profile/profile.component').then((m) => m.ProfileComponent),
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
        // Stock: business data – no SUPER_ADMIN
        path: 'stock',
        canActivate: [RoleGuard],
        data: { requiredRoles: ['ADMIN', 'EMPLOYEE'] },
        loadChildren: () =>
          import('./stock/stock.module').then((m) => m.StockModule),
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
