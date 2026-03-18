import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {CategorieDepense} from "../../../model/depense.model";

@Injectable({
  providedIn: 'root'
})
export class CategorieDepenseService {

  private baseUrl = 'http://localhost:8020/categories-depenses';

  constructor(private http: HttpClient) { }

  // Récupérer toutes les catégories
  getAllCategories(): Observable<CategorieDepense[]> {
    return this.http.get<CategorieDepense[]>(`${this.baseUrl}/getAllCategories`);
  }

  // Récupérer une catégorie par ID
  getCategorieById(id: number): Observable<CategorieDepense> {
    return this.http.get<CategorieDepense>(`${this.baseUrl}/getCategorieById/${id}`);
  }

  // Créer une nouvelle catégorie
  createCategorie(categorie: CategorieDepense): Observable<any> {
    return this.http.post(`${this.baseUrl}/createCategorie`, categorie);
  }

  // Mettre à jour une catégorie
  updateCategorie(id: number, categorie: CategorieDepense): Observable<CategorieDepense> {
    return this.http.put<CategorieDepense>(`${this.baseUrl}/updateCategorie/${id}`, categorie);
  }

  // Supprimer une catégorie
  deleteCategorie(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/deleteCategorie/${id}`);
  }
}
