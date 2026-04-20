# CURRENT_STATE.md
# Estado Actual del Proyecto

## Fecha de última actualización
2026-04-20

## Etapa
**Etapa 0 — Bootstrapping inicial completado** · **FastAPI reforzado como esqueleto IA/analítica**

## Qué está hecho ✅
- Estructura completa de carpetas del proyecto
- Angular 17 standalone (shell + dashboard base, rutas lazy, `ApiService`, entornos tipados, proxy local)
- Refresh visual UI/UX del frontend Angular:
  - tokens visuales globales alineados a estilo accesible de alto contraste
  - shell con header flotante, foco visible y superficies elevadas
  - `acceso-hub` redisenado con cards por rol y mejor jerarquia visual
  - `portal-login` mejorado con ayudas contextuales, foco y estados visuales
  - `dashboard` mejorado con seccion de modulos y accion manual para refrescar salud
- Spring Boot 3 con endpoint `/health` y estructura modular por feature (`modules/*`)
- Seguridad Spring: login por portal (`/api/auth/login`) y semilla idempotente de actores demo
  (roles + usuarios) para ADMINISTRADOR, DISENADOR_POLITICAS, RESPONSABLE_AREA y CLIENTE.
- FastAPI: `GET /health` (canónico) + alias `GET /api/health`, Swagger, capas `core/`, `middleware/`, dominios stub `bottlenecks` y `recommendations`, settings por entorno
- Configuración Nginx como reverse proxy (rutas /backend/, /fastapi/)
- docker-compose.yml con 6 servicios orquestados
- Dockerfiles multi-stage para Angular, Spring Boot y FastAPI
- Variables de entorno por servicio (.env.example)
- .gitignore completo
- README inicial con arquitectura y comandos
- Memoria del agente en `docsAi/ai/` e índice en `docs/ai/README.md`
- Rutas REST Spring bajo `/api/...` (sin segmento `/v1`); login JWT por portal; app Flutter cliente (esqueleto)

## Qué NO está hecho todavía ⏳
- Lógica de negocio (dominio de trámites)
- Entidades y colecciones MongoDB
- Autenticación y autorización finas (RBAC por recurso, permisos por endpoint)
- Módulos de features en Angular (mas alla del cascaron visual y accesos iniciales)
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
