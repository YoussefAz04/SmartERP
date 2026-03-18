import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Evenement} from "../../../model/evenement.model";
import {Observable} from "rxjs";
import {CategorieEvenement} from "../../../model/categorieEvenement.model";

@Injectable({
  providedIn: 'root'
})
export class EvenementService {

  private apiUrl = 'http://localhost:8020/evenement';

  constructor(private http: HttpClient) {}

  getAllEvenement(): Observable<Evenement[]> {
    return this.http.get<Evenement[]>(`${this.apiUrl}/getAllEvenement`);
  }

  saveEvenement(event: Evenement, idCategorie: number): Observable<Evenement> {
    return this.http.post<Evenement>(`${this.apiUrl}/saveEvenement/${idCategorie}`, event);
  }
  deleteEvenement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/deleteEvenement/${id}`);
  }
  getAllCategorie(): Observable<CategorieEvenement[]> {
    return this.http.get<CategorieEvenement[]>(`${this.apiUrl}/getAllCategorie`);
  }
  createCategorieEvenement(evenement: CategorieEvenement): Observable<CategorieEvenement> {
    return this.http.post<CategorieEvenement>(`${this.apiUrl}/createCategorieEvenement`, evenement);
  }
}
