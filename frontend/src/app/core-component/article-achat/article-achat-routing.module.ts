import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ArticleAchatComponent } from './article-achat.component';
import { AddArticleAchatComponent } from './add-article-achat/add-article-achat.component';
import { EditArticleAchatComponent } from './edit-article-achat/edit-article-achat.component';
import { RoleGuard } from '../../core/guard/role/role.guard';

const routes: Routes = [
  { path: '', redirectTo: 'listArticleAchat', pathMatch: 'full' },
  { path: 'listArticleAchat', component: ArticleAchatComponent },
  { path: 'add', component: AddArticleAchatComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
  { path: 'edit/:id', component: EditArticleAchatComponent, canActivate: [RoleGuard], data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] } },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ArticleAchatRoutingModule {}
