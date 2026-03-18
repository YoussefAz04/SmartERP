import {Injectable} from '@angular/core';
import {Equipe} from "../../../model/equipe.model";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {EquipeDto} from "../../../model/equipeDto.model";

@Injectable({
  providedIn: 'root'
})
export class EquipeService {
  private apiUrl = 'http://localhost:8020/equipe';

  constructor(private http: HttpClient) {}

  createEquipe(equipe: Equipe): Observable<Equipe> {
    return this.http.post<Equipe>(`${this.apiUrl}/create`, equipe);
  }

  getAllEquipes(): Observable<Equipe[]> {
    return this.http.get<Equipe[]>(`${this.apiUrl}/getAllEquipe`);
  }

  getEquipeById(id: number): Observable<EquipeDto> {
    return this.http.get<EquipeDto>(`${this.apiUrl}/getEquipeById/${id}`);
  }
  updateEquipe(idEquipe: number, equipeDto: EquipeDto): Observable<any> {
    return this.http.put(`${this.apiUrl}/update/${idEquipe}`, equipeDto);
  }

}
