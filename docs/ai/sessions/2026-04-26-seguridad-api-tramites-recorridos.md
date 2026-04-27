# Sesión 2026-04-26 — Seguridad API trámites y recorridos

## Objetivo

Cerrar el bypass descrito en el handoff: `POST /api/tramites/{id}/recorridos` y `GET /api/tramites` sin las mismas reglas de área/rol que el flujo.

## Cambios

- **`TramiteFlujoAutorizacionService`**: `assertFlujoActor`, `assertResponsablePuedeActuarEnNodo`, `areaDelNodo`, `assertPuedeConsultarTramiteStaff`, `assertPuedeRegistrarRecorridoViaApiTramites`, helpers de rol y `requireUsuarioAreaId`.
- **`TramiteFlujoService`**: delega en el servicio de autorización; respuestas finales usan `TramitesService.obtenerInterno` para no exigir visibilidad staff tras ya haber autorizado el flujo.
- **`TramitesService`**: `listar`, `colaFifo`, `obtener`, `listarRecorridos` con reglas por rol; `listarRecorridosInterno` y `obtenerInterno` para cliente/servicios internos; `registrarRecorridoDesdeApi` para el controlador REST.
- **`TramiteRepository`**: consultas por `areaActualId` para listado y cola FIFO del responsable.
- **`ClienteTramitesService`**: detalle usa `listarRecorridosInterno` tras `obtenerDeCliente`.
- **`TramitesController`**: `POST .../recorridos` → `registrarRecorridoDesdeApi`.

## Limitación conocida

El listado paginado del responsable filtra por `areaActualId` del trámite; en bifurcación paralela con ramas de otras áreas y `areaActualId` aún en el nodo split, un responsable de una rama pendiente puede no ver el trámite en la primera página hasta que exista consulta/agregación más rica (la lectura puntual `GET /{id}` sí contempla ramas pendientes de su área).

## Verificación

Compilar backend con Maven en entorno con `mvn` en PATH: `mvn compile -DskipTests` desde `backend/`.
