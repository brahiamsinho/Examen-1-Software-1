# Sesion 2026-04-20 — Seeder de actores y login por portal

## Objetivo
Asegurar que los actores demo del sistema queden sembrados de forma confiable y que el login por portal funcione con esos actores.

## Cambios aplicados
- Se creó `PortalRol` como enum central para códigos de actor/portal:
  - `ADMINISTRADOR`
  - `DISENADOR_POLITICAS`
  - `RESPONSABLE_AREA`
  - `CLIENTE`
- `AuthService` ahora valida `portalRol` con ese enum en lugar de strings duplicados.
- `DevAuthSeedConfiguration` fue refactorizado a semilla idempotente:
  - upsert de roles por código
  - upsert de usuarios demo por correo
  - asignación de `rolId` correcta para cada actor
  - activación de usuario (`estado=true`)
  - password demo solo para usuarios nuevos

## Verificación ejecutada
- Build backend por Docker: `docker compose build backend` ✅
- Stack arriba: `docker compose up -d --build` ✅
- Login probado contra `POST /backend/api/auth/login` para los 4 actores demo con `demo123` ✅
  - admin@tramites.local -> ADMINISTRADOR
  - politicas@tramites.local -> DISENADOR_POLITICAS
  - area@tramites.local -> RESPONSABLE_AREA
  - cliente@tramites.local -> CLIENTE

## Nota operativa
En este entorno no está disponible `mvn` en host. La validación de compilación se realizó dentro del flujo Docker del servicio backend.
