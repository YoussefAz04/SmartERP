import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EntrepriseService {
  private baseUrl = `${environment.apiUrl}/entreprise`;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = this.getToken();
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    } else {
      console.warn('⚠️ No token found in localStorage');
    }
    return headers;
  }

  private getToken(): string | null {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        return user.token || null;
      } catch (error) {
        console.error('Error parsing token:', error);
        return null;
      }
    }
    return null;
  }

  // ✅ GET ALL ENTERPRISES - Matches backend: GET /entreprise/getAll
  getAllEntreprises(): Observable<any[]> {
    console.log('🔍 Loading enterprises from:', `${this.baseUrl}/getAll`);
    return this.http.get<any[]>(
      `${this.baseUrl}/getAll`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET ENTERPRISE BY ID - Matches backend: GET /entreprise/getById/{id}
  getEntrepriseById(id: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/getById/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ ADD ENTERPRISE - Matches backend: POST /entreprise/create
  ajouterEntreprise(data: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/create`,
      data,
      { headers: this.getHeaders() }
    );
  }

  // ✅ UPDATE ENTERPRISE - Matches backend: PUT /entreprise/update/{id}
  updateEntreprise(id: number, data: any): Observable<any> {
    return this.http.put<any>(
      `${this.baseUrl}/update/${id}`,
      data,
      { headers: this.getHeaders() }
    );
  }

  // ✅ DISABLE ENTERPRISE - Matches backend: PUT /entreprise/disable/{id}
  disableEntreprise(id: number): Observable<any> {
    return this.http.put<any>(
      `${this.baseUrl}/disable/${id}`,
      {},
      { headers: this.getHeaders(), responseType: 'text' as 'json' }
    );
  }

  // ✅ GET CURRENT USER'S ENTERPRISE - Reads enterpriseId from localStorage
  getEntrepriseByCurrentUser(): Observable<any> {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        const entrepriseId = user?.entreprise?.id ?? user?.entrepriseId;
        if (entrepriseId) {
          return this.getEntrepriseById(entrepriseId);
        }
      } catch (error) {
        console.error('Error parsing user data for entreprise lookup:', error);
      }
    }
    return new Observable(observer =>
      observer.error('No enterprise ID found for current user')
    );
  }
}