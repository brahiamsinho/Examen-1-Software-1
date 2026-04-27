# Registro de decisiones técnicas

| Fecha | Decisión | Contexto |
|-------|----------|----------|
| 2026-04-20 | Rutas lazy para shell y dashboard | Reduce el bundle inicial y deja un patrón claro para nuevas features. |
| 2026-04-20 | Prefijos `/backend` y `/fastapi` en `environment` | Alineado con `nginx.conf` y Docker Compose; sin host fijo en el cliente. |
| 2026-04-20 | Health Spring en `GET .../health` | Coincide con `HealthController` de Spring Boot (`/health`, no `/api/health`). |
| 2026-04-20 | `proxy.conf.json` en `ng serve` | Permite desarrollo local del SPA contra Spring/FastAPI sin depender solo del stack Docker. |
| 2026-04-20 | APIs bajo `/api/...` sin segmento `/v1` | Spring (`@RequestMapping("/api/...")`), FastAPI (`API_PREFIX=/api`), Angular y Flutter actualizados; menos confusión con versionado no usado. |
| 2026-04-20 | Modelado de políticas con **bpmn-js** (BPMN 2.0), no Mermaid | BPMN es el estándar de negocio/proceso; encaja con exportación XML y futura persistencia; Mermaid no cubre semántica BPMN ni interoperabilidad típica. |
| 2026-04-20 | `portalRol` leído fusionando `ActivatedRoute` raíz→hoja | Evita fallos de login si el `data` del portal no está solo en el snapshot local del componente. |
| 2026-04-20 | Dos bitácoras: `bitacora_auditoria` (admin) vs `bitacora` (dominio script.db) | La auditoría de pantallas admin no mezcla con la bitácora de negocio de trámites/políticas del esquema Mongo principal. |
| 2026-04-20 | `recorridos_tramite` vive en el módulo **tramites** | Cohesión del agregado trámite + recorrido; el módulo seguimiento cubre notificaciones y bitácora de dominio. |
| 2026-04-21 | Login móvil cliente sin credenciales por defecto + UI minimalista | Reduce riesgo de dejar datos demo en builds; la semilla dev se documenta solo en modo debug apuntando al backend. |
| 2026-04-22 | Colaboración en vivo del **modelado de políticas** vía **WebSocket** (`/ws/politicas`) | Salas por `politicaId` en Spring; JWT en query `access_token` (limitación del API WebSocket del navegador); presencia + broadcast JSON de celdas X6 con `revision` por sala; Nginx con `Upgrade`/`Connection` y proxy Angular `ws: true`. |
| 2026-04-22 | **Optimistic locking** en `PUT /api/politicas/{id}` con campo Mongo **`lockVersion`** (`@Version`) | Independiente de `version` de negocio; el cliente envía `lockVersion` en el cuerpo; 409 si hubo guardado concurrente (`ApiException` o `OptimisticLockingFailureException`). |
| 2026-04-22 | Semilla dev: **segundo usuario** `politicas2@tramites.local` con rol `DISENADOR_POLITICAS` (misma `demo123` que el primero) | Permite abrir dos sesiones (p. ej. ventana normal + incógnito) y validar colaboración en tiempo real en el modelado. |
| 2026-04-25 | Ingreso móvil del cliente desacoplado de política de negocio | El cliente solo sube documento y el trámite entra a `ATENCION_CLIENTE`; la política se asigna después por planificación. Se removió selección de políticas en mobile y endpoint `/api/cliente/politicas`. |
| 2026-04-26 | Rol `PLANIFICADOR` + API y UI de asignación de política | Endpoints `/api/planificador/tramites/pendientes-politica` y `POST .../asignar-politica`; trámite debe estar en nodo de ingreso (`app.workflow.intake-node-id`). Ramas `PARALELO`: aprobación por rama y avance al join si existe sucesor común directo desde todas las ramas. |
| 2026-04-26 | Semilla dev: más `RESPONSABLE_AREA` por departamento | Área «Tecnología de la información»; usuarios demo `legal2@`, `rrhh@`, `ti@`, `ti2@`, `atencion@` (además de `area@` y `legal@`) con `areaId` asignado vía `upsertResponsableDeArea`. |
| 2026-04-26 | Autorización centralizada staff en `/api/tramites` y recorridos | Nuevo `TramiteFlujoAutorizacionService`; listado/cola por rol; `POST /recorridos` alineado a nodo/área como el flujo; portal cliente usa métodos internos tras verificar propiedad. |
