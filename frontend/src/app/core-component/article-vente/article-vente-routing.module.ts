import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ArticleVenteComponent } from './article-vente.component';
import { AddArticleVenteComponent } from './add-article-vente/add-article-vente.component';
import { EditArticleVenteComponent } from './edit-article-vente/edit-article-vente.component';

const routes: Routes = [
  { path: '', redirectTo: 'listArticleVente', pathMatch: 'full' },
  { path: 'listArticleVente', component: ArticleVenteComponent },
  { path: 'add', component: AddArticleVenteComponent },
  { path: 'edit/:id', component: EditArticleVenteComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ArticleVenteRoutingModule {}
