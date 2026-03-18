import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {Departement} from "../../../model/departement.model";
import {HttpClient} from "@angular/common/http";
import {DepartementDTO} from "../../../Dto/departement.dto";

@Injectable({
  providedIn: 'root'
})
export class DepartementService {

  constructor(private http: HttpClient) {}
  private baseUrl = 'http://localhost:8020/departement'; // à adapter


  getAllDepartements(): Observable<DepartementDTO[]> {
    return this.http.get<DepartementDTO[]>(`${this.baseUrl}/all`);
  }

  createDepartement(departement: Departement,entrepriseId : number): Observable<Departement> {
    return this.http.post<Departement>(`${this.baseUrl}/add/${entrepriseId}`, departement);
  }

  updateDepartement(id: number, departement: Departement): Observable<Departement> {
    return this.http.put<Departement>(`${this.baseUrl}/update/${id}`, departement);
  }

  deleteDepartement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete/${id}`);
  }

  getDepartementDToById(id: number): Observable<DepartementDTO> {
    return this.http.get<DepartementDTO>(`${this.baseUrl}/${id}`);
  }
}
