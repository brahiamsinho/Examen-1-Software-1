# Estado actual del proyecto

## Frontend Angular (`frontend/`)

- **Shell**: layout con cabecera, navegación mínima e `Inicio` → `/dashboard`, área principal y pie.
- **Rutas**: hub `/acceso` con portales por rol; paneles lazy `admin`, `disenador`, `responsable-area` con `authGuard` + `roleGuard`; JWT en `sessionStorage` e interceptor Bearer.
- **Auth**: `POST /backend/api/auth/login` con `{ correo, contrasena, portalRol }` (proxy en dev). Semilla dev documentada en pantalla de login (solo no producción).
- **Diseñador de políticas** (`features/disenador-politicas`): shell con navbar/sidebar; rutas `resumen`, `politicas`, **`modelado`** — editor **BPMN** con `bpmn-js` (import/export XML), sin Mermaid.
- **Features**: `dashboard` como panel post-login genérico. Otras carpetas según repo.
- **Core**: `ApiService` con `getFromBackend` / `getFromFastApi`, constantes de endpoints y modelo `HealthResponseDto` alineado con Spring/FastAPI. Health Spring: `GET /backend/health` (proxy) → `/health` en el contenedor.
- **Entornos**: `environment.model.ts` + `environment.ts` / `environment.dev.ts` / `environment.prod.ts` con `appTitle` y URLs de API sin hardcodear hosts (prefijos Nginx).
- **Desarrollo local**: `proxy.conf.json` para `ng serve` hacia Spring (8080) y FastAPI (8000).
- **Tooling**: `tsconfig.app.json` incluye `src/**/*.ts` (antes solo `.d.ts`, lo que era incorrecto para el compilador).
- **Dependencias**: `bpmn-js` para modelado BPMN 2.0 en el diseñador.

## Backend Spring Boot (`backend/`)

- **Arquitectura**: paquetes por feature bajo `com.plataforma.tramites.modules.*` + `shared` (sin capas globales tipo `controllers/` únicos).
- **Persistencia**: Spring Data MongoDB; documentos alineados a colecciones de `script.db` / `init-mongo.js`. URI por `MONGODB_URI` / `MONGO_DB` en Compose.
- **Redis**: `spring-boot-starter-data-redis` + variables `REDIS_HOST` / `REDIS_PORT`; ping en `GET /api/seguridad/infra`.
- **APIs de dominio (mínimas)**: políticas CRUD; trámites + cola FIFO + recorridos; áreas; documentos/formularios; notificaciones y bitácora de dominio (`bitacora`); analítica/recomendaciones estructurales. El módulo **admin** sigue para operación global (`/api/admin/**`) y auditoría en `bitacora_auditoria`.

## Otros módulos del repo

- FastAPI, Docker Compose y Nginx siguen según el repositorio; FastAPI no concentra la lógica principal de negocio.
