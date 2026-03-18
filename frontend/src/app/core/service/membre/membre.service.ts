import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Membre} from "../../../model/membre.model";
import {Observable} from "rxjs";
import {UserRequest} from "../../../model/user-request.model";
import {Equipe} from "../../../model/equipe.model";
import { MembreDto } from 'src/app/model/membreDto.model';

@Injectable({
  providedIn: 'root'
})
export class MembreService {

  private apiUrl = `http://localhost:8020/membre`;

  constructor(private http: HttpClient) {}

  addMembre(membre: Membre, idEquipe: number): Observable<Membre> {
    return this.http.post<Membre>(`${this.apiUrl}/addMembre/${idEquipe}`, membre);
  }

  affecterUserToMembre(users: UserRequest[], idMembre: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/affecterUserToMembre/${idMembre}`, users);
  }
  getAllMembre(): Observable<Membre[]> {
    return this.http.get<Membre[]>(`${this.apiUrl}/getAllMembre`);
  }
  getMembreById(idMembre: number): Observable<MembreDto> {
    return this.http.get<MembreDto>(`${this.apiUrl}/getMembreById/${idMembre}`);
  }

  updateMembre(membreDto: MembreDto): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/update`, membreDto);
  }

  getAllMembresNonAffectes(): Observable<Membre[]> {
    return this.http.get<Membre[]>(`${this.apiUrl}/getAllMembreNotAffected`);
  }
  affecterMembresAEquipe(membres: Membre[], idEquipe: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/affecter-a-equipe/${idEquipe}`, membres);
  }
}

