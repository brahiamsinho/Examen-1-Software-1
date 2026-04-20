/**
 * Rutas canónicas conocidas del backend y del microservicio (sin prefijos de proxy).
 * Los prefijos `/backend` y `/fastapi` vienen de `environment`.
 */
export const ApiEndpoints = {
  springBoot: {
    health: '/health',
  },
  fastApi: {
    health: '/api/health',
  },
} as const;
