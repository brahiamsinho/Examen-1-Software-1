# Sesion 2026-04-27 - fix ObjectId en modelado

## Contexto

- Se reporto perdida de cambios al editar nodos en `/disenador/modelado`.
- En logs de Nginx aparecia `GET /backend/api/seguridad/usuarios?areaId=%5Bobject%20Object%5D` (400).
- El efecto visible era: aplicar nodo + guardar grafo + recargar => asignacion de area/responsable desaparecia.

## Cambios aplicados

1. `frontend/src/app/features/disenador-politicas/utils/politica-x6-mapper.ts`
   - Nuevo helper `asStringId`.
   - Normalizacion de `areaId`, `usuarioId` y `areaId` de asignaciones:
     - al construir celdas desde politica API.
     - al leer `node.getData()` para serializar el grafo.

2. `frontend/src/app/features/disenador-politicas/modelado/disenador-politicas-modelado.component.ts`
   - Normalizacion en `aplicarInspectorNodo`, `onCambioAreaNodo`, `cargarUsuariosArea` y `selectNode`.
   - Helper local `asStringId` para blindar el estado del inspector ante valores tipo objeto.

3. `frontend/src/app/features/disenador-politicas/models/politica-negocio.model.ts`
   - `politicaDtoToUpsertBody` ahora normaliza IDs antes de enviar `PUT /api/politicas/{id}`.

## Resultado esperado

- No volver a emitir `areaId=[object Object]` en consultas de usuarios por area.
- Guardado/recarga del grafo debe preservar `areaId` y `asignacionesResponsable` de cada nodo.

## Pendiente inmediato

- Verificacion manual end-to-end en navegador con la politica que reproducia el bug (`69ef0aebc8156125d40b1bd2`).
