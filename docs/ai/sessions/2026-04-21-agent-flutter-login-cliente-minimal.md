# Sesión 2026-04-21 — Login móvil cliente (minimalista)

## Objetivo

Pantalla de login del portal **CLIENTE** en Flutter con diseño minimalista, alineada al modelo `usuarios` de `script.db` / `init-mongo.js` y al contrato Spring `POST /api/auth/login`.

## Cambios

- `mobile/tramites_cliente/lib/features/auth/login_screen.dart`: layout limpio, `AutofillGroup`, `Semantics`, animación suave de error, campos vacíos por defecto, texto de ayuda solo en `kDebugMode` (referencia a semilla en backend, sin contraseña en pantalla).
- `mobile/tramites_cliente/lib/main.dart`: `ColorScheme` neutro, `surface` claro, `inputDecorationTheme` con bordes inferiores (estética minimalista).

## Sin cambios

- `auth_repository.dart`: ya enviaba `correo`, `contrasena`, `portalRol: CLIENTE` a `/api/auth/login`.

## Verificación

- `flutter analyze` en `mobile/tramites_cliente` sin issues.

## Siguiente paso sugerido

- Persistir JWT (p. ej. `flutter_secure_storage`) y refrescar redirección del router al arranque.
