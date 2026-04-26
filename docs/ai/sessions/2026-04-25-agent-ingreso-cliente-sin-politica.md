## Sesion: ingreso cliente sin politica seleccionada

### Objetivo
- Ajustar el flujo para que el cliente suba documento sin elegir politica de negocio.
- El caso entra primero a atencion al cliente; planificacion asigna politica despues.

### Cambios aplicados
- Backend `ClienteTramitesService`:
  - elimina dependencia de `politicaId` en ingreso cliente.
  - crea tramite con `TramitesService.crearIngresoCliente(...)`.
  - registra recorrido inicial en nodo configurable de intake.
- Backend `TramitesService`:
  - nuevo metodo `crearIngresoCliente(...)` para alta sin politica asignada.
  - `toResponse` tolera `politicaId` nulo.
- Backend `ClienteTramitesController`:
  - multipart `/api/cliente/tramites/inicio-con-documento` sin `politicaId`.
- Backend `DocumentosService`:
  - si un tramite no tiene politica ni nodo actual, devuelve error de planificacion pendiente.
- Config:
  - nuevas propiedades `app.workflow.intake-node-id`, `app.workflow.intake-node-name`, `app.workflow.intake-area-id`.
- Mobile Flutter:
  - elimina selector/listado de politicas.
  - mantiene carga de archivo + asunto opcional.
  - mensaje de exito actualizado a ingreso por atencion al cliente.
- Limpieza:
  - eliminado endpoint `/api/cliente/politicas` y DTO asociado.

### Estado de verificacion
- `dart analyze` en `mobile/tramites_cliente`: sin issues.
- Lints IDE en archivos Java tocados: sin errores.
- Build Maven no verificado en esta maquina por ausencia de `mvn` en PATH.

### Siguientes pasos recomendados
- Implementar endpoint de planificador para asignar politica al tramite en cola de atencion.
- Implementar motor de avance con soporte real a ramas paralelas y condicion de merge.
