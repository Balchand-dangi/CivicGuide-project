import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, catchError, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Protects a route by checking the authenticated user's role with the backend.
 *
 * The role is NOT read from localStorage and the JWT is NOT exposed to Angular
 * because it is stored in an HttpOnly cookie.
 */
export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.getCurrentUser().pipe(
      map((user) => {
        if (allowedRoles.includes(user.role)) {
          return true;
        }

        // Authenticated, but trying to access a route for another role.
        redirectToOwnDashboard(user.role, router);
        return false;
      }),
      catchError(() => {
        // No valid authenticated session.
        router.navigate(['/login']);
        return of(false);
      }),
    );
  };
};

function redirectToOwnDashboard(role: string, router: Router): void {
  switch (role) {
    case 'USER':
      router.navigate(['/dashboard']);
      break;
    case 'MENTOR':
      router.navigate(['/mentor/dashboard']);
      break;
    case 'ADMIN':
      router.navigate(['/admin/dashboard']);
      break;
    default:
      router.navigate(['/login']);
  }
}

/**
 * Backward-compatible authentication-only guard for routes that only need
 * a valid session. Role-protected routes should use roleGuard([...]).
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isAuthenticated().pipe(
    map((isAuthenticated) => {
      if (isAuthenticated) {
        return true;
      }

      router.navigate(['/login']);
      return false;
    }),
  );
};
