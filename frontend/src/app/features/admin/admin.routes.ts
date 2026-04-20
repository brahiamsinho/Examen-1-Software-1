import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('@features/admin/shell/admin-shell.component').then((m) => m.AdminShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'resumen' },
      {
        path: 'resumen',
        loadComponent: () =>
          import('@features/admin/resumen/admin-home.component').then((m) => m.AdminHomeComponent),
      },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('@features/admin/usuarios/admin-users-page.component').then((m) => m.AdminUsersPageComponent),
      },
      {
        path: 'roles',
        loadComponent: () =>
          import('@features/admin/roles/admin-roles-page.component').then((m) => m.AdminRolesPageComponent),
      },
      {
        path: 'permisos',
        loadComponent: () =>
          import('@features/admin/permisos/admin-permissions-page.component').then(
            (m) => m.AdminPermissionsPageComponent,
          ),
      },
      {
        path: 'bitacora',
        loadComponent: () =>
          import('@features/admin/bitacora/admin-audit-page.component').then((m) => m.AdminAuditPageComponent),
      },
    ],
  },
];
