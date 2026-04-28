# Sesión 2026-04-27 — fase 3 tests seguridad flujo

## Objetivo

Ejecutar fase 3 aprobada: validar reglas de autorización por rol/área en el flujo modular de trámites.

## Implementación

- Nuevo test:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoAutorizacionServiceTest.java`
- Casos cubiertos:
  1. Admin puede actuar sobre nodo sin restricción de área.
  2. Responsable con área distinta al nodo es bloqueado (`403`).
  3. Responsable puede consultar trámite por rama paralela pendiente de su área.
  4. Registro de recorrido via API ajusta `areaId` según área del nodo para no-admin.

## Verificación

- `./mvnw -q test` OK.

## Estado

- Fase 3 completada (nivel tests de servicio).
- Próximo salto: integración HTTP/repos reales para validar contrato completo de endpoints.
