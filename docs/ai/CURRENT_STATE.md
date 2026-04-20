# Estado actual del proyecto

## Frontend Angular (`frontend/`)

- **Shell**: layout con cabecera, navegación mínima e `Inicio` → `/dashboard`, área principal y pie.
- **Rutas**: carga perezosa del shell y del panel; redirección `''` → `dashboard`; comodín a `''`.
- **Features**: `dashboard` implementado como panel vacío (sin pantallas de negocio). Carpetas reservadas: `seguridad`, `politicas`, `tramites`, `documentos`, `seguimiento`, `analitica` (solo `.gitkeep`). `shared/` reservado para UI transversal.
- **Core**: `ApiService` con `getFromBackend` / `getFromFastApi`, constantes de endpoints y modelo `HealthResponseDto` alineado con Spring/FastAPI. Health Spring: `GET /backend/health` (proxy) → `/health` en el contenedor.
- **Entornos**: `environment.model.ts` + `environment.ts` / `environment.dev.ts` / `environment.prod.ts` con `appTitle` y URLs de API sin hardcodear hosts (prefijos Nginx).
- **Desarrollo local**: `proxy.conf.json` para `ng serve` hacia Spring (8080) y FastAPI (8000).
- **Tooling**: `tsconfig.app.json` incluye `src/**/*.ts` (antes solo `.d.ts`, lo que era incorrecto para el compilador).

## Otros módulos del repo

- Backend Spring Boot, FastAPI, Docker Compose y Nginx siguen según el repositorio; no modificados en esta entrega salvo coherencia de contratos de health usados por el frontend.
