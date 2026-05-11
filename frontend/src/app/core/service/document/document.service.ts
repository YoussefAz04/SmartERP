import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CategorieDocument} from "../../../model/CategorieDocument.model";
import {Document} from "../../../model/Document.model";

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = `${environment.apiUrl}/document`;

  constructor(private http: HttpClient) { }

  ajouterCategorieDocument(categorie: CategorieDocument): Observable<CategorieDocument> {
    return this.http.post<CategorieDocument>(`${this.apiUrl}/ajouterCategorieDocument`, categorie);
  }

  getDocumentById(id: number): Observable<Document> {
    return this.http.get<Document>(`${this.apiUrl}/getDocumentById/${id}`);
  }

  supprimerDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/supprimerDocument/${id}`);
  }

  ajouterDocument(document: Document, categorieId: number): Observable<Document> {
    const formData = new FormData();
    formData.append('document', new Blob([JSON.stringify(document)], { type: 'application/json' }));
    // la variable document est envoyée en multipart/form-data avec le JSON comme Blob
    return this.http.post<Document>(`${this.apiUrl}/ajouterDocument/${categorieId}`, formData);
  }

  affecterChemin(idDocument: number, idFile: number): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/${idDocument}/affecter-chemin/${idFile}`, null);
  }
  getAllDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.apiUrl}/getAllDocument`);
  }
  getAllCategories(): Observable<CategorieDocument[]> {
    return this.http.get<CategorieDocument[]>(`${this.apiUrl}/getAllCategorieDocument`);
  }
  updateDocument(id: number, document: Document): Observable<any> {
    return this.http.put(`${this.apiUrl}/updateDocument/${id}`, document);
  }
  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/deleteDocument/${id}`);
  }

}
