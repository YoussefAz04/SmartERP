import { Injectable } from '@angular/core';
import {Role} from "../../../model/role.model";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private apiUrl = 'http://localhost:8020/api/roles';

  constructor(private http: HttpClient) {}

  // Créer un rôle
  createRole(role: Role): Observable<Role> {
    return this.http.post<Role>(`${this.apiUrl}/create`, role);
  }

  // Récupérer tous les rôles
  getAllRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/getAllRole`);
  }

  // Récupérer un rôle par ID
  getRoleById(id: number): Observable<Role> {
    return this.http.get<Role>(`${this.apiUrl}/getById/${id}`);
  }

  // Modifier un rôle
  updateRole(id: number, role: Role): Observable<Role> {
    return this.http.put<Role>(`${this.apiUrl}/updateRole/${id}`, role);
  }

  // Supprimer un rôle
  deleteRole(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }

  // Assigner un rôle à un utilisateur
  assignRoleToUser(idUser: number, idRole: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/assign-role/${idUser}/${idRole}`, null);
  }
}
