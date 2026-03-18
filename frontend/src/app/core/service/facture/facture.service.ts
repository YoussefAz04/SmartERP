import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {FactureAchat} from "../../../model/FactureAchat.model";

@Injectable({
  providedIn: 'root'
})
export class FactureService {

  private apiUrl = 'http://localhost:8020/facture';

  constructor(private http: HttpClient) { }

  getAllFactureAchat(): Observable<FactureAchat[]> {
    return this.http.get<FactureAchat[]>(`${this.apiUrl}/getAllFactureAchat`);
  }

  getFactureAchatById(id: number): Observable<FactureAchat> {
    const url = `${this.apiUrl}/getFactureAchatById/${id}`;
    return this.http.get<FactureAchat>(url);
  }
  updateFactureAchat(id: number, facture: FactureAchat): Observable<FactureAchat> {
    return this.http.put<FactureAchat>(`${this.apiUrl}/updateFactureAchat/${id}`, facture);
  }

  ajouterFacture(idDocument: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${idDocument}`, {});
  }

}
