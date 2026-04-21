# Sesión 2026-04-20 — Backend dominio alineado a script.db

## Respuesta a “¿está todo implementado?”

**Antes:** existía arquitectura modular, `/health`, Mongo/Redis en configuración, auth JWT, módulo **admin** (CRUD usuarios/roles/permisos + bitácora de auditoría en `bitacora_auditoria`), y stubs `*/status` en políticas/trámites/documentos/seguimiento/analítica.

**Ahora:** se completó una **primera capa funcional mínima** por dominio según `script.db` (colecciones y enums validados en servicio donde aplica), sin absorber lógica en FastAPI y sin ML real.

## Cambios destacados

- **políticas**: `PoliticaNegocioDocument` con nodos/conexiones/asignaciones embebidos; CRUD REST bajo `/api/politicas` (`PoliticasDominioService`).
- **trámites**: CRUD/col FIFO/recorridos; `recorridos_tramite` movido desde `seguimiento` a **`modules/tramites`** (misma colección).
- **seguridad**: áreas (`areas`) CRUD `/api/seguridad/areas`; diagnóstico Mongo+Redis `GET /api/seguridad/infra`.
- **documentos**: formularios + archivos por trámite bajo `/api/documentos/...`.
- **seguimiento**: `notificaciones` + **`bitacora`** de dominio (distinta de `bitacora_auditoria` del admin).
- **analítica**: listado/creación estructural de `analisis_rendimiento` y `recomendaciones_politica`.

## Pendiente siguiente fase

- Reglas de autorización por actor (DISENADOR, RESPONSABLE_AREA, CLIENTE) por endpoint.
- Reglas de negocio avanzadas de trámites (transiciones de estado, cierre de recorrido, integración BPMN↔nodos).
- Integración FastAPI solo como cliente (WebClient) para sugerencias cuando exista contrato.
- Tests de integración Mongo (Testcontainers) y contratos API.
