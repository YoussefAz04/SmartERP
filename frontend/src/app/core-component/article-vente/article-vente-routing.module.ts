import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ArticleVenteComponent } from './article-vente.component';
import { AddArticleVenteComponent } from './add-article-vente/add-article-vente.component';
import { EditArticleVenteComponent } from './edit-article-vente/edit-article-vente.component';
import { RoleGuard } from '../../core/guard/role/role.guard';

const routes: Routes = [
  { path: '', redirectTo: 'listArticleVente', pathMatch: 'full' },
  { path: 'listArticleVente', component: ArticleVenteComponent },
  { path: 'add', component: AddArticleVenteComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
  { path: 'edit/:id', component: EditArticleVenteComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ArticleVenteRoutingModule {}
