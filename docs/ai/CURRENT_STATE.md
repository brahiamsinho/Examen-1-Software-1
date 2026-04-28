# Estado actual del proyecto

## Frontend Angular (`frontend/`)

- **Shell**: layout con cabecera, navegación mínima e `Inicio` → `/dashboard`, área principal y pie.
- **Rutas**: hub `/acceso` con portales por rol; paneles lazy `admin`, `disenador`, `responsable-area` con `authGuard` + `roleGuard`; JWT en `sessionStorage` e interceptor Bearer.
- **Auth**: `POST /backend/api/auth/login` con `{ correo, contrasena, portalRol }` (proxy en dev). Semilla dev documentada en pantalla de login (solo no producción).
- **Diseñador de políticas** (`features/disenador-politicas`): shell con navbar/sidebar; rutas `resumen`, `politicas`, **`modelado`** — editor BPMN completo con **bpmn-js** (reemplazo de X6), toolbar de modelado, panel de propiedades y validación básica. Persistencia canónica en `bpmnXml` + adaptación a `nodos`/`conexiones` para compatibilidad del runtime actual.
- **UI/UX modelador BPMN (2026-04-28 madrugada)**: toolbar renovada con acción **"Carriles por departamento"**, tarjeta visual para acciones, panel de propiedades más legible y canvas con look & feel más cercano a notación BPMN por carriles.
- **Carriles por departamento (2026-04-28 madrugada)**: en `disenador-politicas-modelado`, el modelador ahora puede sincronizar lanes en base a áreas activas (`areas`). Si no existe colaboración/participante, se crea automáticamente y se generan/renombran lanes con nombres de departamento.
- **Asignación automática a carril**: al aplicar propiedades de un nodo, el `carrilBpmn` se deriva del área seleccionada (si no se escribe manualmente) y el elemento seleccionado se mueve al lane correspondiente.
- **Metadatos BPMN de negocio (2026-04-28)**: por elemento se gestionan `carrilBpmn`, `formularioExternoUrl` (HTTPS), `areaId` y `asignacionesResponsable` en `documentation` JSON del XML BPMN.
- **Versionado de políticas**: `politicas_negocio_revisiones` con snapshots por POST/PUT + APIs `GET /api/politicas/:id/revisiones` y `GET .../revisiones/:n`.
- **Backend auth error JSON (2026-04-27)**: `RestAuthenticationEntryPoint` ya no crea `new ObjectMapper()`; ahora inyecta el mapper de Spring para serializar correctamente `Instant` en `ErrorResponse` (401/errores de auth).
- **Backend políticas (2026-04-27)**: `GET /api/politicas/{id}` ahora devuelve IDs anidados como `string` hex (no como objeto BSON `{timestamp,date}`), incluyendo `nodos.areaId` y `asignacionesResponsable.{usuarioId,areaId}`. Esto estabiliza la recarga visual del diseñador.
- **Fix crítico persistencia modelador (2026-04-27)**: `PUT /api/politicas/{id}` podía responder `500` en políticas con documento legacy sin `lockVersion`. Se agregó inicialización automática de `lockVersion=0` en `PoliticasDominioService` antes del guardado (vía `MongoTemplate`) para migrar en caliente y evitar pérdida de cambios tras `F5`. Cubierto por test de integración `PoliticasDominioServiceLegacyLockMongoIT` (documento sin campo `lockVersion` → `reemplazar` exitoso, lock pasa a `1`).
- **Manejo de conflicto Mongo mejorado (2026-04-27)**: `DuplicateKeyException` ahora se traduce a `409 CONFLICT` en `GlobalExceptionHandler` con mensaje funcional (antes quedaba como `500` genérico).
- **Regresión frontend BPMN (2026-04-28)**: prueba `bpmn-policy-adapter.spec.ts` para mapeo BPMN XML <-> `PoliticaUpsertBody` y validación estructural base.
- **BPMN canvas en blanco (seguimiento 2026-04-28)**:
  - Se removió configuración inválida de `bpmn-js` (`keyboard.bindTo`) en `disenador-politicas-modelado.component.ts`.
  - El adaptador `bpmn-policy-adapter.ts` ahora genera bloque BPMN-DI (`BPMNDiagram`, `BPMNPlane`, `BPMNShape`, `BPMNEdge`) cuando se reconstruye XML desde `nodos`/`conexiones`.
  - Se agregó `canvas.zoom('fit-viewport')` tras `importXML` para evitar que el diagrama quede fuera de vista.
  - `loadPolitica` aplica fallback defensivo a `defaultBpmnXml()` si el `bpmnXml` persistido es inválido, mostrando banner de error funcional.
  - Se movieron estilos base de `diagram-js` y fuente BPMN a `src/styles.scss` (global) para evitar pérdida de estilos por scoping del componente.
  - `importXml` ahora muestra advertencias de `importXML` en banner y fuerza `canvas.resized()+fit-viewport` en doble pasada para contenedores con layout tardío.
- **Planificador** (`features/planificador`): acceso `/acceso/planificador`; módulo lazy `/planificador/pendientes` para asignar política a trámites ingresados sin `politicaId`.
- **Features**: `dashboard` como panel post-login genérico. Otras carpetas según repo.
- **Core**: `ApiService` con `getFromBackend` / `getFromFastApi`, constantes de endpoints y modelo `HealthResponseDto` alineado con Spring/FastAPI. Health Spring: `GET /backend/health` (proxy) → `/health` en el contenedor.
- **Entornos**: `environment.model.ts` + `environment.ts` / `environment.dev.ts` / `environment.prod.ts` con `appTitle` y URLs de API sin hardcodear hosts (prefijos Nginx).
- **Desarrollo local**: `proxy.conf.json` para `ng serve` hacia Spring (8080) y FastAPI (8000).
- **Tooling**: `tsconfig.app.json` incluye `src/**/*.ts` (antes solo `.d.ts`, lo que era incorrecto para el compilador).
- **Dependencias**: `bpmn-js` + `bpmn-moddle` para modelado BPMN.

## Backend Spring Boot (`backend/`)

- **Arquitectura**: paquetes por feature bajo `com.plataforma.tramites.modules.*` + `shared` (sin capas globales tipo `controllers/` únicos).
- **Persistencia**: Spring Data MongoDB; documentos alineados a colecciones de `script.db` / `init-mongo.js`. URI por `MONGODB_URI` / `MONGO_DB` en Compose.
- **Redis**: `spring-boot-starter-data-redis` + variables `REDIS_HOST` / `REDIS_PORT`; ping en `GET /api/seguridad/infra`.
- **APIs de dominio (mínimas)**: políticas CRUD (`lockVersion` en documento/respuesta para concurrencia); trámites + cola FIFO + recorridos; áreas; documentos/formularios; notificaciones y bitácora de dominio (`bitacora`); analítica/recomendaciones estructurales. El módulo **admin** sigue para operación global (`/api/admin/**`) y auditoría en `bitacora_auditoria`.
- **WebSocket**: endpoint `/ws/politicas` (módulo `politicas.collab`): salas en memoria por política; roles `DISENADOR_POLITICAS` y `ADMINISTRADOR`.
- **Planificador**: rol `PLANIFICADOR` (semilla `planificador@tramites.local` / `demo123`); API bajo `/api/planificador/tramites` — listado de trámites sin política y `POST .../{id}/asignar-politica` (ubica en nodo `esInicial` de la política). Flujo de ramas paralelas: `GET/POST /api/tramites/{id}/flujo/salidas` y `aprobar-rama-paralela` (join por sucesor común directo).
- **Integración IA (2026-04-27)**: `FastApiMlClient` (WebClient) en módulo `planificador/client` con métodos `sugerirPolitica()` y `analizarCuellosBotella()`. Endpoints:
  - `GET /api/planificador/tramites/{id}/sugerir-politica` → sugiere política vía IA
  - `POST /api/planificador/tramites/{id}/registrar-decision-ia` → auditoría aceptar/rechazar
  - `GET /api/analitica/politicas/{politicaId}/cuellos-botella` → proxy a FastAPI bottleneck
- **Auditoría IA**: `DecisionIaAuditService` en módulo planificador (log + memoria)
- **Notificaciones push FCM (2026-04-27)**: módulo `seguimiento` con `FcmTokenDocument` (colección `fcm_tokens`), `FcmTokenRepository`, `FcmNotificationService`. `FirebaseConfiguration` inicializa Firebase Admin SDK desde `app.firebase.credentials-path`. `POST /api/seguimiento/notificaciones/fcm-token` registra token del usuario. `TramiteFlujoService` envía notificación al cliente cuando el trámite avanza o completa etapa paralela.
- **Flujo condicional + paralelo multi-hop (2026-04-27)**:
  - Nuevo `TramiteFlujoCondicionEvaluator` en `modules/tramites/service` (`==`, `!=`, `in [...]`, `exists`).
  - `TramiteFlujoService.avanzar` ahora valida salidas por condición y devuelve error funcional cuando no hay salida válida.
  - `aprobarRamaParalela` pasa de join directo a convergencia multi-salto (BFS con profundidad máxima) para ramas `PARALELO`.
  - Contexto de condición incluye campos de `tramite` y, cuando existe, datos del último formulario del trámite.
  - Pruebas E2E-lite con `MockMvc` en `TramiteFlujoControllerIntegrationTest` para `/flujo/avanzar` (condición true -> 200, condición false -> 422 `SIN_SALIDA_VALIDA`, condición inválida -> 400 `CONDICION_INVALIDA`, planificador/no auth/token inválido -> 403/401).
  - Se validó idempotencia en `POST /flujo/aprobar-rama-paralela`: repetir la misma rama no rompe estado ni duplica avance (permanece en split hasta completar ramas).
  - Integración con repos reales (Mongo) agregada en `TramiteFlujoServiceMongoIntegrationTest` usando Testcontainers:
    - persiste `nodoActualId`/`areaActualId` al `avanzar`,
    - persiste convergencia a `join` y limpieza de estado paralelo al completar ramas.
  - Cobertura Mongo real ampliada (fase 9):
    - idempotencia de rama repetida en paralelo (no duplica aprobación ni avanza prematuramente),
    - no convergencia multi-hop retorna `422 CONVERGENCIA_NO_ENCONTRADA`,
    - condición basada en `form.*` (ej. `form.tipo`) habilita transición usando último formulario persistido.
  - Endurecimiento del mini-lenguaje de condiciones (fase 10):
    - parser con patrones explícitos para `==`, `!=`, `in [..]`, `exists`,
    - validación de nombre de campo (`[A-Za-z_][A-Za-z0-9_.]*`),
    - rechazo de `in []` y valores vacíos en comparaciones,
    - mensajes `CONDICION_INVALIDA` más guiados con ejemplos válidos.
  - Contrato HTTP reforzado (fase 11) en `TramiteFlujoControllerIntegrationTest`:
    - `400 CONDICION_INVALIDA` ahora se valida con mensaje guiado (incluye "Operadores soportados" y "Ejemplos"),
    - casos de campo inválido y `in []` cubiertos end-to-end en endpoint `/api/tramites/{id}/flujo/avanzar`.
  - Fix de build Docker backend (fase 12):
    - agregado `org.testcontainers:junit-jupiter` en `backend/pom.xml` (scope `test`) para resolver anotaciones `@Testcontainers` / `@Container` durante `testCompile` en `mvn clean package -DskipTests`.

## Mobile Flutter (`mobile/tramites_cliente/`)

- **Login cliente**: pantalla minimalista (tipografía clara, superficie neutra, campos con subrayado vía `InputDecorationTheme`); campos alineados al dominio Mongo `usuarios` (**correo**, **contrasena** vía API); `POST /api/auth/login` con `portalRol: CLIENTE` (`AuthRepository`). URL base por `--dart-define=API_BASE_URL` o `.env` → `API_BASE_URL`; default sin `.env`: Android emulador `http://10.0.2.2:8080`, iOS simulador `http://127.0.0.1:8080`. **Dispositivo físico + Docker:** Compose publica Spring en host `${BACKEND_HOST_PORT:-8080}:8080`; en el teléfono hace falta `.env` con la IPv4 LAN del PC (misma WiFi). Mensaje de error de red en `kDebugMode` incluye la base URL efectiva. Sin credenciales precargadas; pista de semilla **solo** en `kDebugMode` (sin contraseña en UI).
- **Persistencia de sesión (2026-04-27)**: token + datos de usuario se guardan en `flutter_secure_storage` (SharedPreferences cifrado en Android, Keychain en iOS). `SecureStorage` en `core/secure_storage.dart` maneja `saveSession`, `getToken`, `getUserInfo`, `clearSession`. `main.dart` usa `StatefulWidget` que carga sesión al iniciar y redirige a `/` si hay token válido.
- **Ingreso de documento (cliente)**: ya no elige política. Home envía archivo a `/api/cliente/tramites/inicio-con-documento` (Multipart `archivo`). Backend crea trámite en `ATENCION_CLIENTE` (`app.workflow.intake-*`) y registra recorrido. **Docker (fix 2026-04-27):** el mensaje **"No se pudo guardar el archivo"** venía de `IOException` al escribir en `APP_UPLOAD_DIR` (default `./data/uploads`): el usuario no-root del contenedor no podía crear `/app/data`; `backend/Dockerfile` hace `mkdir` + `chown` de `/app/data` para `appuser`. **Seguimiento:** `GET /api/cliente/tramites` y `GET /api/cliente/tramites/{id}` (solo dueño). App Flutter: **Mis trámites**.
- **Perfil del usuario**: pantalla `/perfil` muestra nombre completo, correo, rol (con label legible), avatar con iniciales, botón de copiar al portapapeles y logout con confirmación. Acceso desde ícono de persona en AppBar de Home.
- **Dependencias**: `http`, `go_router`, `file_picker`, `flutter_secure_storage`, `flutter_dotenv`, `firebase_core`, `firebase_messaging`, `flutter_local_notifications`.
- **Notificaciones push + in-app (Fase A, 2026-04-27)**: FCM con `data.tramiteId` y `tramiteCodigo` para deep link. Misma acción persiste fila en `notificaciones` vía `ClienteTramiteEventoNotificacionService` (tipos `AVANCE`, `CIERRE`, `RECHAZO`, `POLITICA_ASIGNADA`). Portal cliente: `GET/PATCH /api/cliente/seguimiento/notificaciones` (JWT). Flutter: pantalla **Notificaciones**, ruta `/tramite/:tramiteId`, icono campana en Home, `onMessageOpenedApp` + **`getInitialMessage()`** (cold start) + cola `_pendingTramiteId` hasta restaurar sesión o post-login; tap en notificación **local** (foreground) con `payload` = `tramiteId`. Al asignar política (`TramitesService.asignarPoliticaDesdeIngreso`) también notifica. Estados finales del trámite: nodo `FIN` → `APROBADO`; nodo `RECHAZO` (nuevo tipo en políticas) → `RECHAZADO`; no se puede avanzar flujo si estado es `APROBADO`/`RECHAZADO`/`CERRADO`. `PATCH /api/seguimiento/notificaciones/{id}/leida` ahora exige que la notificación sea del usuario autenticado.
- **Flujo trámite (responsable)**: `GET/POST` bajo `/api/tramites/{id}/flujo/*` con control de rol/área; `GET .../flujo/salidas` devuelve **`FlujoSalidasResponse`**: lista de salidas + `nodoActualNombre`, `nodoActualCarrilBpmn`, `formularioExternoUrl` del nodo actual (para enlazar Google Forms u otro HTTPS). Avance simple `POST .../avanzar` con `idConexion`; paralelo en `aprobar-rama-paralela`. Angular bandeja responsable: panel **Flujo** con enlace al formulario si existe.
- **Revisión UI/UX (2026-04-27)**: sin cambios de código; hallazgos: contraste AppBar en `home_screen.dart` (texto blanco fijo), falta manejo de sesión expirada (401) en `home_screen.dart`/`mis_tramites_screen.dart`, posible cierre de `ClienteApi` y formato de fecha en detalle de trámites.

## FastAPI — Microservicio IA (`fastapi/`)

- **10 endpoints ML** bajo prefijo `/api/ml/` (vía Nginx `/fastapi/`):
  - **Recomendador políticas**: `POST train` (RandomForest con datos de Mongo), `POST predict` (top-1 política + score), `GET model-info`
  - **Cuellos botella**: `POST seed` (datos prueba), `POST analyze` (median + max/median ratio, severidad critical/warning)
  - **Asistente diseño**: `POST suggest-next` (patrones de politicas_negocio), `POST validate` (6 reglas estructurales: INICIO, FIN, huérfanos, DECISION sin ramas, ciclos)
  - **Predicción SLA**: `POST predict` (ETA por nodo, riesgo global CRITICO/ALTO/MEDIO/BAJO)
  - **Google Colab pipeline**: `POST export/dataset` (CSV con features), `POST import/model` (recibe .pkl entrenado en GPU)
- **Dominios IA**: `policy_recommendation/`, `bottlenecks/`, `policy_design/`, `sla/`, `colab/`
- **Persistencia**: MongoDB via Motor async; modelos .pkl en volumen Docker `ml-models:/data/models`
- **Contrato con Spring Boot**: comunicación HTTP interna (`FASTAPI_BASE_URL`); graceful degradation (fallback si FastAPI no responde)
- **Docker**: `depends_on mongo`, volumen `ml-models`, variables de entorno completas

## Memoria y documentación del agente

- **Consolidación completada**: se unificó el contexto técnico en `docs/ai/` y se removieron duplicados de `docsAi/ai/`.
- `docs/ai` queda como única fuente de verdad para visión, arquitectura, stack, estado, handoff, decisiones y sesiones.

## Agentes, rules y skills (VS Code)

- Se agregó el agente **`orchestrator`** para coordinar cambios cross-stack, dividir trabajo por fases y delegar a especialistas (`architect-planner`, `backend-springboot`, `frontend-angular`, `ia-fastapi`, `mongodb-modeler`, `devops-docker`).
- Se mejoraron los 6 subagentes existentes con:
  - descripciones orientadas a intención (`Use when`) para mejor invocación,
  - reglas explícitas de coordinación entre servicios,
  - entregables mínimos esperados para trazabilidad técnica.
- La configuracion de agentes se consolidó en formato nativo de VS Code en `.github/agents/*.agent.md`.
- Las rules de Cursor se migraron a instrucciones de VS Code en `.github/instructions/*.instructions.md`.
- Las skills se consolidaron en `.agents/skills/` (13 skills activas, incluyendo las migradas desde Cursor).
- Se eliminó la carpeta `.cursor/` (agents/rules/skills) para evitar duplicidad y drift de configuracion.
