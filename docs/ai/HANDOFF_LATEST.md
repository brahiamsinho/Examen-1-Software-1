# Handoff reciente

## Último cambio relevante (UI/UX BPMN + carriles por departamento)

- Se mejoró el modelador `disenador-politicas-modelado` para acercarlo a ejemplos BPMN por departamentos:
  - nuevo botón en toolbar: **Carriles por departamento**,
  - sincronización de lanes según áreas activas,
  - creación automática de colaboración/participante si el diagrama no lo tenía.
- Al aplicar propiedades de negocio, `carrilBpmn` ahora puede derivarse del área seleccionada y el nodo se reposiciona automáticamente dentro del lane de ese departamento.
- Se renovó el aspecto visual del toolbar, panel de propiedades y canvas (bordes, gradientes suaves, jerarquía visual) para una experiencia más clara.

## Estado actual de incidencia (BPMN canvas en blanco)

- **Síntoma reportado por usuario**: el área del diagrama aparece en blanco y los artefactos no se pueden seleccionar.
- **Fixes ya aplicados en frontend**:
  - eliminación de `keyboard.bindTo` (error runtime de `diagram-js`),
  - generación de BPMN-DI al adaptar política a XML,
  - `fit-viewport` después de importar XML,
  - fallback a diagrama base cuando el XML de la política es inválido,
  - estilos base `diagram-js`/BPMN movidos a `src/styles.scss` (scope global),
  - banner funcional con warnings de `importXML` + `canvas.resized()` en doble pasada.
- **Lectura técnica actual**: ya no se trata solo de configuración de teclado; el foco pasa a inspección directa del XML que llega al modeler y/o del estado del contenedor visual en runtime.
- **Siguiente paso inmediato antes de más features**: instrumentar diagnóstico visible en UI (banner con mensaje exacto de `importXML`) y verificar en caliente qué XML se está intentando renderizar.

## Último cambio relevante (BPMN frontend completo + `bpmnXml`)

- Frontend reemplazó X6 por **bpmn-js** en `disenador-politicas-modelado`: canvas BPMN + toolbar + panel de propiedades + validación.
- Nuevo adaptador `bpmn-policy-adapter.ts`: `policyToBpmnXml` y `bpmnXmlToPolicyUpsertBody` para mantener compatibilidad con `nodos`/`conexiones`.
- Backend políticas incorpora **`bpmnXml`** en documento/DTO/request y en snapshots de `politicas_negocio_revisiones`.
- `GET /api/tramites/{id}/flujo/salidas` mantiene `FlujoSalidasResponse` con metadatos de nodo y enlace de formulario.

## Último cambio relevante (IA/ML — 2026-04-27)

- **FastAPI — 10 endpoints ML** (policy recommendation, bottleneck detection, design assistant, SLA prediction, Colab pipeline)
- **Spring Boot — 3 endpoints IA proxy** (sugerir-politica, registrar-decision-ia, cuellos-botella) + `FastApiMlClient`
- **Angular — Dashboard cuellos botella** (`CuellosBotellaDashboardComponent` integrado en dashboard)
- **Docker — fastapi**: depends_on mongo, volume ml-models, modelo_dir=/data/models
- **Arquitectura IA**: desacoplada (FastAPI ML → Spring Boot proxy → Angular UI). Predicción asistida (humano confirma). Graceful degradation si FastAPI cae.
- **Archivos clave IA**: ver `docs/ai/CURRENT_STATE.md` sección FastAPI

## Último cambio relevante (Flutter: cold start push + cola tramiteId)

- `getInitialMessage` vía `FirebaseConfig.deliverColdStartTramiteDeepLinkIfAny()` (tras registrar handler en `main.dart`).
- `_pendingTramiteId` hasta sesión restaurada o login; drenaje en post-frame tras `_loadSession` / `_onLoggedIn`.
- Notificación local (foreground): `payload` = `tramiteId` + tap → mismo deep link.

## Último cambio relevante (Fase A: cierre flujo + notificaciones cliente)

- **Dominio**: nodo `FIN` → trámite `APROBADO`; nodo `RECHAZO` → `RECHAZADO` (`PoliticaNodoTerminalResolver`; tipo `RECHAZO` en políticas + toolbar Angular).
- **Backend**: `ClienteTramiteEventoNotificacionService` (FCM + Mongo `notificaciones`); flujo bloqueado si `APROBADO`/`RECHAZADO`/`CERRADO`; notificación al asignar política; `ClienteSeguimientoController` `/api/cliente/seguimiento/notificaciones`; FCM incluye `data.tramiteId`; `PATCH .../leida` valida dueño.
- **Mobile**: pantalla notificaciones, detalle `/tramite/:id`, campana en Home, deep link `onMessageOpenedApp`.

## Último cambio relevante (Flutter físico + Docker: URL API)

- **Problema**: login desde teléfono (debug inalámbrico) mostraba “Sin conexión…”: el default `10.0.2.2:8080` solo aplica al **emulador** Android; además Compose **no** publicaba el puerto 8080 del `backend` al host.
- **Cambios**:
  - `docker-compose.yml`: `ports: "${BACKEND_HOST_PORT:-8080}:8080"` en servicio `backend`.
  - Raíz `.env.example`: variable `BACKEND_HOST_PORT=8080` documentada.
  - `mobile/tramites_cliente/lib/core/api_config.dart`: default por `defaultTargetPlatform` (Android → `10.0.2.2`, iOS → `127.0.0.1`).
  - `login_screen.dart`: en `kDebugMode`, el error de red muestra `kApiBaseUrl` y recordatorio de `.env` + IP LAN.
  - `mobile/.../.env.example` y `README.md`: aclaración Docker + físico.

## Último cambio relevante (test integración lock legacy)

- Nuevo `PoliticasDominioServiceLegacyLockMongoIT` (`@DataMongoTest` + Testcontainers): inserta en `politicas_negocio` un documento **sin** clave `lockVersion`, invoca `PoliticasDominioService.reemplazar` con cuerpo válido y `lockVersion: 0`, comprueba respuesta/BD con lock en `1`. Complementa el hotfix de `inicializarLockVersionLegacySiHaceFalta` sin tocar mobile.

## Último cambio relevante (hotfix persistencia políticas)

- **Problema**: en modelador de políticas, `Guardar grafo completo` disparaba `PUT /api/politicas/{id}` con `500`, por lo que al recargar (`F5`) parecían “perderse” cambios.
- **Causa técnica**: documentos legacy en `politicas_negocio` sin campo `lockVersion` (introducido luego con `@Version`) rompían el flujo de actualización.
- **Fix aplicado**:
  - `backend/.../PoliticasDominioService.java`: nuevo paso `inicializarLockVersionLegacySiHaceFalta` que setea `lockVersion=0` en Mongo cuando falta, antes de `save`.
  - `backend/.../GlobalExceptionHandler.java`: `DuplicateKeyException` mapeado a `409` para evitar `500` opacos.
- **Impacto UX**: el usuario vuelve a poder guardar y recargar sin perder cambios del nodo (área/responsable/nombre) por falla del backend.

## Último cambio relevante (rules + skills + limpieza)

- Se migraron las rules de Cursor a instrucciones de VS Code en `.github/instructions/`:
  - `00-project-foundation.instructions.md`
  - `10-backend-modular-springboot.instructions.md`
  - `20-mongodb-persistence.instructions.md`
  - `30-fastapi-microservice.instructions.md`
  - `40-frontend-angular.instructions.md`
  - `50-devops-docker-nginx.instructions.md`
- Se migraron y consolidaron skills de `.cursor/skills/` en `.agents/skills/`, quedando 13 skills en total.
- Se corrigieron referencias legacy en skills:
  - `saving-workspace-context` ahora apunta a `.github/instructions/` y `.agents/skills/`.
  - `ui-ux-pro-max` ahora incluye frontmatter valido y rutas de script en `.agents/skills/ui-ux-pro-max/`.
- Se eliminó `.cursor/` (agents/rules/skills) porque ya no se usa en VS Code.

## Último cambio relevante (migración a VS Code)

- Se adaptaron los agentes definidos inicialmente para Cursor a formato nativo de VS Code en `.github/agents/` con extension `.agent.md`.
- Se publicaron 7 agentes: `orchestrator`, `architect-planner`, `backend-springboot`, `frontend-angular`, `ia-fastapi`, `mongodb-modeler`, `devops-docker`.
- Se ajusto frontmatter para compatibilidad con Copilot Chat custom agents:
  - `tools` con aliases validos de VS Code (`execute`, `read`, `edit`, `search`, `agent`, `todo`),
  - `argument-hint` para guiar invocacion,
  - `agents` en `orchestrator` para delegacion controlada.
- `.cursor/agents` se mantiene temporalmente para no romper flujo previo.

## Último cambio relevante (agentes)

- Se incorporó `.cursor/agents/orchestrator.md` como capa de coordinación técnica para tareas multi-servicio.
- Se reforzaron los agentes existentes de `.cursor/agents/` (`architect-planner`, `backend-springboot`, `frontend-angular`, `ia-fastapi`, `mongodb-modeler`, `devops-docker`) con:
  - descripciones por intención (`Use when`) para mejorar descubribilidad,
  - reglas de delegación y coordinación,
  - entregables esperados por especialidad.
- Objetivo del ajuste: reducir solapamientos entre agentes especialistas y mejorar continuidad/handoff entre sesiones.

## Último cambio relevante (documentación)

- Se consolidó la memoria entre `docs/` y `docsAi/`: archivos base (`PROJECT_VISION`, `ARCHITECTURE`, `TECH_STACK`) migrados a `docs/ai/`, `README` actualizado y duplicados desactualizados eliminados de `docsAi/ai/`.

## Último cambio relevante (backend Spring Boot)

- Dominio Mongo alineado a **script.db**: CRUD políticas (`politicas_negocio` con nodos/conexiones embebidos), trámites + cola FIFO + recorridos (`recorridos_tramite` en módulo **tramites**), áreas (`areas`), documentos/formularios, notificaciones + bitácora de negocio (`bitacora`, distinta de `bitacora_auditoria` del admin), analítica/recomendaciones estructurales.
- **Infra**: `GET /api/seguridad/infra` comprueba Mongo (lectura) y Redis (PING).

## Último cambio relevante (mobile Flutter)

- **Login portal cliente**: UI minimalista; integración existente con Spring `POST /api/auth/login` y campos homónimos a `usuarios` en `script.db` (`correo`, contrasena hasheada en servidor). Tema global con inputs subrayados.
- **Nuevo flujo de ingreso**: cliente sube documento sin seleccionar política; mensaje de confirmación indica ingreso en atención al cliente.
- **Seguimiento**: `GET /api/cliente/tramites` + `GET /api/cliente/tramites/{id}` (detalle + recorridos); ruta app **Mis trámites** (`/mis-tramites`).
- **Persistencia de sesión (2026-04-27)**: se reemplazó la variable global `_token` por `flutter_secure_storage`. `SecureStorage` en `core/secure_storage.dart` cifra token + datos de usuario (nombres, apellidos, correo, rol). `main.dart` ahora es `StatefulWidget` que carga sesión al iniciar y redirige automáticamente. `LoginResult` captura todos los campos del backend (`apellidos`, `correo`, `expiresInSeconds`).
- **Pantalla de perfil (2026-04-27)**: nueva ruta `/perfil` con avatar (iniciales), nombre completo, correo, rol legible, botón copiar al portapapeles y logout con diálogo de confirmación. Acceso desde ícono de persona en AppBar de Home.
- **Revisión UI/UX (2026-04-27)**: análisis sin cambios de código. Issues: contraste AppBar por texto blanco fijo en `home_screen.dart`, falta manejo explícito de expiración de sesión (401) en `home_screen.dart`/`mis_tramites_screen.dart`. Minor: cierre de `ClienteApi` y formato de fecha en detalle.

## Último cambio relevante (planificador + flujo paralelo)

- **Rol** `PLANIFICADOR` en `PortalRol`, semilla `planificador@tramites.local`, seguridad `/api/planificador/**`.
- **API**: `GET /api/planificador/tramites/pendientes-politica`, `POST /api/planificador/tramites/{id}/asignar-politica`; `GET/POST` bajo `/api/tramites/{id}/flujo/...` para salidas del grafo, **`POST .../flujo/avanzar`** (avance por `idConexion` sin paralelo múltiple) y aprobación de ramas `PARALELO` (join por sucesor común directo). Flujo restringido a **admin** o **responsable** del área del nodo/rama.

## Último cambio relevante (seguridad trámites / recorridos)

- **`TramiteFlujoAutorizacionService`**: reglas compartidas entre `TramiteFlujoService` y `TramitesService` (consulta de expediente, alta de recorrido vía REST, listado/cola).
- **`GET /api/tramites`**, **`GET .../cola/fifo`**: admin todo; planificador solo sin `politicaId`; responsable solo `areaActualId` de su usuario.
- **`GET /api/tramites/{id}`**, **`GET/POST .../recorridos`**: visibilidad staff (planificador solo sin política; responsable por `areaActualId` o rama paralela pendiente de su área); `POST` recorridos con política exige mismo criterio de nodo/área que el flujo; sin política, solo admin. Cliente sigue en `/api/cliente/tramites/**` con `listarRecorridosInterno` tras `obtenerDeCliente`.
- **Angular**: portal `/acceso/planificador`, módulo lazy `/planificador/pendientes` con tabla y asignación de política.

## Último cambio relevante (frontend Angular)

- **Login por portal** (`/acceso/politicas`): lectura robusta de `portalRol` desde el árbol de rutas; bloqueo con mensaje claro si falta; pistas de credenciales de **semilla dev** solo cuando no es producción.
- **Diseñador de políticas / modelado**: editor **AntV X6** en `/disenador/modelado`; REST `GET/PUT /api/politicas` con **`lockVersion`** en cuerpo/respuesta; **WebSocket** colaborativo (`PoliticasCollaborationService` → `ws(s)://host/backend/ws/politicas?access_token=…`) con presencia y sync debounced del grafo entre diseñador y administrador. **Deshacer/Rehacer** con `@antv/x6-plugin-history` (botones + Ctrl+Z / Ctrl+Y / Ctrl+Mayús+Z); al cargar política o al aplicar `GRAPH_UPDATE` remoto se **limpia** el historial para no mezclar con colaboración.
- **Normalización de IDs en modelado**: se añadió `asStringId` en mapper X6 + serialización de upsert + flujo del inspector para bloquear la fuga de `ObjectId` como objeto en `areaId` (`GET /api/seguridad/usuarios?areaId=[object Object]`), causa de pérdida de asignaciones tras recargar.
- **Refuerzo de normalización (regresión `[object Object]`)**: helpers de ID ampliados en `politicas-api.service.ts`, `areas-api.service.ts`, `usuarios-area-api.service.ts`, `politica-x6-mapper.ts` y `politica-negocio.model.ts` para soportar variantes `ObjectId` y evitar fallback inválido.
- **Prueba de regresión frontend**: nuevo spec `frontend/src/app/features/disenador-politicas/models/politica-negocio.model.spec.ts` para validar `ObjectId-like -> string` en `politicaDtoToUpsertBody`.

## Último cambio relevante (backend seguridad/auth)

- `RestAuthenticationEntryPoint` ahora usa `ObjectMapper` inyectado por Spring en lugar de instanciar uno manualmente; con esto los `ErrorResponse` con `Instant` se serializan bien en respuestas 401.

## Último cambio relevante (backend políticas / serialización IDs)

- Se reemplazó la salida de embeddables crudos en `PoliticaNegocioResponse` por DTOs de respuesta con IDs en `string`:
  - `nodos[].areaId`
  - `nodos[].asignacionesResponsable[].usuarioId`
  - `nodos[].asignacionesResponsable[].areaId`
- Archivo clave: `PoliticasDominioService#toResponse` ahora mapea `ObjectId -> toHexString()`.
- Resultado: al recargar `modelado`, Angular ya recibe IDs estables y conserva correctamente área/responsable en inspector.
- Se añadió prueba de regresión `frontend/src/app/features/disenador-politicas/utils/politica-x6-mapper.spec.ts` para blindar normalización en mapper X6 (entrada/salida de grafo).
- Confirmación funcional: usuario validó en UI que ahora sí persiste asignación de nodo tras recarga.

## Último cambio relevante (backend flujo condicional/paralelo)

- Nuevo componente `TramiteFlujoCondicionEvaluator` (módulo `tramites`) con operadores soportados:
  - `campo == valor`
  - `campo != valor`
  - `campo in [a,b,c]`
  - `campo exists`
- `TramiteFlujoService.avanzar` filtra conexiones por condición y falla con `SIN_SALIDA_VALIDA` si no hay transición habilitada para estado/contexto actual.
- `TramiteFlujoService.aprobarRamaParalela` calcula convergencia de ramas con BFS multi-hop (ya no solo sucesor común directo).
- Contexto de evaluación incluye:
  - atributos de `tramite` (`estado`, `prioridad`, etc.),
  - alias `tramite.*`,
  - y campos básicos del último formulario (`form.*`) cuando existe.
- Test agregado:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoCondicionEvaluatorTest.java`
- Tests de servicio agregados para avance condicional:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoServiceCondicionTest.java`
  - Cubre: condición cumple (avance), ninguna condición cumple (`SIN_SALIDA_VALIDA`), condición inválida (`CONDICION_INVALIDA`).
- Tests de servicio agregados para paralelo BFS:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoServiceParaleloBfsTest.java`
  - Cubre:
    - convergencia multi-hop cuando todas las ramas son aprobadas,
    - error `CONVERGENCIA_NO_ENCONTRADA` cuando no existe intersección.
- Tests de servicio agregados para seguridad/roles en flujo:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoAutorizacionServiceTest.java`
  - Cubre:
    - bypass admin en validación de nodo,
    - bloqueo responsable por área distinta,
    - visibilidad por rama paralela pendiente,
    - ajuste automático de `areaId` al registrar recorrido en API de trámites.
- Tests de integración HTTP (E2E-lite) agregados:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/controller/TramiteFlujoControllerIntegrationTest.java`
  - Cubre endpoints:
    - `POST /api/tramites/{tramiteId}/flujo/avanzar`
    - `POST /api/tramites/{tramiteId}/flujo/aprobar-rama-paralela`
  - Escenarios:
    - condición que cumple -> `200`,
    - condición que no cumple -> `422` con `SIN_SALIDA_VALIDA`.
    - responsable con área incorrecta -> `403`.
    - planificador en `avanzar` -> `403`.
    - sin token / token inválido -> `401`.
    - condición inválida -> `400` con `CONDICION_INVALIDA`.
    - paralelo multi-hop converge a join -> `200`.
    - paralelo sin convergencia -> `422` con `CONVERGENCIA_NO_ENCONTRADA`.
    - idempotencia rama paralela repetida -> `200` sin mover `nodoActualId` prematuramente.
- Tests de integración con repos reales (Mongo) agregados:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoServiceMongoIntegrationTest.java`
  - Base técnica:
    - `@DataMongoTest` + `Testcontainers` (`MongoDBContainer`).
    - repos reales `TramiteRepository`, `PoliticaNegocioRepository`, `FormularioTramiteRepository`.
    - mocks acotados a dependencias laterales (`TramiteFlujoAutorizacionService`, `TramitesService`).
  - Escenarios:
    - `avanzar` persiste transición real en Mongo (`nodoActualId`, `areaActualId`),
    - paralelo multi-hop persiste join final y limpia estado paralelo (`split/join/pendientes/aprobadas`).
- Fase 9 - ampliación de casos borde sobre Mongo real:
  - `aprobarRamaParalela_ramaRepetida_esIdempotenteEnMongo`:
    - repetir aprobación de la misma rama mantiene estado consistente (sin duplicado de aprobadas, sin avance prematuro).
  - `aprobarRamaParalela_sinConvergencia_lanza422`:
    - política paralela sin intersección valida error funcional `CONVERGENCIA_NO_ENCONTRADA`.
  - `avanzar_conCondicionPorFormulario_formTipo_persisteDestinoCorrecto`:
    - usa datos reales de `formularios_tramite` (`form.tipo`) para decidir transición.
  - Impacto UX:
    - evita comportamientos inconsistentes ante reintentos de usuario,
    - devuelve errores de negocio claros cuando la política está mal modelada,
    - alinea decisiones de flujo con datos del formulario efectivamente capturados.
- Fase 10 - endurecimiento mini-lenguaje `condicion`:
  - Archivo: `backend/src/main/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoCondicionEvaluator.java`
  - Cambios:
    - patrones de parseo estrictos (`==`, `!=`, `in [A,B]`, `exists`);
    - validación de identificador de campo (letras/números/underscore/punto);
    - `in []` inválido;
    - valor vacío inválido para `==` y `!=`;
    - mensajes de error con ejemplos concretos para planificador/diseñador.
  - Tests ampliados:
    - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoCondicionEvaluatorTest.java`
    - cobertura de campo inválido, valor vacío y lista `in` vacía.
  - Impacto UX:
    - el usuario recibe feedback más claro al guardar/configurar condiciones inválidas;
    - se reduce el “guardó bien pero luego no avanza” por sintaxis ambigua.
- Fase 11 - contrato HTTP de error guiado:
  - Archivo: `backend/src/test/java/com/plataforma/tramites/modules/tramites/controller/TramiteFlujoControllerIntegrationTest.java`
  - Nuevos escenarios:
    - `avanzar_whenCondicionCampoInvalido_returns400WithGuidedMessage`,
    - `avanzar_whenCondicionInListEmpty_returns400WithGuidedMessage`.
  - Ajuste adicional:
    - `avanzar_whenCondicionInvalida_returns400` ahora valida que el mensaje incluya "Operadores soportados" y "Ejemplos".
  - Impacto UX:
    - el frontend puede mostrar mensajes consistentes y accionables al usuario final cuando la condición está mal definida;
    - reduce ciclos de soporte por errores ambiguos de configuración.
- Fase 12 - fix de build en Docker Compose:
  - Problema: `docker compose up -d --build` fallaba en backend con:
    - `package org.testcontainers.junit.jupiter does not exist`
    - símbolos `@Testcontainers` / `@Container` no encontrados en `TramiteFlujoServiceMongoIntegrationTest`.
  - Causa raíz: `mvn clean package -DskipTests` igual compila tests (`testCompile`), y faltaba dependencia de anotaciones junit para Testcontainers.
  - Solución aplicada:
    - `backend/pom.xml`: agregar `org.testcontainers:junit-jupiter` en `scope test`.
  - Validación:
    - `./mvnw -q clean package -DskipTests` en verde.

## Último cambio relevante (FastAPI / Docker)

- Se corrigió incompatibilidad de dependencias con `python:3.12-slim` que impedía levantar `fastapi`:
  - `scikit-learn` 1.3.3 -> 1.4.2
  - `pandas` 2.0.3 -> 2.2.2
  - `joblib` 1.2.0 -> 1.4.2 (evita `ModuleNotFoundError: distutils`)
- Resultado: `docker compose ps` con servicios `backend`, `fastapi`, `frontend`, `mongo`, `redis` en estado healthy.

## Último cambio relevante (WebSocket + Nginx)

- Spring: `spring-boot-starter-websocket`, handler en `modules/politicas/collab/`, handshake JWT por query; salas en memoria por `politicaId`.
- Nginx `location /backend/`: cabeceras `Upgrade` / `Connection` y timeouts largos; `proxy.conf.json` del Angular con `"ws": true` para `ng serve`.

## Último cambio relevante (push notifications FCM)

- **Firebase project**: `transporte-si2`, package `com.example.tramites_cliente`.
- **Mobile**: `firebase_core` + `firebase_messaging` + `flutter_local_notifications`. `FirebaseConfig` inicializa Firebase, maneja permisos y muestra notificaciones locales en foreground. Al login se envía FCM token al backend.
- **Backend**: `firebase-admin` SDK en `pom.xml`. Módulo `seguimiento` con `FcmTokenDocument`, `FcmTokenRepository`, `FcmNotificationService`. `FirebaseConfiguration` init desde `app.firebase.credentials-path`.
- **Endpoint**: `POST /api/seguimiento/notificaciones/fcm-token` (auth required).
- **Trigger**: `TramiteFlujoService` envía notificación al cliente cuando `avanzar()` o join paralelo completado.
- **Docker**: `FIREBASE_CREDENTIALS_PATH` env var + volume mount para credenciales.

### Qué tocar a continuación

1. Mobile: deep linking en notificaciones → abrir detalle del trámite.
2. Backend: notificación push cuando planificador asigna política.
3. Backend: notificación push cuando trámite llega a estado final.
4. Mobile: pantalla de notificaciones con mark-as-read.

### Archivos guía

- `backend/src/main/java/com/plataforma/tramites/modules/politicas/` (incl. `collab/` WebSocket)
- `backend/src/main/java/com/plataforma/tramites/modules/tramites/`
- `backend/src/main/resources/application.yml`
- `frontend/src/app/features/disenador-politicas/modelado/disenador-politicas-modelado.component.ts`
- `frontend/proxy.conf.json`
- `frontend/src/app/features/disenador-politicas/data/politicas-collaboration.service.ts`
- `nginx/nginx.conf`
