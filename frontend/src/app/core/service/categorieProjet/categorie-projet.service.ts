// src/app/core/service/categorie-projet/categorie-projet.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {CategorieProjet} from "../../../model/CategorieProjet.model";

@Injectable({
  providedIn: 'root'
})
export class CategorieProjetService {
  private apiUrl = 'http://localhost:8020/categorieProjet';

  constructor(private http: HttpClient) {}

  getAll(): Observable<CategorieProjet[]> {
    return this.http.get<CategorieProjet[]>(`${this.apiUrl}/getAll`);
  }

  getById(id: number): Observable<CategorieProjet> {
    return this.http.get<CategorieProjet>(`${this.apiUrl}/getById/${id}`);
  }

  create(categorie: CategorieProjet): Observable<CategorieProjet> {
    return this.http.post<CategorieProjet>(`${this.apiUrl}/add`, categorie);
  }

  update(id: number, categorie: CategorieProjet): Observable<CategorieProjet> {
    return this.http.put<CategorieProjet>(`${this.apiUrl}/update/${id}`, categorie);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
}

