# Sesión 2026-04-27 — fase 6 HTTP condición inválida + planificador + 401

## Objetivo

Completar quick wins de integración HTTP en flujo con foco en robustez de contrato y seguridad.

## Implementación

- Archivo actualizado:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/controller/TramiteFlujoControllerIntegrationTest.java`
- Nuevos escenarios cubiertos en `POST /api/tramites/{tramiteId}/flujo/avanzar`:
  1. Condición inválida en arista -> `400` con `CONDICION_INVALIDA`.
  2. Usuario con rol `PLANIFICADOR` intentando avanzar -> `403`.
  3. Sin token -> `401`.
  4. Token inválido -> `401`.

## Verificación

- `./mvnw -q test` OK.

## Estado

- Cobertura HTTP de flujo ya incluye:
  - condicional (true/false/inválida),
  - seguridad por rol/área,
  - auth (401),
  - paralelo multi-hop (con/sin convergencia).
