# Sesión 2026-04-27 — regresión mapper X6 + verificación logs

## Objetivo

Ejecutar el plan de cierre del bug de persistencia visual: blindaje por pruebas en frontend y verificación operativa de logs.

## Implementación

- Se creó `frontend/src/app/features/disenador-politicas/utils/politica-x6-mapper.spec.ts` con cobertura de:
  - `buildCellsFromPolitica` (normalización de IDs ObjectId-like)
  - `graphToPoliticaNegocio` (serialización de IDs al guardar grafo)
- Se mantuvo el test previo de serializer en:
  - `frontend/src/app/features/disenador-politicas/models/politica-negocio.model.spec.ts`

## Verificación operativa

- Revisión de logs `nginx/backend`:
  - Requests `GET /backend/api/seguridad/usuarios?areaId=...` en 200.
  - Requests `PUT /backend/api/politicas/{id}` en 200.
  - Sin evidencia de `areaId=[object Object]`.
  - Sin evidencia de `InvalidDefinitionException` en la ventana revisada.

## Estado

- Bug principal en estado **corregido técnicamente** (contrato backend + normalización frontend + regresiones).
- Pendiente solo validación visual final del usuario en su flujo manual completo.
