import {Component, OnInit} from '@angular/core';
import {DocumentService} from "../../../core/service/document/document.service";
import {MatTooltip} from "@angular/material/tooltip";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {Document} from "../../../model/Document.model";
import {Router} from "@angular/router";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-list-document',
  standalone: true,
  imports: [
    MatTooltip,
    NgIf,
    DatePipe,
    NgForOf
  ],
  templateUrl: './list-document.component.html',
  styleUrl: './list-document.component.scss'
})
export class ListDocumentComponent implements OnInit{

  documentList : Document[] = [] ;
  permissionDocument!: PermissionDto;

  constructor(private documentService : DocumentService,private router : Router) {
  }
  ngOnInit() {
   this.documentService.getAllDocuments().subscribe(data =>{
     this.documentList = data ;
   })

    this.getUserAuthData() ;
   }


  addDocument() {
    this.router.navigate(['/documentation/addDocument'])
  }

  viewDocument(id:any) {
    this.router.navigate(['/documentation/detaisDocument',id])

  }

  deleteDocument(id:any) {
    this.documentService.deleteDocument(id).subscribe({
      next: () => {
        console.log('Document supprimé avec succès');
        this.documentService.getAllDocuments().subscribe(data =>{
          this.documentList = data ;
        });
      },
      error: err => {
        console.error('Erreur lors de la suppression', err);
      }
    });
  }
  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Document"){
          this.permissionDocument = per ;
          console.log("permission Taches" ,this.permissionDocument);
        }
      })
      console.log('Taches:', currentUser?.entreprise.nom);
    }
  }


}
