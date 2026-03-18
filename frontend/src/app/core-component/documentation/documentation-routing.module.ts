import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {DocumentationComponent} from "./documentation.component";
import {ListDocumentComponent} from "./list-document/list-document.component";
import {AddDocumentComponent} from "./add-document/add-document.component";
import {DetaisDocumentComponent} from "./detais-document/detais-document.component";
import {PdfZoneSelectorComponent} from "./pdf-zone-selector/pdf-zone-selector.component";

const routes: Routes = [
  { path: '', redirectTo: 'documentation', pathMatch: 'full' },
  {
    path: 'selectZone',
    component: PdfZoneSelectorComponent,
  },{
    path: 'listDocument',
    component: ListDocumentComponent,
  },{
    path: 'addDocument',
    component: AddDocumentComponent,
  },{
    path: 'detaisDocument/:id',
    component: DetaisDocumentComponent,
  }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DocumentationRoutingModule { }
