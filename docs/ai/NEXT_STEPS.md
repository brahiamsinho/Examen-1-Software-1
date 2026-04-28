# Próximos pasos sugeridos

- **Completado (2026-04-27)**: test backend `PoliticasDominioServiceLegacyLockMongoIT` — política Mongo **sin** campo `lockVersion` → `reemplazar` con `lockVersion: 0` persiste y deja `lockVersion` en `1` (regresión hotfix modelador; no aplica a mobile).
- **Completado (Fase A flujo cliente, 2026-04-27)**: estados terminales `APROBADO`/`RECHAZADO` al llegar a nodo `FIN`/`RECHAZO`; bloqueo de avance si ya finalizó; notificaciones FCM + Mongo al avanzar, al join paralelo, al asignar política; tipo nodo `RECHAZO` en backend y diseñador; API `GET/PATCH /api/cliente/seguimiento/notificaciones`; FCM `data.tramiteId`; Flutter pantalla notificaciones + ruta `/tramite/:id` + tap desde push (`onMessageOpenedApp`).

- **Completado (2026-04-27)**: cold start FCM (`FirebaseConfig.deliverColdStartTramiteDeepLinkIfAny` + `getInitialMessage`), cola `_pendingTramiteId` en `main.dart` hasta `_loadSession` / `_onLoggedIn`, tap en notificación local con `payload` = `tramiteId`.
- **Completado (2026-04-28)**: snapshots `politicas_negocio_revisiones` + APIs de revisiones; nodos `carrilBpmn` + `formularioExternoUrl` (HTTPS); `FlujoSalidasResponse` + enlace en panel flujo responsable; `docs/ai/OPEN_SOURCE_BPMN.md`.
- **Completado (2026-04-28)**: reemplazo frontend de X6 por modelador BPMN (`bpmn-js`) + adaptador XML->dominio + `bpmnXml` en backend.
1. Diseñador Angular: mejorar panel de propiedades BPMN (nombre editable del elemento, edición de condiciones de `sequenceFlow`, reasignación bulk de carriles).
2. Diseñador Angular (prioridad crítica): validar en entorno Docker que el fix global de estilos (`diagram-js` en `src/styles.scss`) eliminó canvas en blanco y confirmar interacción de artefactos.
3. Diseñador Angular: si persiste el blanco, mostrar en UI diagnóstico de tamaño real del canvas (`offsetWidth/offsetHeight`) y snapshot corto de XML importado.
4. Diseñador Angular: agregar paleta de colores por departamento (lane color tokens) y leyenda visual para mapear áreas -> carriles.
5. Diseñador Angular: soportar edición de nombre de lane inline y persistencia explícita de lane-id <-> areaId para minimizar ambigüedad al renombrar áreas.
6. Mobile Flutter: refinar UX notificaciones (badge no leídas, formato fecha).
6. Mobile Flutter: refinar **Mis trámites** (paginación prev/next, skeletons, más campos en detalle como fechaRegistro/prioridad).
7. Mobile Flutter: restricción de tipos de archivo en file picker (solo PDF, imágenes).
8. Mobile Flutter: ajustar AppBar de Home para contraste correcto (evitar texto blanco fijo si fondo claro).
9. Mobile Flutter: manejo centralizado de sesión expirada (401) con logout/redirección.
10. Mantener cobertura de regresión frontend BPMN (adapter, validaciones, serialización de metadatos en `documentation`).
11. Backend: evaluación de condiciones en aristas (`condicion`) y avance secuencial guiado por API o tarea.
12. Mantener `docs/ai/` como única fuente canónica y registrar nuevas sesiones solo en `docs/ai/sessions/`.
13. Validar el flujo de trabajo del nuevo agente `orchestrator` con escenarios reales (cambio solo backend, cambio full-stack y ajuste de infraestructura).
14. Arquitectura (opcional): evaluar motor BPMN 2.0 embebido ([Operaton](https://operaton.org/), etc.) vs. modelo actual — ver `docs/ai/OPEN_SOURCE_BPMN.md`.
