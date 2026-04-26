import { Routes } from '@angular/router';
import { authGuard } from '@core/auth/auth.guard';
import { roleGuard } from '@core/auth/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'acceso' },
  {
    path: 'acceso',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/auth/acceso-hub.component').then((m) => m.AccesoHubComponent),
      },
      {
        path: 'administrador',
        loadComponent: () =>
          import('./features/auth/portal-login.component').then((m) => m.PortalLoginComponent),
        data: { portalRol: 'ADMINISTRADOR', portalTitulo: 'Portal administrador' },
      },
      {
        path: 'politicas',
        loadComponent: () =>
          import('./features/auth/portal-login.component').then((m) => m.PortalLoginComponent),
        data: {
          portalRol: 'DISENADOR_POLITICAS',
          portalTitulo: 'Diseñador de políticas de negocio',
        },
      },
      {
        path: 'area',
        loadComponent: () =>
          import('./features/auth/portal-login.component').then((m) => m.PortalLoginComponent),
        data: { portalRol: 'RESPONSABLE_AREA', portalTitulo: 'Responsable de área' },
      },
      {
        path: 'planificador',
        loadComponent: () =>
          import('./features/auth/portal-login.component').then((m) => m.PortalLoginComponent),
        data: { portalRol: 'PLANIFICADOR', portalTitulo: 'Planificador de trámites' },
      },
    ],
  },
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.routes').then((m) => m.adminRoutes),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMINISTRADOR'] },
  },
  {
    path: 'disenador',
    loadChildren: () =>
      import('./features/disenador-politicas/disenador-politicas.routes').then((m) => m.disenadorPoliticasRoutes),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['DISENADOR_POLITICAS'] },
  },
  {
    path: 'responsable-area',
    loadChildren: () =>
      import('./features/responsable-area/responsable-area.routes').then((m) => m.responsableAreaRoutes),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['RESPONSABLE_AREA'] },
  },
  {
    path: 'planificador',
    loadChildren: () => import('./features/planificador/planificador.routes').then((m) => m.planificadorRoutes),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PLANIFICADOR'] },
  },
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/shell-layout.component').then((m) => m.ShellLayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard-page.component').then((m) => m.DashboardPageComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'acceso' },
];
