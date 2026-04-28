# Sesión 2026-04-27 — fix ObjectId UI + Jackson Instant

## Objetivo

Resolver la regresión visual del diseñador de políticas (asignaciones que “desaparecen” tras recargar) y corregir el error backend de serialización `Instant` en respuestas de autenticación.

## Cambios aplicados

### Frontend (`disenador-politicas`)

- Refuerzo de normalización de IDs para evitar `areaId=[object Object]`.
- Archivos actualizados:
  - `frontend/src/app/features/disenador-politicas/modelado/disenador-politicas-modelado.component.ts`
  - `frontend/src/app/features/disenador-politicas/data/politicas-api.service.ts`
  - `frontend/src/app/features/disenador-politicas/data/areas-api.service.ts`
  - `frontend/src/app/features/disenador-politicas/data/usuarios-area-api.service.ts`
  - `frontend/src/app/features/disenador-politicas/utils/politica-x6-mapper.ts`
  - `frontend/src/app/features/disenador-politicas/models/politica-negocio.model.ts`
- El helper ahora contempla variantes comunes de `ObjectId`: `$oid`, `oid`, `hexString`, `id`, `_id`, `toHexString`, `toString`, y descarta explícitamente el fallback inválido `"[object Object]"`.

### Prueba de regresión

- Nuevo test:
  - `frontend/src/app/features/disenador-politicas/models/politica-negocio.model.spec.ts`
- Valida que `politicaDtoToUpsertBody` convierta `ObjectId-like` a string antes del `PUT`.

### Backend (`seguridad/auth`)

- Archivo actualizado:
  - `backend/src/main/java/com/plataforma/tramites/modules/seguridad/config/RestAuthenticationEntryPoint.java`
- Se eliminó `new ObjectMapper()` manual y se usa `ObjectMapper` inyectado por Spring para serializar `ErrorResponse` con `Instant` en 401.

## Resultado esperado

- Ya no deberían emitirse requests con `areaId=[object Object]` al cargar o editar nodos del diseñador.
- Los errores 401 deben volver en JSON válido (sin `InvalidDefinitionException` por `Instant`).

## Pendiente inmediato

- Verificación E2E en navegador/containers del flujo: editar nodo → aplicar → guardar grafo → recargar → mantener área/responsable visibles.
