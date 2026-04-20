# Iniciar el proyecto (desarrollo local)

## Requisitos

- Docker Desktop (o Docker Engine + Compose v2)
- Git

## Pasos

1. Clonar el repositorio y entrar al directorio del proyecto.

2. Variables de entorno en la raíz:

   ```bash
   cp .env.example .env
   ```

   Opcional: copiá también los `.env.example` de cada servicio si los usás fuera de Docker (`backend/.env`, `fastapi/.env`).

3. Levantar el stack:

   ```bash
   docker compose up --build
   ```

   En segundo plano: `docker compose up --build -d`

4. Comprobar:

   - Nginx: `http://localhost/health`
   - Backend: `http://localhost/backend/health`
   - FastAPI: `http://localhost/fastapi/health` (también `/fastapi/api/health`)

5. Detener:

   ```bash
   docker compose down
   ```

Más detalle y comandos útiles: `README.md`.
