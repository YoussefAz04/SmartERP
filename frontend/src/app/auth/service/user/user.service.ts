import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {HttpClient, HttpParams} from "@angular/common/http";
import { UserRequest} from "../../../model/user-request.model";
import {Observable} from "rxjs";
import {UserDTO} from "../../../model/userDTO.model";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = `${environment.apiUrl}/api/users`;

  constructor(private http: HttpClient) {}

  registerUser(request: unknown, idEntreprise: number, idEquipe: number): Observable<UserRequest> {
    const params = new HttpParams()
      .set('idEntreprise', idEntreprise)
      .set('idEquipe', idEquipe);
    return this.http.post<UserRequest>(`${this.apiUrl}/register`, request, { params });
  }

  createEmployeeUser(request: {
    username: string;
    email: string;
    password: string;
    phone?: string;
    enterpriseId?: number | null;
    moduleIds: number[];
  }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create-employee`, request);
  }

  getAllUsers(): Observable<UserRequest[]> {
    return this.http.get<UserRequest[]>(`${this.apiUrl}/getAllUsers`);
  }

  getAllUsersDTO(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${this.apiUrl}/getAllUsersDTo`);
  }

  getUsersByEnterprise(enterpriseId: number): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${this.apiUrl}/byEntreprise/${enterpriseId}`);
  }

  getUserById(id: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.apiUrl}/getUsersById/${id}`);
  }

  updateUser(id: number, user: UserDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}/updateUser/${id}`, user);
  }

  getAllUsersNotAffectedToMembre(): Observable<UserRequest[]> {
    return this.http.get<UserRequest[]>(`${this.apiUrl}/getAllUsersNotAffectedToMembre`);
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${userId}`);
  }
}
