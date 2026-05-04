import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {Client} from "../../../model/client.model";
import {Observable} from "rxjs";
import {CategorieClient} from "../../../model/categorieClient.model";

export interface ClientUpdateRequest extends Omit<Client, 'categorieClient'> {
  categorieId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private baseUrl = `${environment.apiUrl}/clients`;

  constructor(private http: HttpClient) { }

  // Get token from localStorage
  private getHeaders(): HttpHeaders {
    const token = this.getToken();
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    } else {
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
        return null;
      }
    }
    return null;
  }

  // ✅ ADD CLIENT
  addClient(client: Client, idCategorie: number): Observable<Client> {
    return this.http.post<Client>(
      `${this.baseUrl}/addClient/${idCategorie}`, 
      client,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET ALL CLIENTS
  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(
      `${this.baseUrl}/getAllClients`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET CLIENT BY ID
  getClientById(id: number): Observable<Client> {
    return this.http.get<Client>(
      `${this.baseUrl}/getClientById/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ DELETE CLIENT
  deleteClient(id: number): Observable<any> {
    return this.http.delete<any>(
      `${this.baseUrl}/deleteClient/${id}`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ ADD CATEGORY CLIENT
  addCategorieClient(categorie: CategorieClient): Observable<CategorieClient> {
    return this.http.post<CategorieClient>(
      `${this.baseUrl}/addCategorieClient`, 
      categorie,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET ALL CATEGORIES
  getAllCategorieClients(): Observable<CategorieClient[]> {
    return this.http.get<CategorieClient[]>(
      `${this.baseUrl}/getAllCategorieClients`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ GET CATEGORY BY ID
  getCategorieClientById(id: number): Observable<CategorieClient> {
    return this.http.get<CategorieClient>(
      `${this.baseUrl}/getCategorieClientById/${id}`,
      { headers: this.getHeaders() }
    );
  }
  updateClient(id: number, client: ClientUpdateRequest): Observable<Client> {
    let params = new HttpParams();
    if (client.categorieId != null) {
      params = params.set('idCategorie', client.categorieId.toString());
    }
    return this.http.put<Client>(
      `${this.baseUrl}/updateClient/${id}`,
      client,
      { headers: this.getHeaders(), params }
    );
  }
deleteCategorie(id: number): Observable<any> {
  return this.http.delete(
    `${this.baseUrl}/deleteCategorie/${id}`,
    { headers: this.getHeaders() }
  );
}

}