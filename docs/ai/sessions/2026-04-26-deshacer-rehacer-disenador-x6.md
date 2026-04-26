# Sesión: Deshacer / Rehacer en el diseñador de políticas (X6)

## Objetivo

Añadir flujo tipo Word: **Deshacer** (Ctrl+Z) y **Rehacer** (Ctrl+Y, Ctrl+Mayús+Z) en el editor de grafo, más botones en la caja de herramientas.

## Implementación

- Dependencia: `@antv/x6-plugin-history@^2.2.4` (par de `@antv/x6` 2.x).
- `disenador-politicas-modelado.component.ts`: `graph.use(new History({ stackSize: 300 }))`, atajos vía `bindKey` (ignorar si el foco está en input/textarea/select/contenteditable), `history:change` → `canUndo` / `canRedo`.
- Carga de política (`fromJSON` / `clearCells`) y actualización colaborativa remota: `disableHistory` → cambio de modelo → `enableHistory` + `cleanHistory` para no ofrecer “deshacer” a un estado remoto o vacío incoherente.

## Archivos

- `frontend/src/app/features/disenador-politicas/modelado/disenador-politicas-modelado.component.ts|html|scss`
- `frontend/package.json`
