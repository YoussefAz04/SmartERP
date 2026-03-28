import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from "@angular/common/http";
import { Observable } from "rxjs";
import { Entreprise } from "../../../model/entreprise.model";

@Injectable({
  providedIn: 'root'
})
export class EntrepriseService {

  private apiUrl = 'http://localhost:8020/entreprise';

  constructor(private http: HttpClient) {}

  // Get Authorization Header
  private getHeaders(): HttpHeaders {
    const token = this.getTokenFromLocalStorage();
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    if (token) {
      // Token might be with or without "Bearer " prefix
      const authToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      headers = headers.set('Authorization', authToken);
      console.log('Authorization header set:', authToken.substring(0, 20) + '...');
    } else {
      console.warn('No token found in localStorage');
    }
    
    return headers;
  }

  // Extract token from localStorage (handles the base64 encoded format)
  private getTokenFromLocalStorage(): string | null {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        // Your token is stored as base64 encoded JSON
        const decoded = JSON.parse(atob(item));
        console.log('Decoded localStorage item:', decoded);
        return decoded.token || null;
      } catch (error) {
        console.error('Error parsing token from localStorage:', error);
        return null;
      }
    }
    console.warn('myLSkey not found in localStorage');
    return null;
  }

  ajouterEntreprise(entreprise: Entreprise): Observable<Entreprise> {
    console.log('Creating entreprise with data:', entreprise);
    return this.http.post<Entreprise>(
      `${this.apiUrl}/create`, 
      entreprise,
      { headers: this.getHeaders() }
    );
  }

  getAllEntreprises(): Observable<Entreprise[]> {
    return this.http.get<Entreprise[]>(`${this.apiUrl}/getAll`);
  }

  assignerUserToEntreprise(username: string, nom: string) {
    const params = new HttpParams()
      .set('username', username)
      .set('nom', nom);

    return this.http.post(`${this.apiUrl}/assignerUserToEntreprise`, null, { 
      params, 
      responseType: 'text',
      headers: this.getHeaders()
    });
  }

  getEntrepriseById(id: number): Observable<Entreprise> {
    return this.http.get<Entreprise>(`${this.apiUrl}/getById/${id}`);
  }

  deleteEntreprise(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete/${id}`, { 
      responseType: 'text',
      headers: this.getHeaders()
    });
  }

  updateEntreprise(id: number, entreprise: Entreprise): Observable<Entreprise> {
    return this.http.put<Entreprise>(
      `${this.apiUrl}/update/${id}`, 
      entreprise,
      { headers: this.getHeaders() }
    );
  }
}