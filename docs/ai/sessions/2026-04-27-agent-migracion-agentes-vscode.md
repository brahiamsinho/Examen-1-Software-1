# Sesion 2026-04-27 - Migracion de agentes a VS Code

## Objetivo

Adaptar los agentes creados en formato Cursor para que funcionen como custom agents nativos de VS Code.

## Cambios realizados

1. Se creo la carpeta `.github/agents` y se publicaron 7 archivos `.agent.md`:
   - `orchestrator.agent.md`
   - `architect-planner.agent.md`
   - `backend-springboot.agent.md`
   - `frontend-angular.agent.md`
   - `ia-fastapi.agent.md`
   - `mongodb-modeler.agent.md`
   - `devops-docker.agent.md`
2. Se ajusto frontmatter para VS Code:
   - `description` con trigger "Use when"
   - `tools` con aliases validos (`execute`, `read`, `edit`, `search`, `agent`, `todo`)
   - `argument-hint` por agente
   - `agents` en `orchestrator` para limitar delegacion a especialistas
3. Se actualizo memoria viva:
   - `docs/ai/CURRENT_STATE.md`
   - `docs/ai/HANDOFF_LATEST.md`
   - `docs/ai/NEXT_STEPS.md`
   - `docs/ai/DECISIONS_LOG.md`

## Resultado esperado

- Agentes visibles y utilizables desde VS Code custom agents.
- Mejor delegacion del orquestador a especialistas sin dependencia de estructura Cursor.
- Continuidad tecnica documentada para sesiones futuras.

## Notas

- Se mantiene `.cursor/agents/` como compatibilidad transitoria hasta definir deprecacion.
