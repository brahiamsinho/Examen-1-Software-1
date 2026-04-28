# Sesión: Flutter en dispositivo físico + backend en Docker

## Contexto

Login desde teléfono con debug inalámbrico fallaba con mensaje genérico de sin conexión; el default de URL apuntaba a `10.0.2.2:8080` (solo emulador) y el contenedor `backend` no exponía 8080 en el host.

## Qué se hizo

1. **Docker Compose**: mapeo `BACKEND_HOST_PORT` (default 8080) → contenedor 8080 para acceso directo a Spring desde la red LAN.
2. **Flutter**: defaults por plataforma; mensaje de error en debug con URL efectiva; documentación `.env.example` / README.

## Qué debe hacer el desarrollador

1. `docker compose up -d --build` (o restart del servicio `backend` si ya estaba arriba).
2. En `mobile/tramites_cliente/`, `cp .env.example .env` y setear `API_BASE_URL=http://<IPv4_del_PC>:8080`.
3. Si Windows bloquea, regla de firewall para TCP 8080 entrante (ver README del móvil).

## Archivos tocados

- `docker-compose.yml`, `.env.example` (raíz)
- `mobile/tramites_cliente/lib/core/api_config.dart`, `.../login_screen.dart`, `.env.example`, `README.md`
