import type { AppEnvironment } from './environment.model';

/** Desarrollo (`ng serve --configuration=development`): usar `proxy.conf.json` para Spring/FastAPI locales. */
export const environment: AppEnvironment = {
  production: false,
  appTitle: 'Plataforma de Trámites',
  apiBackendUrl: '/backend',
  apiFastApiUrl: '/fastapi',
};
