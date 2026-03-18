import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CategorieDocument} from "../../../model/CategorieDocument.model";
import {ZoneSelectionneeDeDocumentModel} from "../../../model/ZoneSelectionneeDeDocument.model";
import {Document, FileChemin} from "../../../model/Document.model";

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = 'http://localhost:8020/document';

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

  ajouterZone(documentId: number, zone: ZoneSelectionneeDeDocumentModel): Observable<ZoneSelectionneeDeDocumentModel> {
    return this.http.post<ZoneSelectionneeDeDocumentModel>(`${this.apiUrl}/ajouterZone/${documentId}`, zone);
  }

  uploadFile(file: File): Observable<FileChemin> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<FileChemin>(`${this.apiUrl}/upload-file`, formData);
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
