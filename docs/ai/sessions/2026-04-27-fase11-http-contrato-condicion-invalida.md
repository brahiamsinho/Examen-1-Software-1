# Sesión 2026-04-27 - Fase 11 (HTTP contrato condición inválida)

## Objetivo

Validar de extremo a extremo que el endpoint de flujo devuelve mensajes guiados y consistentes cuando la condición de una conexión es inválida.

## Cambios implementados

- Archivo:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/controller/TramiteFlujoControllerIntegrationTest.java`

- Tests añadidos:
  1. `avanzar_whenCondicionCampoInvalido_returns400WithGuidedMessage`
  2. `avanzar_whenCondicionInListEmpty_returns400WithGuidedMessage`

- Test reforzado:
  - `avanzar_whenCondicionInvalida_returns400` ahora también valida presencia de:
    - `"Operadores soportados"`
    - `"Ejemplos"`

## Validación ejecutada

- `./mvnw -q -Dtest=TramiteFlujoControllerIntegrationTest test`
- Resultado: verde.

## Reflejo en experiencia de usuario

- **Visible en pantalla:** Sí (mensajes de error que consume frontend desde API).
- **Impacto usuario:** Medio-Alto.
- Beneficio:
  - mensajes más claros cuando la condición está mal escrita,
  - menor confusión para planificador/diseñador al corregir política de flujo.

## Próximo paso recomendado

- Documentar formalmente el mini-lenguaje `condicion` en guía funcional del proyecto con ejemplos válidos/inválidos y tabla error API -> acción sugerida.
