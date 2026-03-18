import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Permission} from "../../../model/permission.model";
import {Observable} from "rxjs";
import {PermissionDto} from "../../../model/permissionDto.model";

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  private apiUrl = 'http://localhost:8020/permission'; // adapte l'URL de base

  constructor(private http: HttpClient) {}

  addPermission(permission: Permission, idRole: number, idModule: number): Observable<Permission> {
    const url = `${this.apiUrl}/add/${idRole}/${idModule}`;
    return this.http.post<any>(url, permission);
  }
  getPermissionsByRole(idRole: number): Observable<PermissionDto[]> {
    return this.http.get<PermissionDto[]>(`${this.apiUrl}/getPermissionByRole/${idRole}`);
  }

}

