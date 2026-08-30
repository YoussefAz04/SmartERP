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
    // Access and refresh tokens are HttpOnly cookies. Sending credentials is
    // sufficient and keeps bearer tokens unavailable to JavaScript.
    request = request.clone({ withCredentials: true });

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
        switchMap(() => {
          this.isRefreshing = false;
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

}
