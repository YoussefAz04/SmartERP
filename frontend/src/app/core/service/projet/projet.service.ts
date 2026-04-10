import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {HttpClient, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs';
import {Projet, StatutProjet} from '../../../model/projet.model';

@Injectable({
  providedIn: 'root'
})
export class ProjetService {

  private baseUrl = `${environment.apiUrl}/projets`;

  constructor(private http: HttpClient) { }

  createProjetWithCategorie(projet: Projet, idCategorieProjet: number): Observable<Projet> {
    return this.http.post<Projet>(`${this.baseUrl}/addProjet/${idCategorieProjet}`, projet);
  }

  addProjet(projet: Projet): Observable<Projet> {
    return this.http.post<Projet>(`${this.baseUrl}/addProjetCategorie`, projet);
  }

  getAllProjets(): Observable<Projet[]> {
    return this.http.get<Projet[]>(`${this.baseUrl}/getAll`);
  }
  getAllProjetNotBudget(): Observable<Projet[]> {
    return this.http.get<Projet[]>(`${this.baseUrl}/getAllProjetNotBudget`);
  }

  getProjetById(id: number): Observable<Projet> {
    return this.http.get<Projet>(`${this.baseUrl}/getById/${id}`);
  }

  updateProjet(id: number, projet: Projet): Observable<Projet> {
    return this.http.put<Projet>(`${this.baseUrl}/update/${id}`, projet);
  }

  deleteProjet(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete/${id}`);
  }
  getAllProjetsByStatus(status: StatutProjet): Observable<Projet[]> {
    const params = new HttpParams().set('statusProjet', status);
    return this.http.get<Projet[]>(`${this.baseUrl}/getAllByStatus`, { params });
  }
  updateStatutProjet(idProjet: number, statutProjet: string): Observable<any> {
    const params = new HttpParams().set('statutProjet', statutProjet);
    return this.http.put(`${this.baseUrl}/updateStatut/${idProjet}`, null, { params });
  }
  getNombreProjetByStatut(statut: string): Observable<number> {
    const params = new HttpParams().set('statut', statut);
    return this.http.get<number>(`${this.baseUrl}/count-by-status`, { params });
  }
  nombreDeTacheByProjet(id: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${id}/nombre-taches`);
  }
}
