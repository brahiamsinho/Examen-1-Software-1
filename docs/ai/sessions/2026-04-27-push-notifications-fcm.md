# Sesión: Push Notifications Firebase FCM

**Fecha:** 2026-04-27
**Agente:** mobile-flutter + backend-springboot
**Tipo:** Feature

## Resumen

Se implementó sistema de notificaciones push usando Firebase Cloud Messaging (FCM) para alertar al cliente cuando su trámite avanza de estado.

## Firebase Project

- **Project ID:** `transporte-si2`
- **Package name:** `com.example.tramites_cliente`
- **Service account:** `firebase-adminsdk-fbsvc@transporte-si2.iam.gserviceaccount.com`

## Cambios Mobile (Flutter)

### 1. `pubspec.yaml`
- Agregadas: `firebase_core: ^3.6.0`, `firebase_messaging: ^15.1.3`, `flutter_local_notifications: ^17.2.2`

### 2. Android config
- `settings.gradle.kts`: plugin `com.google.gms.google-services` v4.4.2
- `app/build.gradle.kts`: aplicado `id("com.google.gms.google-services")`
- `app/google-services.json`: corregido package name de `com.example.mobile` → `com.example.tramites_cliente`
- `AndroidManifest.xml`: permisos `INTERNET` + `POST_NOTIFICATIONS`

### 3. `lib/core/firebase_config.dart` (nuevo)
- `initialize()`: init Firebase + local notifications + foreground presentation
- `requestPermission()`: solicita permiso de notificaciones
- `getToken()`: obtiene FCM token del dispositivo
- Foreground handler: muestra notificación local cuando llega mensaje

### 4. `lib/main.dart`
- `FirebaseConfig.initialize()` en `main()`

### 5. `lib/features/auth/login_screen.dart`
- Después de login exitoso: solicita permiso → obtiene FCM token → envía al backend

### 6. `lib/features/notificaciones/notificacion_api.dart` (nuevo)
- `registrarFcmToken(accessToken, fcmToken)` → `POST /api/seguimiento/notificaciones/fcm-token`

## Cambios Backend (Spring Boot)

### 1. `pom.xml`
- Agregada: `firebase-admin:9.2.0`

### 2. `application.yml`
- Nueva config: `app.firebase.credentials-path: ${FIREBASE_CREDENTIALS_PATH:}`

### 3. `docker-compose.yml`
- Backend: env `FIREBASE_CREDENTIALS_PATH` + volume mount para credenciales

### 4. `seguimiento/config/FirebaseConfiguration.java` (nuevo)
- Inicializa Firebase Admin SDK desde archivo de credenciales
- Si no hay credenciales configuradas, loguea info y continúa (notificaciones desactivadas)

### 5. `seguimiento/document/FcmTokenDocument.java` (nuevo)
- Colección: `fcm_tokens`
- Campos: `usuarioId`, `fcmToken`, `registradoEn`, `ultimoUso`

### 6. `seguimiento/repository/FcmTokenRepository.java` (nuevo)
- `findByUsuarioId`, `deleteByFcmToken`, `deleteByUsuarioIdAndFcmToken`

### 7. `seguimiento/service/FcmNotificationService.java` (nuevo)
- `registrarToken(usuarioId, fcmToken)`: guarda/actualiza token
- `enviarNotificacion(usuarioId, titulo, cuerpo, tramiteCodigo)`: envía FCM a todos los tokens del usuario
- Limpia tokens inválidos automáticamente

### 8. `seguimiento/dto/FcmTokenRequest.java` (nuevo)
- Record: `FcmTokenRequest(@NotBlank String fcmToken)`

### 9. `seguimiento/controller/SeguimientoController.java`
- Nuevo endpoint: `POST /api/seguimiento/notificaciones/fcm-token` (auth required)

### 10. `tramites/service/TramiteFlujoService.java`
- Inyectado `FcmNotificationService`
- Después de `avanzar()`: envía notificación al cliente
- Después de `aprobarRamaParalela()` (join completo): envía notificación al cliente

## Flujo de notificaciones

```
Cliente login → solicita permiso FCM → obtiene token → envía al backend
                                                      ↓
                                          Guarda en fcm_tokens collection
                                                      ↓
Responsable avanza trámite → TramiteFlujoService.avanzar()
                                                      ↓
                                    FcmNotificationService.enviarNotificacion()
                                                      ↓
                              Firebase Cloud Messaging → dispositivo cliente
                                                      ↓
                            Notificación local en foreground / push en background
```

## Para activar en desarrollo local

1. Copiar `backend/firebase-credentials.json` a una ruta accesible
2. Ejecutar backend con: `FIREBASE_CREDENTIALS_PATH=/ruta/firebase-credentials.json`
3. En Docker: `FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json` + volume mount

## Pendiente

- Notificaciones cuando planificador asigna política
- Notificaciones cuando trámite es completado
- Deep linking: al tocar notificación, abrir detalle del trámite
- Sonido personalizado para notificaciones
