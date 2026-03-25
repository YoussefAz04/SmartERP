import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Entreprise } from "../../../model/entreprise.model";

@Injectable({
  providedIn: 'root'
})
export class EntrepriseService {

  private apiUrl = 'http://localhost:8020/entreprise';

  constructor(private http: HttpClient) {}

  ajouterEntreprise(entreprise: Entreprise): Observable<Entreprise> {
    console.log('📤 Service: Creating entreprise', entreprise);
    // NO HEADERS - let Angular handle it naturally
    return this.http.post<Entreprise>(`${this.apiUrl}/create`, entreprise);
  }

  getAllEntreprises(): Observable<Entreprise[]> {
    console.log('📥 Service: Fetching all');
    return this.http.get<Entreprise[]>(`${this.apiUrl}/getAll`);
  }

  getEntrepriseById(id: number): Observable<Entreprise> {
    console.log('📥 Service: Fetching by ID:', id);
    return this.http.get<Entreprise>(`${this.apiUrl}/getById/${id}`);
  }

  deleteEntreprise(id: number): Observable<any> {
    console.log('🗑️ Service: Deleting:', id);
    return this.http.delete(`${this.apiUrl}/delete/${id}`, { responseType: 'text' });
  }

  updateEntreprise(id: number, entreprise: Entreprise): Observable<Entreprise> {
    console.log('✏️ Service: Updating:', id);
    return this.http.put<Entreprise>(`${this.apiUrl}/update/${id}`, entreprise);
  }

  assignerUserToEntreprise(username: string, nom: string): Observable<any> {
    const params = new HttpParams().set('username', username).set('nom', nom);
    return this.http.post(`${this.apiUrl}/assignerUserToEntreprise`, null, { params, responseType: 'text' });
  }
}