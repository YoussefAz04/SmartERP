import {Component, OnInit} from '@angular/core';
import {DocumentService} from "../../../core/service/document/document.service";
import {ActivatedRoute} from "@angular/router";
import {Document} from "../../../model/Document.model";
import {NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {SafeUrlPipe} from "./SafeUrlPipe";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-detais-document',
  standalone: true,
  imports: [
    NgIf,
    FormsModule,
    SafeUrlPipe
  ],
  templateUrl: './detais-document.component.html',
  styleUrl: './detais-document.component.scss'
})
export class DetaisDocumentComponent implements OnInit {
  idDocument !: number ;
  documentDetais !: Document ;
  constructor(private documentService : DocumentService,
              private activateRouter : ActivatedRoute,
              private sanitizer: DomSanitizer
              ) {

  }

  ngOnInit() {
    this.idDocument = this.activateRouter.snapshot.params['id'];
    this.documentService.getDocumentById(this.idDocument).subscribe(data => {
      this.documentDetais = data ;
    })

}
onSubmit(){
  this.documentService.updateDocument(this.idDocument,this.documentDetais).subscribe(()=>{
    console.log("document modifier")
  })
}

  getPdfViewerUrl(pdfUrl: string): SafeResourceUrl {
    const googleViewerUrl = `https://docs.google.com/viewer?url=${encodeURIComponent(pdfUrl)}&embedded=true`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(googleViewerUrl);
  }




}
