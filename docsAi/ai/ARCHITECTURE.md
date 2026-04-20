# ARCHITECTURE.md
# Arquitectura del Sistema

## Estilo arquitectónico
**Arquitectura modular con microservicio especializado**

No microservicios puros (sería sobreingeniería en esta etapa).
Spring Boot actúa como backend principal (modular monolith).
FastAPI actúa como servicio especializado para analítica/IA, separado por responsabilidad técnica.

## Justificación de la separación Spring Boot / FastAPI
- Spring Boot: fortaleza en lógica de negocio, transacciones, seguridad, integración robusta
- FastAPI: fortaleza en Python científico (ML, pandas, sklearn, etc.), async nativo, baja latencia
- Esta combinación es un patrón real y válido en producción (polyglot stack)

## Diagrama de alto nivel

```
[Browser]
    │
    ▼
[Nginx :80]  ← único punto de entrada
    ├── /              → [Angular :80]
    ├── /backend/api/  → [Spring Boot :8080]
    └── /fastapi/api/  → [FastAPI :8000]
                               │
                    [MongoDB :27017] [Redis :6379]
```

## Capas por servicio

### Angular (frontend)
- environments/         → config por entorno
- core/services/        → servicios HTTP globales
- features/             → módulos de funcionalidad
- shared/               → componentes reutilizables

### Spring Boot (backend principal)
- shared/controller/    → controladores REST
- [futuro] modules/     → módulos de dominio (tramites, usuarios, etc.)
- [futuro] config/      → configuración de seguridad, CORS, Redis

### FastAPI (microservicio)
- app/api/v1/           → endpoints versionados
- [futuro] app/services/→ lógica analítica, ML
- [futuro] app/models/  → esquemas Pydantic

## Red interna Docker
Todos los servicios en red `tramites-net`.
Solo Nginx expone puerto 80 al host.
Servicios internos NO son accesibles directamente desde exterior.

## Persistencia
- MongoDB: base de datos documental principal
- Redis: caché, sesiones, rate limiting (futuro)

## Despliegue target
Azure VM → Docker Compose → Nginx con dominio + HTTPS (Certbot)
