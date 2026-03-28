import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
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
    token: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8020/api/auth';
  private tokenSubject = new BehaviorSubject<string | null>(this.getStoredToken());
  public token$ = this.tokenSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(credentials: AuthRequest): Observable<any> {
    console.log('🔐 Login attempt:', credentials.username);
    return this.http.post<any>(`${this.apiUrl}/login`, credentials);
  }

  register(data: RegisterRequest): Observable<any> {
    console.log('📝 Register attempt:', data.username);
    return this.http.post<any>(`${this.apiUrl}/register`, data);
  }

  logout(): void {
    console.log('🔓 Logging out...');
    
    localStorage.removeItem('authToken');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('myLSkey');
    sessionStorage.clear();
    
    this.tokenSubject.next(null);

    console.log('✅ Auth data cleared');

    setTimeout(() => {
      this.router.navigate(['/auth/signin']).then((success) => {
        if (success) {
          console.log('✅ Logged out and redirected to signin');
        } else {
          console.error('❌ Navigation failed');
          window.location.href = '/auth/signin';
        }
      }).catch((err) => {
        console.error('❌ Logout navigation error:', err);
        window.location.href = '/auth/signin';
      });
    }, 300);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired(token);
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  setToken(token: string): void {
    console.log('✅ Token saved');
    localStorage.setItem('token', token);
    localStorage.setItem('authToken', token);
    this.tokenSubject.next(token);
  }

  private getStoredToken(): string | null {
    return localStorage.getItem('token') || localStorage.getItem('authToken');
  }

  private isTokenExpired(token: string): boolean {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return true;
      
      const payload = JSON.parse(atob(parts[1]));
      const exp = payload.exp * 1000;
      return Date.now() >= exp;
    } catch (error) {
      console.error('❌ Token decode error:', error);
      return true;
    }
  }

  decodeToken(): any {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      return JSON.parse(atob(payload));
    } catch (e) {
      console.error('❌ Error decoding token:', e);
      return null;
    }
  }

  getRoles(): string[] {
    const decoded = this.decodeToken();
    return decoded?.roles || [];
  }

  refreshPermissionDtoList(userId: number): Observable<PermissionDto[]> {
    const params = new HttpParams().set('userId', userId);
    return this.http.post<PermissionDto[]>(`${this.apiUrl}/refreshPermissionDtoList`, null, { params });
  }
}