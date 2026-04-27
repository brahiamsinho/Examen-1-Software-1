# ARCHITECTURE.md
# Arquitectura del Sistema

## Estilo arquitectonico
Arquitectura modular con servicio especializado.

Spring Boot funciona como backend principal (modular monolith por features).
FastAPI funciona como servicio especializado para analitica/IA.

## Justificacion de separacion Spring Boot / FastAPI
- Spring Boot: dominio principal, seguridad JWT, flujo de tramites y APIs core
- FastAPI: capacidades Python para analitica/ML y evolucion de componentes IA
- La separacion es por responsabilidad tecnica, no por moda

## Vista de alto nivel

```
[Browser / Mobile]
    |
    v
[Nginx :80]  <- punto de entrada
    |- /              -> [Angular]
    |- /backend/...   -> [Spring Boot :8080]
    '- /fastapi/...   -> [FastAPI :8000]
                         |- [MongoDB :27017]
                         '- [Redis :6379]
```

## Capas por servicio

### Angular (frontend)
- `core/`: servicios base, auth, guards, interceptores
- `features/`: modulos por dominio (disenador, planificador, responsable, etc.)
- `shared/`: piezas reutilizables

### Spring Boot (backend principal)
- `modules/*`: organizacion por feature (tramites, seguridad, cliente, politicas)
- `shared/*`: piezas transversales
- Persistencia con Spring Data MongoDB

### FastAPI (servicio especializado)
- `app/core/`: settings y configuracion base
- `app/middleware/`: trazabilidad de requests
- `app/domain/*`: dominios de analitica/recomendacion
- `app/api/v1/`: exposicion REST bajo prefijo `/api`

## Networking y despliegue
- Red Docker interna para todos los servicios
- Solo Nginx se publica al host
- Configuracion por variables de entorno para local, Docker y nube
