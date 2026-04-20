import type { AppEnvironment } from './environment.model';

/** Entorno por defecto (sin `fileReplacements`). Mismas rutas relativas que en Docker detrás de Nginx. */
export const environment: AppEnvironment = {
  production: false,
  appTitle: 'Plataforma de Trámites',
  apiBackendUrl: '/backend',
  apiFastApiUrl: '/fastapi',
};
