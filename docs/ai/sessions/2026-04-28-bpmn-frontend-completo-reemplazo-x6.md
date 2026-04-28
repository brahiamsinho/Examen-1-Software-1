# Sesión: BPMN frontend completo (reemplazo X6)

## Objetivo

Reemplazar el editor de políticas X6 por BPMN en Angular y mantener compatibilidad con el backend actual (`nodos`/`conexiones`) agregando `bpmnXml` como fuente canónica visual.

## Implementado

- Frontend:
  - `disenador-politicas-modelado.component` migrado a `bpmn-js`.
  - Componentes nuevos: `bpmn-toolbar`, `bpmn-properties-panel`, `bpmn-validation-panel`.
  - Adaptador `bpmn-policy-adapter.ts`:
    - `policyToBpmnXml(politica)`
    - `bpmnXmlToPolicyUpsertBody(xml, politica)`
    - validaciones BPMN base.
  - Eliminadas utilidades X6 (`politica-x6-mapper.ts`, spec asociada).
  - Nueva prueba `bpmn-policy-adapter.spec.ts`.
- Backend:
  - Campo `bpmnXml` en `PoliticaNegocioDocument`, `PoliticaUpsertRequest`, `PoliticaNegocioResponse`.
  - `PoliticasDominioService` persiste/retorna `bpmnXml`.
  - Snapshots `PoliticaNegocioRevisionDocument` también guardan `bpmnXml`.
- Esquema:
  - `script.db` actualizado para `bpmnXml` en `politicas_negocio` y `politicas_negocio_revisiones`.

## Verificación

- `npx ng build --configuration=development` OK.
- No se pudo compilar backend en esta sesión: `mvn` no disponible en PATH del entorno.

## Pendiente inmediato

- Ajustar pruebas automatizadas del frontend en este workspace (`ng test` no resolvió target con el comando actual).
- Añadir edición avanzada BPMN (nombre de elementos, condiciones de secuencia desde UI).
