## Sesion: planificador + flujo paralelo (MVP)

### Backend
- Nuevo `PortalRol.PLANIFICADOR`, usuario semilla `planificador@tramites.local`, matcher `SecurityConfiguration` para `/api/planificador/**`.
- `TramiteRepository.findByPoliticaIdIsNullOrderByFechaRegistroDesc`.
- `TramitesService`: `listarSinPolitica`, `asignarPoliticaDesdeIngreso` (valida nodo de intake vs `app.workflow.intake-node-id`), limpia estado paralelo embebido, recorrido de asignacion.
- `TramiteDocument`: campos opcionales para estado de split paralelo (`paraleloSplitNodoId`, listas pendientes/aprobadas, `paraleloJoinNodoId`).
- `TramiteFlujoService` + `TramiteFlujoController`: salidas desde nodo actual; `aprobar-rama-paralela` con join por **interseccion de sucesores directos** de cada rama (MVP).
- `PlanificadorTramitesController` + DTO `PlanificadorAsignarPoliticaRequest`.

### Frontend
- Rutas `acceso/planificador` y lazy `planificador/pendientes`; hub y `AuthService.portalHomeUrl`.
- `PlanificadorApiService`; pantalla de pendientes con selector de politicas (`PoliticasApiService`).

### Limitaciones conocidas
- Join paralelo solo si existe un unico nodo destino **comun en un paso** desde todas las ramas.
- Sin evaluacion automatica de `condicion` en aristas todavia.
