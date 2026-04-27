# Sesión: flujo trámite (cliente + responsable + seguridad)

## Objetivo

Cerrar el gap entre el modelo de negocio (documento → planificador → política → áreas → paralelo) y la implementación: API de seguimiento cliente, avance por conexión con validación de política/área, UI responsable, endurecimiento de permisos en flujo.

## Backend

- `TramiteRepository`: `findByClienteIdOrderByFechaRegistroDesc`, `findByIdAndClienteId`.
- `TramitesService`: `listarPorClienteId`, `obtenerDeCliente`.
- `ClienteTramitesController` + service: `GET /api/cliente/tramites`, `GET /api/cliente/tramites/{id}` → `ClienteTramiteDetalleResponse(tramite, recorridos)`.
- `TramiteFlujoService`: inyección `UsuarioRepository`; `listarSalidas` / `aprobarRamaParalela` / nuevo `avanzar` exigen **ROLE_ADMINISTRADOR** o **ROLE_RESPONSABLE_AREA** con `usuario.areaId == areaId` del nodo (rama en paralelo); bifurcación PARALELO ≥2 salidas → no `avanzar`, sí `aprobar-rama-paralela`; trámite en estado paralelo intermedio → conflicto en `avanzar`.
- `TramiteFlujoController`: `POST .../flujo/avanzar` + `TramiteFlujoAvanzarRequest`.

## Frontend Angular

- `TramitesApiService`: `getSalidas`, `avanzarFlujo`, `aprobarRamaParalela`.
- `responsable-area-tramites`: columna **Flujo**, modal con salidas; modo paralelo multi vs avance por `idConexion`.

## Mobile Flutter

- `ClienteApi`: listado y detalle; pantalla `MisTramitesScreen`; ruta `/mis-tramites`; enlace desde home.

## Archivos clave

- `backend/.../tramites/service/TramiteFlujoService.java`, `TramiteFlujoController.java`, `TramitesService.java`, `ClienteTramitesController.java`, `ClienteTramitesService.java`, `ClienteTramiteDetalleResponse.java`, `TramiteFlujoAvanzarRequest.java`, `TramiteRepository.java`
- `frontend/.../responsable-area/tramites/*`, `tramites-api.service.ts`, `salida-flujo.model.ts`
- `mobile/tramites_cliente/lib/features/cliente/cliente_api.dart`, `home/mis_tramites_screen.dart`, `main.dart`, `home_screen.dart`
- `README.md`, `docs/ai/CURRENT_STATE.md`
