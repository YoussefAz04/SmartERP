// src/app/services/budget.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Budget} from "../../../model/budget.model";

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private apiUrl = 'http://localhost:8020/budgets';

  constructor(private http: HttpClient) {}

   getAllBudgets(): Observable<Budget[]> {
    return this.http.get<Budget[]>(`${this.apiUrl}/getAllBudgets`);
  }

  // Récupérer un budget par ID
  getBudgetById(id: number): Observable<Budget> {
    return this.http.get<Budget>(`${this.apiUrl}/getBudgetById/${id}`);
  }
  getBudgetsByProjet(id: number): Observable<Budget> {
    return this.http.get<Budget>(`${this.apiUrl}/getBudgetsByProjet/${id}`);
  }

  // Ajouter un budget
  addBudget(budget: Budget): Observable<Budget> {
    return this.http.post<Budget>(`${this.apiUrl}/addBudget`, budget);
  }

  // Mettre à jour un budget
  updateBudget(id: number, budget: Budget): Observable<Budget> {
    return this.http.put<Budget>(`${this.apiUrl}/updateBudget/${id}`, budget);
  }

  // Supprimer un budget
  deleteBudget(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/deleteBudget/${id}`, { responseType: 'text' });
  }
}
