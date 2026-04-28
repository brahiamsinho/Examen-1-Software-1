# Sesión 2026-04-27 — fase 1 tests condicionales flujo

## Objetivo

Ejecutar fase 1 aprobada: cubrir comportamiento condicional de `TramiteFlujoService` en avance.

## Implementación

- Nuevo test:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoServiceCondicionTest.java`
- Casos implementados:
  1. Condición cumple -> avance a nodo destino + actualización de `nodoActualId/areaActualId`.
  2. Ninguna condición cumple -> `ApiException` `UNPROCESSABLE_ENTITY` con `SIN_SALIDA_VALIDA`.
  3. Condición inválida -> `ApiException` `BAD_REQUEST` con `CONDICION_INVALIDA`.

## Verificación

- `./mvnw -q test` OK.

## Estado

- Fase 1 completada.
- Siguiente fase recomendada: pruebas de integración end-to-end para flujo condicional + paralelo multi-hop.
