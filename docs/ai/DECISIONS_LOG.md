# Registro de decisiones técnicas

| Fecha | Decisión | Contexto |
|-------|----------|----------|
| 2026-04-20 | Rutas lazy para shell y dashboard | Reduce el bundle inicial y deja un patrón claro para nuevas features. |
| 2026-04-20 | Prefijos `/backend` y `/fastapi` en `environment` | Alineado con `nginx.conf` y Docker Compose; sin host fijo en el cliente. |
| 2026-04-20 | Health Spring en `GET .../health` | Coincide con `HealthController` de Spring Boot (`/health`, no `/api/health`). |
| 2026-04-20 | `proxy.conf.json` en `ng serve` | Permite desarrollo local del SPA contra Spring/FastAPI sin depender solo del stack Docker. |
| 2026-04-20 | APIs bajo `/api/...` sin segmento `/v1` | Spring (`@RequestMapping("/api/...")`), FastAPI (`API_PREFIX=/api`), Angular y Flutter actualizados; menos confusión con versionado no usado. |
