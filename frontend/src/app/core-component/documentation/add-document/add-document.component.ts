import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {CategorieDocument} from "../../../model/CategorieDocument.model";
import {DocumentService} from "../../../core/service/document/document.service";
import {Validators} from "ngx-editor";
import {RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-add-document',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    NgForOf
  ],
  templateUrl: './add-document.component.html',
  styleUrl: './add-document.component.scss'
})
export class AddDocumentComponent  implements OnInit{
  documentForm: FormGroup;
  selectedFile: File | null = null;
  categories: CategorieDocument[] = [];

  constructor(
    private fb: FormBuilder,
    private documentService: DocumentService
  ) {
    this.documentForm = this.fb.group({
      nom: ['', Validators.required],
      type: ['', Validators.required],
      categorieId: [null, Validators.required]
    });
  }
ngOnInit() {
this.documentService.getAllCategories().subscribe(data =>{
  this.categories = data ;
  console.log(data);
})
}


idFile !: number ;
  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.documentService.uploadFile(file).subscribe(data =>{
        this.idFile = data.id ;
console.log(data);
      });
    }
  }


  onSubmit() {
    if (this.documentForm.invalid) {
      return;
    }


    this.documentService.ajouterDocument(this.documentForm.value
      ,this.documentForm.value.categorieId)
      .subscribe(data =>{
          console.log('Document ajouté avec succès');
          console.log("id de document " ,data.id,"id file ",this.idFile)
        if (data.id)
          this.documentService.affecterChemin(data.id,this.idFile).subscribe(()=>{
            console.log("file affecter");
          })
          this.documentForm.reset();

      });
  }

  protected readonly routes = routes;
}
