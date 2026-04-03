import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EntrepriseService {
  private baseUrl = `${environment.apiUrl}/entreprises`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = this.getToken();
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    } else {
      console.warn('No token found in localStorage');
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

  // ✅ GET ALL ENTERPRISES
  getAllEntreprises(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/getAllEntreprises`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET ENTERPRISE BY ID
  getEntrepriseById(id: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/getEntrepriseById/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ ADD ENTERPRISE
  ajouterEntreprise(data: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/addEntreprise`,
      data,
      { headers: this.getHeaders() }
    );
  }

  // ✅ UPDATE ENTERPRISE
  updateEntreprise(id: number, data: any): Observable<any> {
    return this.http.put<any>(
      `${this.baseUrl}/updateEntreprise/${id}`,
      data,
      { headers: this.getHeaders() }
    );
  }

  // ✅ DELETE ENTERPRISE
  deleteEntreprise(id: number): Observable<any> {
    return this.http.delete<any>(
      `${this.baseUrl}/deleteEntreprise/${id}`,
      { headers: this.getHeaders() }
    );
  }
}