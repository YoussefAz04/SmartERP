import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListSavComponent } from './list-sav/list-sav.component';
import { AddSavComponent } from './add-sav/add-sav.component';
import { SavDetailComponent } from './sav-detail/sav-detail.component';

const routes: Routes = [
  { path: '', redirectTo: 'list', pathMatch: 'full' },
  { path: 'list', component: ListSavComponent },
  { path: 'add', component: AddSavComponent },
  { path: 'edit/:id', component: AddSavComponent },
  { path: 'detail/:id', component: SavDetailComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SavRoutingModule {}
