import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Fournisseur } from '../../../model/fournisseur.model';
import { CategorieFournisseur } from '../../../model/categorieFournisseur.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FournisseurService {
  private baseUrl = 'http://localhost:8020/fournisseurs';

  constructor(private http: HttpClient) {}

  // Get token from localStorage
  private getHeaders(): HttpHeaders {
    const token = this.getToken();
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return headers;
  }

  private getToken(): string | null {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const user = JSON.parse(atob(item));
        return user.token || null;
      } catch (error) {
        console.error('Error parsing token:', error);
        return null;
      }
    }
    return null;
  }

  // ✅ ADD FOURNISSEUR
  addFournisseur(fournisseur: Fournisseur, idCategorie: number): Observable<Fournisseur> {
    return this.http.post<Fournisseur>(
      `${this.baseUrl}/addFournisseur/${idCategorie}`,
      fournisseur,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET ALL FOURNISSEURS
  getAllFournisseurs(): Observable<Fournisseur[]> {
    return this.http.get<Fournisseur[]>(
      `${this.baseUrl}/getAllFournisseurs`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET FOURNISSEUR BY ID
  getFournisseurById(id: number): Observable<Fournisseur> {
    return this.http.get<Fournisseur>(
      `${this.baseUrl}/getFournisseurById/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ DELETE FOURNISSEUR
  deleteFournisseur(id: number): Observable<any> {
    return this.http.delete<any>(
      `${this.baseUrl}/deleteFournisseur/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ UPDATE FOURNISSEUR
  updateFournisseur(id: number, fournisseur: Fournisseur): Observable<Fournisseur> {
    return this.http.put<Fournisseur>(
      `${this.baseUrl}/updateFournisseur/${id}`,
      fournisseur,
      { headers: this.getHeaders() }
    );
  }

  // ✅ ADD CATEGORIE FOURNISSEUR
  addCategorieFournisseur(categorie: CategorieFournisseur): Observable<CategorieFournisseur> {
    return this.http.post<CategorieFournisseur>(
      `${this.baseUrl}/addCategorieFournisseur`,
      categorie,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET ALL CATEGORIES FOURNISSEUR
  getAllCategorieFournisseurs(): Observable<CategorieFournisseur[]> {
    return this.http.get<CategorieFournisseur[]>(
      `${this.baseUrl}/getAllCategorieFournisseurs`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ DELETE CATEGORIE FOURNISSEUR
  deleteCategorieFournisseur(id: number): Observable<any> {
    return this.http.delete<any>(
      `${this.baseUrl}/deleteCategorieFournisseur/${id}`,
      { headers: this.getHeaders() }
    );
  }
}
