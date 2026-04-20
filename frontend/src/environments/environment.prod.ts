import type { AppEnvironment } from './environment.model';

/** Producción: el navegador llama a los mismos prefijos; Nginx enruta a contenedores. */
export const environment: AppEnvironment = {
  production: true,
  appTitle: 'Plataforma de Trámites',
  apiBackendUrl: '/backend',
  apiFastApiUrl: '/fastapi',
};
