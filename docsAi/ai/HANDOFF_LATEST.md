# HANDOFF_LATEST.md
# Handoff — Última sesión

## Fecha
2026-04-20

## Agente / Sesión
Cursor — Seeder de actores + login por portal

## Qué se hizo en esta sesión
Implementación de seeders de actores para que los logins por portal funcionen de forma consistente:

- Nuevo enum `PortalRol` como fuente única de códigos de portal/actor.
- `AuthService` actualizado para validar `portalRol` contra `PortalRol` (evita divergencias).
- `DevAuthSeedConfiguration` refactorizado a seeder idempotente:
  - upsert de roles
  - upsert de usuarios demo por actor
  - conserva contraseña de usuarios existentes; crea password demo para nuevos.
- Validación funcional con logins reales para los 4 actores demo.

### Archivos tocados (Backend seguridad + memoria)
- `backend/src/main/java/com/plataforma/tramites/modules/seguridad/model/PortalRol.java`
- `backend/src/main/java/com/plataforma/tramites/modules/seguridad/service/AuthService.java`
- `backend/src/main/java/com/plataforma/tramites/modules/seguridad/config/DevAuthSeedConfiguration.java`
- `docsAi/ai/CURRENT_STATE.md`
- `docsAi/ai/HANDOFF_LATEST.md`
- `docsAi/ai/NEXT_STEPS.md`
- `docsAi/ai/sessions/2026-04-20-agent-seeders-actores-login.md`

## Estado al finalizar
`docker compose build backend` finaliza correctamente y `POST /backend/api/auth/login`
funciona para los 4 usuarios demo (`demo123`) según su portal.

## Cómo continuar
1. Agregar endpoint protegido de prueba para validar JWT extremo a extremo.
2. Restringir semilla demo por perfil/entorno (mantener solo en desarrollo).
3. Completar RBAC por endpoint usando el `rolCodigo` del token.
