---
name: devops-docker-nginx
description: "Use when changing Dockerfiles, compose, nginx, or environment wiring. Enforces simple, reproducible local infrastructure ready for Azure VM migration."
applyTo:
  - docker-compose.yml
  - docker/**
  - nginx/**
  - backend/Dockerfile
  - frontend/Dockerfile
  - fastapi/Dockerfile
  - .env
  - .env.example
---

# DevOps Docker Nginx Rules

## Regla principal

La infraestructura local debe ser simple, reproducible y lista para migrar despues a Azure VM.

## Servicios esperados

- frontend Angular
- backend Spring Boot
- microservicio FastAPI
- MongoDB
- Redis
- Nginx

## Principios

- Usar Dockerfiles limpios por servicio.
- Usar docker-compose.yml como punto central de desarrollo local.
- Nginx debe funcionar como reverse proxy.
- Mantener nombres claros de servicios, redes y volumenes.
- Usar variables de entorno de ejemplo, no credenciales reales.

## Prohibido

- Meter Kubernetes o complejidad innecesaria en esta etapa.
- Hacer despliegue productivo avanzado antes de cerrar la base tecnica.
- Hardcodear puertos, hosts o secretos dentro del codigo.

## Alcance inicial

- healthchecks
- compose funcional
- nginx base
- .env.example
- puertos bien documentados
