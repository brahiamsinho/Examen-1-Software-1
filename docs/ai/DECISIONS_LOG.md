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
