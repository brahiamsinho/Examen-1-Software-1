# Sesión 2026-04-27 — fase 4 integración HTTP flujo

## Objetivo

Iniciar pruebas end-to-end lite (HTTP real con `MockMvc`) para el módulo de flujo de trámites.

## Implementación

- Nuevo test de integración:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/controller/TramiteFlujoControllerIntegrationTest.java`
- Casos cubiertos sobre `POST /api/tramites/{tramiteId}/flujo/avanzar`:
  1. Condición cumple -> `200 OK` y `nodoActualId` esperado.
  2. Condición no cumple -> `422` con mensaje `SIN_SALIDA_VALIDA`.

## Notas de arquitectura

- Se mantiene modularidad por feature (`modules/tramites`).
- Integración atraviesa capa web + seguridad JWT + servicio de flujo.
- Repos/servicios secundarios se mockean con `@MockBean` para aislar escenario del endpoint sin romper contrato HTTP.

## Verificación

- `./mvnw -q test` OK.

## Siguiente expansión recomendada

- Agregar integración HTTP para:
  - `aprobar-rama-paralela` con convergencia multi-hop,
  - bloqueos de seguridad (`403`) por rol/área.
