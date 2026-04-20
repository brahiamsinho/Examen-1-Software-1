import { Routes } from '@angular/router';

export const disenadorPoliticasRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./shell/disenador-politicas-shell.component').then((m) => m.DisenadorPoliticasShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'resumen' },
      {
        path: 'resumen',
        loadComponent: () =>
          import('./resumen/disenador-politicas-resumen.component').then(
            (m) => m.DisenadorPoliticasResumenComponent,
          ),
      },
      {
        path: 'politicas',
        loadComponent: () =>
          import('./politicas/disenador-politicas-catalogo.component').then(
            (m) => m.DisenadorPoliticasCatalogoComponent,
          ),
      },
    ],
  },
];
