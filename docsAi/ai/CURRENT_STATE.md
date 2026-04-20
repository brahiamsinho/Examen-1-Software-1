# CURRENT_STATE.md
# Estado Actual del Proyecto

## Fecha de última actualización
2026-04-16

## Etapa
**Etapa 0 — Bootstrapping inicial completado**

## Qué está hecho ✅
- Estructura completa de carpetas del proyecto
- Angular 17 standalone (main.ts, app.config, routes, HomeComponent mínimo)
- Spring Boot 3 con endpoint `/api/health` funcional
- FastAPI con endpoint `/api/v1/health` funcional + Swagger docs
- Configuración Nginx como reverse proxy (rutas /backend/, /fastapi/)
- docker-compose.yml con 6 servicios orquestados
- Dockerfiles multi-stage para Angular, Spring Boot y FastAPI
- Variables de entorno por servicio (.env.example)
- .gitignore completo
- README inicial con arquitectura y comandos
- docs/ai/ con memoria persistente del proyecto

## Qué NO está hecho todavía ⏳
- Lógica de negocio (dominio de trámites)
- Entidades y colecciones MongoDB
- Autenticación (JWT / Spring Security)
- Módulos de features en Angular
- Servicios de dominio en Spring Boot
- Modelos ML/IA en FastAPI
- HTTPS / Certbot para Azure VM
- Testing (unit, integration)
- CI/CD pipeline

## Servicios y puertos (en Docker)
| Servicio | Puerto expuesto |
|---|---|
| Nginx (acceso principal) | 80 |
| Resto | solo internos |

## Comando para levantar
```bash
cp .env.example .env
cp backend/.env.example backend/.env
cp fastapi/.env.example fastapi/.env
docker compose up --build
```
