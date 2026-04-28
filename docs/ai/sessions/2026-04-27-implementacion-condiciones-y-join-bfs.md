# Sesión 2026-04-27 — implementación condiciones + join BFS

## Objetivo

Implementar avance de flujo condicionado por aristas (`condicion`) y convergencia paralela multi-salto siguiendo arquitectura modular del backend.

## Cambios aplicados

### Módulo `tramites` (backend)

- Nuevo componente:
  - `backend/src/main/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoCondicionEvaluator.java`
- Operadores soportados:
  - `campo == valor`
  - `campo != valor`
  - `campo in [a,b,c]`
  - `campo exists`
- Error de sintaxis/operador:
  - `CONDICION_INVALIDA` con detalle de formato esperado.

### Integración en `TramiteFlujoService`

- `avanzar(...)`:
  - filtra salidas del nodo actual por condición evaluada.
  - si ninguna aplica: `SIN_SALIDA_VALIDA`.
  - mantiene validaciones de seguridad y de conexión seleccionada.
- `aprobarRamaParalela(...)`:
  - split paralelo inicial usa solo ramas cuya condición aplica.
  - convergencia pasa de “sucesor común directo” a **BFS multi-hop** (`calcularJoinMultiHop`).
  - si no hay convergencia común: `CONVERGENCIA_NO_ENCONTRADA`.
- Contexto de evaluación:
  - campos de `tramite` (y alias `tramite.*`),
  - datos del último formulario (`form.*`) cuando existe.

### Pruebas

- Nuevo test unitario:
  - `backend/src/test/java/com/plataforma/tramites/modules/tramites/service/TramiteFlujoCondicionEvaluatorTest.java`
- Casos cubiertos:
  - `==`, `!=`, `in`, `exists`,
  - expresión inválida.

## Verificación

- `./mvnw -DskipTests compile` OK.
- `./mvnw test -q` OK.

## Pendiente recomendado

- Tests de integración de flujo completo (DB + políticas + trámites) para casos condicionales y paralelo multi-hop.
