import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Client} from "../../../model/client.model";
import {Observable} from "rxjs";
import {CategorieClient} from "../../../model/categorieClient.model";

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private baseUrl = 'http://localhost:8020/clients';

  constructor(private http: HttpClient) { }

  addClient(client: Client, idCategorie: number): Observable<Client> {
    return this.http.post<Client>(`${this.baseUrl}/addClient/${idCategorie}`, client);
  }

  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.baseUrl}/getAllClients`);
  }

  getClientById(id: number): Observable<Client> {
    return this.http.get<Client>(`${this.baseUrl}/getClientById/${id}`);
  }

  addCategorieClient(categorie: CategorieClient): Observable<CategorieClient> {
    return this.http.post<CategorieClient>(`${this.baseUrl}/addCategorieClient`, categorie);
  }

  getAllCategorieClients(): Observable<CategorieClient[]> {
    return this.http.get<CategorieClient[]>(`${this.baseUrl}/getAllCategorieClients`);
  }

  getCategorieClientById(id: number): Observable<CategorieClient> {
    return this.http.get<CategorieClient>(`${this.baseUrl}/getCategorieClientById/${id}`);
  }
}
