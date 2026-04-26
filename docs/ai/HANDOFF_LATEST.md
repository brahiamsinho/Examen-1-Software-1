# Handoff reciente

## Último cambio relevante (backend Spring Boot)

- Dominio Mongo alineado a **script.db**: CRUD políticas (`politicas_negocio` con nodos/conexiones embebidos), trámites + cola FIFO + recorridos (`recorridos_tramite` en módulo **tramites**), áreas (`areas`), documentos/formularios, notificaciones + bitácora de negocio (`bitacora`, distinta de `bitacora_auditoria` del admin), analítica/recomendaciones estructurales.
- **Infra**: `GET /api/seguridad/infra` comprueba Mongo (lectura) y Redis (PING).

## Último cambio relevante (mobile Flutter)

- **Login portal cliente**: UI minimalista; integración existente con Spring `POST /api/auth/login` y campos homónimos a `usuarios` en `script.db` (`correo`, contrasena hasheada en servidor). Tema global con inputs subrayados.
- **Nuevo flujo de ingreso**: cliente sube documento sin seleccionar política; mensaje de confirmación indica ingreso en atención al cliente.

## Último cambio relevante (planificador + flujo paralelo)

- **Rol** `PLANIFICADOR` en `PortalRol`, semilla `planificador@tramites.local`, seguridad `/api/planificador/**`.
- **API**: `GET /api/planificador/tramites/pendientes-politica`, `POST /api/planificador/tramites/{id}/asignar-politica`; `GET/POST` bajo `/api/tramites/{id}/flujo/...` para salidas del grafo y aprobación de ramas `PARALELO` (join por sucesor común directo).
- **Angular**: portal `/acceso/planificador`, módulo lazy `/planificador/pendientes` con tabla y asignación de política.

## Último cambio relevante (frontend Angular)

- **Login por portal** (`/acceso/politicas`): lectura robusta de `portalRol` desde el árbol de rutas; bloqueo con mensaje claro si falta; pistas de credenciales de **semilla dev** solo cuando no es producción.
- **Diseñador de políticas / modelado**: editor **AntV X6** en `/disenador/modelado`; REST `GET/PUT /api/politicas` con **`lockVersion`** en cuerpo/respuesta; **WebSocket** colaborativo (`PoliticasCollaborationService` → `ws(s)://host/backend/ws/politicas?access_token=…`) con presencia y sync debounced del grafo entre diseñador y administrador. **Deshacer/Rehacer** con `@antv/x6-plugin-history` (botones + Ctrl+Z / Ctrl+Y / Ctrl+Mayús+Z); al cargar política o al aplicar `GRAPH_UPDATE` remoto se **limpia** el historial para no mezclar con colaboración.

## Último cambio relevante (WebSocket + Nginx)

- Spring: `spring-boot-starter-websocket`, handler en `modules/politicas/collab/`, handshake JWT por query; salas en memoria por `politicaId`.
- Nginx `location /backend/`: cabeceras `Upgrade` / `Connection` y timeouts largos; `proxy.conf.json` del Angular con `"ws": true` para `ng serve`.

### Qué tocar a continuación

1. Backend: evaluar `condicion` en conexiones al avanzar de nodo (motor de reglas o integración con datos del trámite).
2. Backend: join paralelo más allá del sucesor común directo (multi-hop o nodo sync explícito).
3. Pruebas integración Mongo / transiciones de estado de trámite y flujo paralelo.

### Archivos guía

- `backend/src/main/java/com/plataforma/tramites/modules/politicas/` (incl. `collab/` WebSocket)
- `backend/src/main/java/com/plataforma/tramites/modules/tramites/`
- `backend/src/main/resources/application.yml`
- `frontend/src/app/features/disenador-politicas/modelado/disenador-politicas-modelado.component.ts`
- `frontend/proxy.conf.json`
- `frontend/src/app/features/disenador-politicas/data/politicas-collaboration.service.ts`
- `nginx/nginx.conf`
