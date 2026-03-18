import { Injectable } from '@angular/core';
import {CategorieTache} from "../../../model/categorieTache.model";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class CategorieTacheService {

  private baseUrl = 'http://localhost:8020/categorie-taches';

  constructor(private http: HttpClient) { }

  // Créer une nouvelle catégorie
  addCategorie(categorie: CategorieTache): Observable<CategorieTache> {
    return this.http.post<CategorieTache>(`${this.baseUrl}/add`, categorie);
  }

  // Récupérer toutes les catégories
  getAllCategories(): Observable<CategorieTache[]> {
    return this.http.get<CategorieTache[]>(`${this.baseUrl}/getAll`);
  }

  // Récupérer une catégorie par ID
  getCategorieById(id: number): Observable<CategorieTache> {
    return this.http.get<CategorieTache>(`${this.baseUrl}/getById/${id}`);
  }

  // Mettre à jour une catégorie
  updateCategorie(id: number, categorie: CategorieTache): Observable<CategorieTache> {
    return this.http.put<CategorieTache>(`${this.baseUrl}/update/${id}`, categorie);
  }

  // Supprimer une catégorie
  deleteCategorie(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete/${id}`);
  }
}
