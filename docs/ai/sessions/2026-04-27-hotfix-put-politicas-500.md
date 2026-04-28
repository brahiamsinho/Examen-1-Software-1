# Sesion 2026-04-27 - Hotfix PUT politicas 500

## Contexto

- En `disenador/modelado`, al guardar y recargar (`F5`), los cambios parecian perderse.
- Evidencia operativa: `PUT /backend/api/politicas/{id}` respondiendo `500` en Nginx.
- Se observo actividad concurrente en Mongo alrededor de `politicas_negocio` y documentos legacy.

## Diagnostico aplicado

- Riesgo principal detectado: politicas antiguas sin `lockVersion` (campo agregado luego con `@Version` para concurrencia optimista).
- Ese estado deja el guardado en una ruta no compatible y termina en error servidor.

## Cambios implementados

1. `backend/src/main/java/com/plataforma/tramites/modules/politicas/service/PoliticasDominioService.java`
   - Se inyecto `MongoTemplate`.
   - Nuevo metodo `inicializarLockVersionLegacySiHaceFalta(...)`.
   - En `reemplazar(...)`, antes de validar `lockVersion` del request, se migra en caliente:
     - si el documento no tiene `lockVersion`, se hace `updateFirst` a `lockVersion=0` y se sincroniza el objeto en memoria.

2. `backend/src/main/java/com/plataforma/tramites/shared/exception/GlobalExceptionHandler.java`
   - Nuevo handler `DuplicateKeyException -> 409 CONFLICT` con mensaje funcional para el usuario.

## Resultado esperado en UX

- Guardar grafo vuelve a persistir sin error 500.
- Al recargar, se mantiene area/responsable y cambios del nodo.
- Si hay conflicto de escritura real, el usuario recibe 409 en lugar de error generico 500.

## Verificacion recomendada

1. Abrir una politica existente (incluyendo una creada antes del hotfix).
2. Cambiar nombre/area/responsable de un nodo.
3. Clic en `Guardar grafo completo`.
4. Confirmar que no aparece 500 en `PUT /api/politicas/{id}`.
5. Recargar con `F5` y validar que los cambios siguen.
