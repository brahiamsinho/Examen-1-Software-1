# Estado actual del proyecto

## Frontend Angular (`frontend/`)

- **Shell**: layout con cabecera, navegación mínima e `Inicio` → `/dashboard`, área principal y pie.
- **Rutas**: hub `/acceso` con portales por rol; paneles lazy `admin`, `disenador`, `responsable-area` con `authGuard` + `roleGuard`; JWT en `sessionStorage` e interceptor Bearer.
- **Auth**: `POST /backend/api/auth/login` con `{ correo, contrasena, portalRol }` (proxy en dev). Semilla dev documentada en pantalla de login (solo no producción).
- **Diseñador de políticas** (`features/disenador-politicas`): shell con navbar/sidebar; rutas `resumen`, `politicas`, **`modelado`** — editor de grafo con **AntV X6** (nodos/conexiones alineados a Mongo); **colaboración en tiempo real** para diseñador/admin vía WebSocket `ws(s)://…/backend/ws/politicas?access_token=…` (presencia + sincronización debounced del `toJSON` del grafo).
- **Features**: `dashboard` como panel post-login genérico. Otras carpetas según repo.
- **Core**: `ApiService` con `getFromBackend` / `getFromFastApi`, constantes de endpoints y modelo `HealthResponseDto` alineado con Spring/FastAPI. Health Spring: `GET /backend/health` (proxy) → `/health` en el contenedor.
- **Entornos**: `environment.model.ts` + `environment.ts` / `environment.dev.ts` / `environment.prod.ts` con `appTitle` y URLs de API sin hardcodear hosts (prefijos Nginx).
- **Desarrollo local**: `proxy.conf.json` para `ng serve` hacia Spring (8080) y FastAPI (8000).
- **Tooling**: `tsconfig.app.json` incluye `src/**/*.ts` (antes solo `.d.ts`, lo que era incorrecto para el compilador).
- **Dependencias**: `@antv/x6` (+ plugins teclado/selección) para el lienzo del diseñador.

## Backend Spring Boot (`backend/`)

- **Arquitectura**: paquetes por feature bajo `com.plataforma.tramites.modules.*` + `shared` (sin capas globales tipo `controllers/` únicos).
- **Persistencia**: Spring Data MongoDB; documentos alineados a colecciones de `script.db` / `init-mongo.js`. URI por `MONGODB_URI` / `MONGO_DB` en Compose.
- **Redis**: `spring-boot-starter-data-redis` + variables `REDIS_HOST` / `REDIS_PORT`; ping en `GET /api/seguridad/infra`.
- **APIs de dominio (mínimas)**: políticas CRUD (`lockVersion` en documento/respuesta para concurrencia); trámites + cola FIFO + recorridos; áreas; documentos/formularios; notificaciones y bitácora de dominio (`bitacora`); analítica/recomendaciones estructurales. El módulo **admin** sigue para operación global (`/api/admin/**`) y auditoría en `bitacora_auditoria`.
- **WebSocket**: endpoint `/ws/politicas` (módulo `politicas.collab`): salas en memoria por política; roles `DISENADOR_POLITICAS` y `ADMINISTRADOR`.

## Mobile Flutter (`mobile/tramites_cliente/`)

- **Login cliente**: pantalla minimalista (tipografía clara, superficie neutra, campos con subrayado vía `InputDecorationTheme`); campos alineados al dominio Mongo `usuarios` (**correo**, **contrasena** vía API); `POST /api/auth/login` con `portalRol: CLIENTE` (`AuthRepository`). URL base por `--dart-define=API_BASE_URL` o default emulador `http://10.0.2.2:8080`. Sin credenciales precargadas; pista de semilla **solo** en `kDebugMode` (sin contraseña en UI).

## Otros módulos del repo

- FastAPI, Docker Compose y Nginx siguen según el repositorio; FastAPI no concentra la lógica principal de negocio.
