# Plataforma de Gestión de Trámites

Sistema de gestión de trámites basado en políticas de negocio.

## Stack tecnológico

| Servicio | Tecnología | Puerto interno |
|---|---|---|
| Reverse Proxy | Nginx | 80 (expuesto) |
| Frontend | Angular 17 | 80 (interno) |
| Backend principal | Spring Boot 3 + Java 21 | 8080 (interno) |
| Microservicio IA | FastAPI + Python 3.12 | 8000 (interno) |
| Base de datos | MongoDB 7 | 27017 (interno) |
| Caché | Redis 7 | 6379 (interno) |

> Solo Nginx expone puerto al host. El resto comunica internamente via red Docker `tramites-net`.

---

## Arquitectura

```
Cliente (browser)
        │
        ▼
   [Nginx :80]  ← Reverse Proxy
   ┌────┴────────────────────────┐
   │                             │
   ▼                             ▼
[Frontend Angular]     /backend/ → [Spring Boot :8080]
                       /fastapi/ → [FastAPI :8000]
                                         │
                              [MongoDB] [Redis]
```

Nginx actúa como gateway único. El frontend consume las APIs a través de rutas relativas (`/backend/`, `/fastapi/`), lo que permite desplegar en cualquier IP o dominio sin cambiar código.

---

## Primeros pasos

Guía corta paso a paso: [iniciar_proyecto.md](iniciar_proyecto.md).

### 1. Clonar y preparar variables de entorno

```bash
git clone <repo-url>
cd examen-1-software

# Copiar y ajustar .env raíz
cp .env.example .env

# Copiar .env de cada servicio
cp backend/.env.example backend/.env
cp fastapi/.env.example fastapi/.env
```

### 2. Levantar todos los servicios

```bash
docker compose up --build
```

Para ejecutar en background:

```bash
docker compose up --build -d
```

### 3. Verificar que todo funciona

| Qué verificar | URL |
|---|---|
| Frontend Angular | http://localhost |
| Nginx health | http://localhost/health |
| Spring Boot health | http://localhost/backend/api/health |
| FastAPI health | http://localhost/fastapi/api/v1/health |
| FastAPI docs (Swagger) | http://localhost/fastapi/docs |

---

## Comandos útiles

```bash
# Ver logs de todos los servicios
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f backend
docker compose logs -f fastapi
docker compose logs -f nginx

# Detener todos los servicios
docker compose down

# Detener y eliminar volúmenes (cuidado: borra datos MongoDB)
docker compose down -v

# Reconstruir un servicio específico
docker compose up --build backend

# Ver estado de los contenedores
docker compose ps

# Acceder a MongoDB shell (sin auth en el compose de desarrollo)
docker exec -it tramites-mongo mongosh

# Acceder a Redis CLI
docker exec -it tramites-redis redis-cli
```

---

## Estructura del proyecto

```
examen-1-software/
├── frontend/       # Angular 17 SPA
├── backend/     # Spring Boot 3 - Backend principal
├── fastapi/   # FastAPI - Microservicio analítica/IA
├── nginx/                  # Reverse proxy config
├── docsAi/                 # Memoria persistente del agente (Markdown)
│   └── ai/
├── docker-compose.yml      # Orquestación de servicios
├── .env.example            # Variables de entorno (ejemplo)
└── .gitignore
```

---

## Despliegue en Azure VM (futuro)

1. Provisioná una VM con Ubuntu 22.04 LTS en Azure
2. Instalá Docker + Docker Compose en la VM
3. Copiá el proyecto (git clone o scp)
4. Ajustá las variables de entorno de producción
5. Configurá un dominio o IP elástica de Azure
6. (Opcional) Agregá HTTPS con Certbot + Nginx

---

## Estado del proyecto

**Etapa actual:** Bootstrapping inicial — lienzo limpio ✓  
**Próxima etapa:** Definir dominio, entidades y casos de uso
