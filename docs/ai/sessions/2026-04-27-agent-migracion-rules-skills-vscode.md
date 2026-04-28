# Sesion 2026-04-27 - Migracion de rules y skills a VS Code

## Objetivo

Adaptar rules y skills del esquema Cursor al esquema nativo de VS Code, y limpiar configuracion obsoleta.

## Cambios realizados

1. Se migraron rules de `.cursor/rules/` a `.github/instructions/`:
   - `00-project-foundation.instructions.md`
   - `10-backend-modular-springboot.instructions.md`
   - `20-mongodb-persistence.instructions.md`
   - `30-fastapi-microservice.instructions.md`
   - `40-frontend-angular.instructions.md`
   - `50-devops-docker-nginx.instructions.md`
2. Se migraron skills de `.cursor/skills/` a `.agents/skills/`:
   - `caveman`
   - `fastapi-templates`
   - `find-skills`
   - `java-springboot`
   - `mongodb-modeling`
   - `saving-workspace-context`
   - `ui-ux-pro-max`
   - `verifying-in-browser`
   - `visual-qa-testing`
3. Se normalizaron skills migradas:
   - `ui-ux-pro-max/SKILL.md`: frontmatter valido + rutas de scripts ajustadas a `.agents/skills/ui-ux-pro-max/...`
   - `saving-workspace-context/SKILL.md`: referencias actualizadas de `.cursor/*` a `.github/instructions/` y `.agents/skills/`
4. Se eliminó configuracion legacy no usada:
   - `.cursor/agents/`
   - `.cursor/rules/`
   - `.cursor/skills/`
   - carpeta `.cursor/` (vacía)

## Resultado esperado

- VS Code usa una sola estructura coherente:
  - agentes en `.github/agents/`
  - instrucciones en `.github/instructions/`
  - skills en `.agents/skills/`
- Menor riesgo de drift o duplicidad entre Cursor y VS Code.

## Riesgos pendientes

- Verificar en uso real que todas las instrucciones se adjunten cuando corresponda (`applyTo`).
- Verificar descubribilidad automática de las skills migradas por `description`.
