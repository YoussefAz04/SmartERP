import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Module} from "../../../model/module.model";
import {ModuleTittle} from "../../../model/moduleTittle.model";
import {SousModule} from "../../../model/sousModule.model";

@Injectable({
  providedIn: 'root'
})
export class ModuleService {

  private apiUrl = 'http://localhost:8020/module'; // adapte au besoin

  constructor(private http: HttpClient) {}

  getAllModules(): Observable<Module[]> {
    return this.http.get<Module[]>(`${this.apiUrl}/getAllModule`);
  }
  getAllSousModule(): Observable<SousModule[]> {
    return this.http.get<SousModule[]>(`${this.apiUrl}/getAllSousModule`);
  }

  getAllModulesTittle(): Observable<ModuleTittle[]> {
    return this.http.get<ModuleTittle[]>(`${this.apiUrl}/getAllModulesTittle`);
  }

  getModuleById(id: number): Observable<Module> {
    return this.http.get<Module>(`${this.apiUrl}/getModule/${id}`);
  }

  updateStatusByRole(roleId: number): Observable<void> {
    const url = `${this.apiUrl}/update-status/${roleId}`;
    return this.http.put<void>(url, {});
  }
  addModuleTittle(moduleTittle: ModuleTittle): Observable<any> {
    return this.http.post(`${this.apiUrl}/addModuleTittle`, moduleTittle);
  }
  addModule(module: Module): Observable<any> {
    return this.http.post(`${this.apiUrl}/addModule`, module);
  }
  deleteModuleTittle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/deleteModuleTittle/${id}`);
  }
  getAllModulesNotAffecter(idModuleTittle: number): Observable<Module[]> {
    return this.http.get<Module[]>(`${this.apiUrl}/getAllModuleNotAffecter/${idModuleTittle}`);
  }
  affecterModulesAuModuleTittle(modules: Module[], idModuleTittle: number): Observable<any> {
    const url = `${this.apiUrl}/affecterModuleTittle/${idModuleTittle}`;
    return this.http.post<any>(url, modules);
  }
  addModuleWithTitreModule(module: Module, idModuleTitre: number): Observable<Module> {
    return this.http.post<Module>(`${this.apiUrl}/addModuleWithTitreModule/${idModuleTitre}`, module);
  }
  createSousModule(sousModule: SousModule, idModule: number): Observable<SousModule> {
    return this.http.post<SousModule>(
      `${this.apiUrl}/createSousModule/${idModule}`,
      sousModule
    );
  }


}
