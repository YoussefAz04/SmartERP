import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
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

    if (token) {
      request = request.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          localStorage.removeItem('myLSkey');
          localStorage.removeItem('entrepriseId');
          localStorage.removeItem('enterpriseId');
          this.router.navigate(['/auth/signin']);
        }
        return throwError(() => error);
      })
    );
  }
}
