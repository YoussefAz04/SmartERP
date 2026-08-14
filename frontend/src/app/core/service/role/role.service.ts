import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {Role} from "../../../model/role.model";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

export type RoleType = 'SUPER_ADMIN' | 'ADMIN' | 'EMPLOYEE';

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private apiUrl = `${environment.apiUrl}/api/roles`;

  constructor(private http: HttpClient) {}

  // Create a role
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

  // Assign role à un utilisateur
  assignRoleToUser(idUser: number, idRole: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/assign-role/${idUser}/${idRole}`, null);
  }

  // Retirer un rôle d'un utilisateur
  removeRoleFromUser(idUser: number, idRole: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/remove-role/${idUser}/${idRole}`);
  }

  // ----------------------------------------------------------------
  // Role-checking helpers (read from localStorage session)
  // ----------------------------------------------------------------

  /**
   * Returns the role names stored in the current session.
   */
  getCurrentRoles(): string[] {
    try {
      const raw = localStorage.getItem('myLSkey');
      if (!raw) return [];
      const session = JSON.parse(decodeURIComponent(escape(atob(raw))));
      return Array.isArray(session.roles) ? session.roles : [];
    } catch {
      return [];
    }
  }

  /** True if the authenticated user has the given role. */
  hasRole(role: RoleType): boolean {
    return this.getCurrentRoles().includes(role);
  }

  /** True if the user has any of the given roles. */
  hasAnyRole(roles: RoleType[]): boolean {
    const current = this.getCurrentRoles();
    return roles.some(r => current.includes(r));
  }

  isSuperAdmin(): boolean {
    return this.hasRole('SUPER_ADMIN');
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  isEmployee(): boolean {
    return this.hasRole('EMPLOYEE');
  }

  /** True for SUPER_ADMIN or ADMIN. */
  isAdminOrAbove(): boolean {
    return this.hasAnyRole(['SUPER_ADMIN', 'ADMIN']);
  }
}

