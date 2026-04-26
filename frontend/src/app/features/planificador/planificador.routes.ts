import { Routes } from '@angular/router';

export const planificadorRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./shell/planificador-shell.component').then((m) => m.PlanificadorShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'pendientes' },
      {
        path: 'pendientes',
        loadComponent: () =>
          import('./pendientes/planificador-pendientes.component').then((m) => m.PlanificadorPendientesComponent),
      },
    ],
  },
];
