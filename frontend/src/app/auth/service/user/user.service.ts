import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import { UserRequest} from "../../../model/user-request.model";
import {Observable} from "rxjs";
import {UserDTO} from "../../../model/userDTO.model";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = 'http://localhost:8020/api/users';

  constructor(private http: HttpClient) {}

  registerUser(request: unknown,idEntreprise:number, idEquipe:number): Observable<UserRequest> {
    const params = new HttpParams()
      .set('idEntreprise',idEntreprise)
      .set('idEquipe',idEquipe)
    return this.http.post<UserRequest>(`${this.apiUrl}/register`, request,{ params });
  }
  getAllUsers(): Observable<UserRequest[]> {
    return this.http.get<UserRequest[]>(`${this.apiUrl}/getAllUsers`);
  }
  getAllUsersDTO(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${this.apiUrl}/getAllUsersDTo`);
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
}
