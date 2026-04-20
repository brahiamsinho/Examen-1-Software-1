/**
 * Contrato de configuración por entorno (Spring Boot vía Nginx: `/backend`, FastAPI: `/fastapi`).
 */
export interface AppEnvironment {
  production: boolean;
  /** Título mostrado en el shell (branding). */
  appTitle: string;
  /** Prefijo público del backend Spring (reverse proxy). Sin barra final. */
  apiBackendUrl: string;
  /** Prefijo público del microservicio FastAPI. Sin barra final. */
  apiFastApiUrl: string;
}
