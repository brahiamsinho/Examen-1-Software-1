# Sesión 2026-04-27 — fix respuesta políticas ObjectId string

## Problema detectado

Aunque el frontend había endurecido normalización de IDs, `GET /api/politicas/{id}` seguía devolviendo `ObjectId` anidados como objeto BSON (`{timestamp,date}`) en:

- `nodos[].areaId`
- `nodos[].asignacionesResponsable[].usuarioId`
- `nodos[].asignacionesResponsable[].areaId`

Esto provocaba pérdida visual de selección (área/responsable) al recargar el diseñador.

## Cambio aplicado

- `backend/src/main/java/com/plataforma/tramites/modules/politicas/dto/PoliticaNegocioResponse.java`
  - Se definieron DTOs de salida explícitos para nodos/conexiones/asignaciones con IDs `string`.
- `backend/src/main/java/com/plataforma/tramites/modules/politicas/service/PoliticasDominioService.java`
  - `toResponse` ahora mapea `ObjectId` con `toHexString()`.

## Verificación

- Rebuild backend en Docker completado.
- `GET /backend/api/politicas/{id}` verificado: responde IDs anidados como strings hex.

## Resultado esperado

- El diseñador mantiene área/responsable tras `Guardar grafo` + `F5` porque el contrato backend ya no rompe el parsing de IDs en frontend.
