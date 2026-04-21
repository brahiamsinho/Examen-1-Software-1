# Handoff reciente

## Último cambio relevante (backend Spring Boot)

- Dominio Mongo alineado a **script.db**: CRUD políticas (`politicas_negocio` con nodos/conexiones embebidos), trámites + cola FIFO + recorridos (`recorridos_tramite` en módulo **tramites**), áreas (`areas`), documentos/formularios, notificaciones + bitácora de negocio (`bitacora`, distinta de `bitacora_auditoria` del admin), analítica/recomendaciones estructurales.
- **Infra**: `GET /api/seguridad/infra` comprueba Mongo (lectura) y Redis (PING).

## Último cambio relevante (frontend Angular)

- **Login por portal** (`/acceso/politicas`): lectura robusta de `portalRol` desde el árbol de rutas; bloqueo con mensaje claro si falta; pistas de credenciales de **semilla dev** solo cuando no es producción.
- **Diseñador de políticas**: nueva ruta `/disenador/modelado` con editor **BPMN 2.0** (`bpmn-js` `Modeler`): diagrama nuevo, importar/exportar `.bpmn`, reinicio. Sidebar del shell enlaza “Editor BPMN”; el catálogo enlaza al editor.

### Qué tocar a continuación

1. Angular: consumir `GET/POST/PUT/DELETE /api/politicas` y listados de trámites; opcional: import BPMN → payload `PoliticaUpsertRequest`.
2. Spring: **autorización fina por actor** en endpoints de dominio (hoy: JWT autenticado genérico).
3. Pruebas integración Mongo / transiciones de estado de trámite.

### Archivos guía

- `backend/src/main/java/com/plataforma/tramites/modules/politicas/`
- `backend/src/main/java/com/plataforma/tramites/modules/tramites/`
- `backend/src/main/resources/application.yml`
- `frontend/src/app/features/disenador-politicas/modelado/disenador-politicas-modelado.component.ts`
- `frontend/proxy.conf.json`
