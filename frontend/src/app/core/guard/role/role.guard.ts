import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { RoleService, RoleType } from '../../../core/service/role/role.service';

/**
 * Route guard that restricts navigation to users that hold at least one of
 * the roles listed in route.data.requiredRoles.
 *
 * Usage in route definitions:
 *   {
 *     path: 'user-management',
 *     canActivate: [RoleGuard],
 *     data: { requiredRoles: ['SUPER_ADMIN', 'ADMIN'] },
 *     ...
 *   }
 */
@Injectable({
  providedIn: 'root',
})
export class RoleGuard {
  constructor(private roleService: RoleService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    const requiredRoles: RoleType[] = route.data?.['requiredRoles'] ?? [];

    if (requiredRoles.length === 0 || this.roleService.hasAnyRole(requiredRoles)) {
      return true;
    }

    return this.router.createUrlTree(['/error-pages/error-404']);
  }
}
