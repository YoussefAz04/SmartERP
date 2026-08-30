import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from "rxjs";
import { HttpClient, HttpParams } from "@angular/common/http";
import { PermissionDto } from "../../model/permissionDto.model";
import { Entreprise } from "../../model/entreprise.model";
import { Router } from '@angular/router';


// ✅ FIXED: Changed to accept username instead of email
export interface AuthRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  phone?: string;
}

export interface AuthResponse {
  status: string;
  message: string;
  data: {
    id: number;
    username: string;
    email: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = `${environment.apiUrl}/api/auth`;
  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(credentials: AuthRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials);
  }

  register(data: RegisterRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, data);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/reset-password`, { token, newPassword });
  }

  logout(): void {
    // Revoke the refresh token server-side and clear auth cookies
    this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe({
      error: () => { /* ignore network errors during logout */ }
    });

    localStorage.removeItem('authToken');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('myLSkey');
    sessionStorage.clear();
    
    setTimeout(() => {
      this.router.navigate(['/auth/signin']).then((success) => {
        if (success) {
        } else {
          window.location.href = '/auth/signin';
        }
      }).catch((err) => {
        window.location.href = '/auth/signin';
      });
    }, 300);
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('myLSkey') !== null;
  }

  getRoles(): string[] {
    const stored = localStorage.getItem('myLSkey');
    if (!stored) return [];
    try {
      const session = JSON.parse(decodeURIComponent(escape(atob(stored))));
      return Array.isArray(session.roles) ? session.roles : [];
    } catch {
      return [];
    }
  }

  refreshPermissionDtoList(userId: number): Observable<PermissionDto[]> {
    const params = new HttpParams().set('userId', userId);
    return this.http.post<PermissionDto[]>(`${this.apiUrl}/refreshPermissionDtoList`, null, { params });
  }
}
