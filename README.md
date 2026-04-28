# Plataforma de Gestión de Trámites

Sistema de gestión de trámites basado en políticas de negocio.

## Stack tecnológico

| Servicio          | Tecnología              | Puerto interno                |
| ----------------- | ----------------------- | ----------------------------- |
| Reverse Proxy     | Nginx                   | 80 (expuesto)                 |
| Frontend          | Angular 17              | 80 (interno)                  |
| App móvil cliente | Flutter (Dart)          | fuera de Docker en desarrollo |
| Backend principal | Spring Boot 3 + Java 21 | 8080 (interno)                |
| Microservicio IA  | FastAPI + Python 3.12   | 8000 (interno)                |
| Base de datos     | MongoDB 7               | 27017 (interno)               |
| Caché             | Redis 7                 | 6379 (interno)                |

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

| Qué verificar          | URL                                                            |
| ---------------------- | -------------------------------------------------------------- |
| Frontend Angular       | http://localhost                                               |
| Nginx health           | http://localhost/health                                        |
| Spring Boot health     | http://localhost/backend/health                                |
| FastAPI health         | http://localhost/fastapi/health (alias: `/fastapi/api/health`) |
| FastAPI docs (Swagger) | http://localhost/fastapi/docs                                  |

### Acceso por rol (Angular + Spring)

- **Selector de portales:** `http://localhost/acceso` (o `http://localhost:4200/acceso` con `ng serve`).
- **Administrador:** `/acceso/administrador`
- **Diseñador de políticas de negocio:** `/acceso/politicas`
- **Responsable de área:** `/acceso/area`
- **Cliente (móvil):** app Flutter en `mobile/tramites_cliente` (mismo endpoint `POST /api/auth/login` con `portalRol: CLIENTE`).

Si `APP_DEV_AUTH_SEED_ENABLED=true` y Mongo está vacío en roles, Spring inserta usuarios demo (contraseña **`demo123`**):

| Rol                 | Correo                                                                                                                 |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| ADMINISTRADOR       | `admin@tramites.local`                                                                                                 |
| DISENADOR_POLITICAS | `politicas@tramites.local` y `politicas2@tramites.local` (misma contraseña; útil para probar el editor en tiempo real) |
| RESPONSABLE_AREA    | `area@tramites.local`                                                                                                  |
| CLIENTE             | `cliente@tramites.local`                                                                                               |

API: `POST /backend/api/auth/login` (vía Nginx) cuerpo JSON `{ "correo", "contrasena", "portalRol" }`. Respuesta incluye `accessToken` (Bearer) para llamadas autenticadas.

**Cliente (seguimiento):** con rol `CLIENTE`, `GET /api/cliente/tramites` (paginado) y `GET /api/cliente/tramites/{id}` devuelven trámite + recorridos solo si el expediente pertenece al usuario del token.

**Responsable de área (flujo):** `GET /api/tramites/{id}/flujo/salidas` devuelve JSON con `salidas` y metadatos del nodo actual (`nodoActualNombre`, `nodoActualCarrilBpmn`, `formularioExternoUrl` para p. ej. Google Forms). `POST .../flujo/avanzar` (cuerpo `{ "idConexion", "observacion?" }`) avanza sin bifurcación PARALELO múltiple; `POST .../flujo/aprobar-rama-paralela` para ramas paralelas. Solo **ADMINISTRADOR** o **RESPONSABLE_AREA** cuyo `areaId` coincide con el nodo correspondiente de la política.

**API genérica `/api/tramites` (staff):** `GET /api/tramites` y `GET /api/tramites/{id}/cola/fifo` quedan acotados por rol: administrador ve todo; planificador solo trámites **sin** `politicaId`; responsable solo trámites cuyo `areaActualId` coincide con su área (la cola FIFO respeta el mismo filtro). `GET /api/tramites/{id}`, `GET .../recorridos` y `POST .../recorridos` aplican la misma idea de visibilidad; el alta de recorrido por `POST` exige las mismas reglas de área que el flujo (sin política asignada, solo admin). El portal **cliente** sigue usando `/api/cliente/tramites/**` para listado y detalle.

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

#
docker compose down -v --rmi all

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
├── frontend/          # Angular 17 SPA (admin / diseñador / responsable)
├── mobile/            # Flutter — app cliente
│   └── tramites_cliente/
├── backend/           # Spring Boot 3 - Backend principal
├── fastapi/           # FastAPI - Microservicio analítica/IA
├── nginx/             # Reverse proxy config
├── docs/ai/           # Memoria persistente del agente (Markdown)
├── docker-compose.yml
├── .env.example
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
