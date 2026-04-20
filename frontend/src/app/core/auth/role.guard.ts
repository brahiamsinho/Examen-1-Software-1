import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const required = route.data['roles'] as string[] | undefined;
  if (!required?.length) {
    return true;
  }
  const rol = auth.getRolCodigo();
  if (!rol || !required.includes(rol)) {
    void router.navigateByUrl(auth.portalHomeUrl());
    return false;
  }
  return true;
};
