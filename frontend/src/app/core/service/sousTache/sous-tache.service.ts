import { Injectable } from '@angular/core';
import { SousTache } from 'src/app/model/sousTache.model';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SousTacheService {

  private apiUrl = 'http://localhost:8020/sous-taches';

  constructor(private http: HttpClient) {}

  getAll(): Observable<SousTache[]> {
    return this.http.get<SousTache[]>(`${this.apiUrl}/getAll`);
  }

  getById(id: number): Observable<SousTache> {
    return this.http.get<SousTache>(`${this.apiUrl}/${id}`);
  }

  create(sousTache: SousTache): Observable<SousTache> {
    return this.http.post<SousTache>(`${this.apiUrl}/add`, sousTache);
  }

  update(id: number, sousTache: SousTache): Observable<SousTache> {
    return this.http.put<SousTache>(`${this.apiUrl}/${id}`, sousTache);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  assignerSousTache(idTache: number, idSousTache: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/assign-sous-tache/${idTache}/${idSousTache}`, null);
  }

  createWithTache(sousTache: SousTache, tacheId: number): Observable<SousTache> {
    return this.http.post<SousTache>(`${this.apiUrl}/with-tache/${tacheId}`, sousTache);
  }
}
