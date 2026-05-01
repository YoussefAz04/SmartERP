import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {HttpClient} from "@angular/common/http";
import {Permission} from "../../../model/permission.model";
import {Observable} from "rxjs";
import {PermissionDto} from "../../../model/permissionDto.model";

export interface ModulePermissionRequest {
  moduleId: number;
  create: boolean;
  update: boolean;
  view: boolean;
  delete: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  private apiUrl = `${environment.apiUrl}/permission`; // adapte l'URL de base

  constructor(private http: HttpClient) {}

  addPermission(permission: Permission, idRole: number, idModule: number): Observable<Permission> {
    const url = `${this.apiUrl}/add/${idRole}/${idModule}`;
    return this.http.post<any>(url, permission);
  }

  getPermissionsByRole(idRole: number): Observable<PermissionDto[]> {
    return this.http.get<PermissionDto[]>(`${this.apiUrl}/getPermissionByRole/${idRole}`);
  }

  getPermissionsByUser(userId: number): Observable<PermissionDto[]> {
    return this.http.get<PermissionDto[]>(`${this.apiUrl}/getPermissionByUser/${userId}`);
  }

  updateUserPermissions(userId: number, permissions: ModulePermissionRequest[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/updateUserPermissions/${userId}`, permissions);
  }
}

