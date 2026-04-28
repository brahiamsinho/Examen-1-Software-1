# Sesión: Mobile - Token Persistencia + Perfil

**Fecha:** 2026-04-27
**Agente:** mobile-flutter
**Tipo:** Feature

## Resumen

Se implementó persistencia segura de sesión y pantalla de perfil para la app Flutter `tramites_cliente`.

## Cambios realizados

### 1. `pubspec.yaml`
- Agregada dependencia `flutter_secure_storage: ^9.2.2`

### 2. `lib/core/secure_storage.dart` (nuevo)
- Servicio `SecureStorage` con:
  - `saveSession(token, nombres, apellidos, correo, rol)` → guarda todos los campos
  - `getToken()` → lee solo el token
  - `getUserInfo()` → lee nombres, apellidos, correo, rol
  - `clearSession()` → borra todo
- Configurado con `encryptedSharedPreferences` en Android y `KeychainAccessibility.first_unlock_this_device` en iOS

### 3. `lib/features/auth/auth_repository.dart`
- `LoginResult` ahora incluye: `apellidos`, `correo`, `expiresInSeconds` (todos los campos del backend `LoginResponse`)
- Parser del JSON actualizado para extraer los nuevos campos

### 4. `lib/main.dart`
- Refactorizado de global variables a `StatefulWidget` con `SessionData`
- `_loadSession()` al iniciar: lee storage, si hay token válido redirige a `/`
- `_onLoggedIn()` guarda en storage + estado
- `_onLogout()` limpia storage + estado, redirige a `/login`
- Nueva ruta `/perfil` → `ProfileScreen`

### 5. `lib/features/auth/login_screen.dart`
- Callback `onLoggedIn` ahora recibe 5 parámetros: `token, nombres, correo, rol, apellidos`

### 6. `lib/features/profile/profile_screen.dart` (nuevo)
- Avatar circular con iniciales del nombre
- Cards con: nombre completo, correo, rol (label legible)
- Botón copiar al portapapeles en cada campo
- Botón logout con diálogo de confirmación

### 7. `lib/features/home/home_screen.dart`
- Agregado `IconButton(Icons.person_outline)` en AppBar → navega a `/perfil`

### 8. `docs/ai/` actualizados
- `CURRENT_STATE.md`: sección mobile ampliada con persistencia + perfil
- `NEXT_STEPS.md`: reordenado, notificaciones como prioridad #1
- `HANDOFF_LATEST.md`: sección mobile actualizada con detalles de cambios

## Ejecutado
- `flutter pub get` → dependencias instaladas correctamente

## Pendiente para próxima sesión
- Pantalla de notificaciones (`/api/seguimiento/notificaciones`)
- Paginación completa (botón "Anterior") en Mis Trámites
- Restricción de tipos de archivo en file picker
- WebSocket para actualizaciones en tiempo real
