import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {FactureAchat} from "../../../model/FactureAchat.model";
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FactureService {

  private apiUrl = `${environment.apiUrl}/facture`;

  constructor(private http: HttpClient) { }

  getAllFactureAchat(): Observable<FactureAchat[]> {
  return this.http.get<FactureAchat[]>(`${this.apiUrl}/getAllFactureAchat`);
}

  getFactureAchatById(id: number): Observable<FactureAchat> {
    const url = `${this.apiUrl}/getFactureAchatById/${id}`;
    return this.http.get<FactureAchat>(url);
  }

  /** Récupérer l'historique des factures d'achat d'un fournisseur. */
  getFacturesByFournisseur(fournisseurId: number): Observable<FactureAchat[]> {
    return this.http.get<FactureAchat[]>(`${this.apiUrl}/fournisseur/${fournisseurId}`);
  }

  updateFactureAchat(id: number, facture: FactureAchat): Observable<FactureAchat> {
    return this.http.put<FactureAchat>(`${this.apiUrl}/updateFactureAchat/${id}`, facture);
  }

  ajouterFacture(idDocument: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${idDocument}`, {});
  }

  deleteFactureAchat(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/deleteFactureAchat/${id}`);
  }

  /**
   * Importer une facture d'achat depuis un PDF en utilisant les zones OCR
   * sauvegardées (typeDeDocument = "FACTURE_ACHAT").
   */
  importFromPdf(file: File): Observable<Record<string, unknown>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Record<string, unknown>>(`${this.apiUrl}/import-pdf`, formData);
  }

  /** Importer + sauvegarder en une étape la facture d'achat extraite. */
  saveFromPdf(file: File): Observable<Record<string, unknown>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Record<string, unknown>>(`${this.apiUrl}/save-from-pdf`, formData);
  }

}
