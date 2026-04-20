import { Routes } from '@angular/router';

export const responsableAreaRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./shell/responsable-area-shell.component').then((m) => m.ResponsableAreaShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'resumen' },
      {
        path: 'resumen',
        loadComponent: () =>
          import('./resumen/responsable-area-resumen.component').then((m) => m.ResponsableAreaResumenComponent),
      },
      {
        path: 'tramites',
        loadComponent: () =>
          import('./tramites/responsable-area-tramites.component').then((m) => m.ResponsableAreaTramitesComponent),
      },
    ],
  },
];
