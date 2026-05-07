import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private isRefreshing = false;
  private refreshDone$ = new BehaviorSubject<boolean>(false);

  constructor(private router: Router, private http: HttpClient) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // Attach stored Bearer token for backwards compatibility during migration,
    // and always send cookies (withCredentials) so the httpOnly cookie is included.
    const item = localStorage.getItem('myLSkey');
    let token: string | null = null;

    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        token = user.token || null;
      } catch {
        token = null;
      }
    }

    request = request.clone({ withCredentials: true });

    if (token) {
      request = request.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !this.isRefreshRequest(request)) {
          return this.handleUnauthorized(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  private isRefreshRequest(req: HttpRequest<unknown>): boolean {
    try {
      const url = new URL(req.url, window.location.origin);
      return url.pathname === '/api/auth/refresh' || url.pathname === '/api/auth/login';
    } catch {
      return req.url.endsWith('/api/auth/refresh') || req.url.endsWith('/api/auth/login');
    }
  }

  private handleUnauthorized(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    if (this.isRefreshing) {
      // Wait for the in-flight refresh to complete, then retry
      return this.refreshDone$.pipe(
        filter(done => done),
        take(1),
        switchMap(() => next.handle(this.cloneWithCredentials(request)))
      );
    }

    this.isRefreshing = true;
    this.refreshDone$.next(false);

    return this.http
      .post<any>(`${environment.apiUrl}/api/auth/refresh`, {}, { withCredentials: true })
      .pipe(
        switchMap((res: any) => {
          this.isRefreshing = false;
          // Update stored token with the new access token from the response body
          if (res?.token) {
            this.updateStoredToken(res.token);
          }
          this.refreshDone$.next(true);
          return next.handle(this.cloneWithCredentials(request));
        }),
        catchError(err => {
          this.isRefreshing = false;
          this.refreshDone$.next(false);
          localStorage.removeItem('myLSkey');
          localStorage.removeItem('entrepriseId');
          localStorage.removeItem('enterpriseId');
          localStorage.removeItem('token');
          localStorage.removeItem('authToken');
          this.router.navigate(['/auth/signin']);
          return throwError(() => err);
        })
      );
  }

  private cloneWithCredentials(req: HttpRequest<unknown>): HttpRequest<unknown> {
    return req.clone({ withCredentials: true });
  }

  private updateStoredToken(newToken: string): void {
    const item = localStorage.getItem('myLSkey');
    if (item) {
      try {
        const user = JSON.parse(decodeURIComponent(escape(atob(item))));
        user.token = newToken;
        localStorage.setItem('myLSkey', btoa(unescape(encodeURIComponent(JSON.stringify(user)))));
      } catch {
        // ignore parse errors
      }
    }
    localStorage.setItem('token', newToken);
    localStorage.setItem('authToken', newToken);
  }
}
