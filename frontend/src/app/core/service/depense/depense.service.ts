import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Depense} from "../../../model/depense.model";
import {BudgetPerformanceDto} from "../../../model/budgetPerformanceDto.model";


@Injectable({
  providedIn: 'root'
})
export class DepenseService {

  private baseUrl = 'http://localhost:8020/depenses'; // URL de ton backend

  constructor(private http: HttpClient) { }

  // Récupérer toutes les dépenses
  getAllDepenses(): Observable<Depense[]> {
    return this.http.get<Depense[]>(`${this.baseUrl}/getAllDepenses`);
  }

  // Récupérer une dépense par ID
  getDepenseById(id: number): Observable<Depense> {
    return this.http.get<Depense>(`${this.baseUrl}/getDepenseById/${id}`);
  }

  // Ajouter une dépense
  addDepense(depense: Depense, idBudget: number, idCategorie: number): Observable<Depense> {
    return this.http.post<Depense>(`${this.baseUrl}/addDepense/${idBudget}/${idCategorie}`, depense);
  }


  // Mettre à jour une dépense
  updateDepense(id: number, depense: Depense): Observable<Depense> {
    return this.http.put<Depense>(`${this.baseUrl}/updateDepense/${id}`, depense);
  }

  // Supprimer une dépense
  deleteDepense(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/deleteDepense/${id}`);
  }


  //  1. Total dépense d’un projet
  getTotalDepenseByProjet(idProjet: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/total-depenses/${idProjet}`);
  }

  //  2. Dépenses par catégorie
  getDepenseByCategorie(idCategorie: number): Observable<Depense[]> {
    return this.http.get<Depense[]>(`${this.baseUrl}/depenses-par-categorie/${idCategorie}`);
  }

  //  3. Taux d’utilisation d’un projet
  getTauxUtilisation(idProjet: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/taux-utilisation/${idProjet}`);
  }

  //  4. Performance budgets
  getPerformanceBudgets(): Observable<BudgetPerformanceDto[]> {
    return this.http.get<BudgetPerformanceDto[]>(`${this.baseUrl}/performance-budgets`);
  }

}
