# Handoff reciente

## Último cambio relevante (frontend Angular)

Se consolidó la **base profesional** del frontend alineada con `.cursor/rules/40-frontend-angular.md` y el stack del examen (plataforma de trámites / políticas de negocio).

### Qué tocar a continuación

1. Añadir rutas hijas bajo el shell por feature (lazy `loadComponent` o rutas con `loadChildren` si se pasa a módulos).
2. Definir contratos REST con Spring Boot (DTOs en `core/models`, servicios por feature).
3. Autenticación: interceptor HTTP + guardas de ruta cuando el módulo de seguridad esté listo.
4. Opcional: `npx skills find angular` en [skills.sh](https://skills.sh/) si se quieren skills comunitarias adicionales (p. ej. testing o a11y).

### Archivos guía

- `frontend/src/app/app.routes.ts`
- `frontend/src/app/core/layout/shell-layout.component.html`
- `frontend/src/app/core/services/api.service.ts`
- `frontend/src/environments/environment*.ts`
- `frontend/proxy.conf.json`
