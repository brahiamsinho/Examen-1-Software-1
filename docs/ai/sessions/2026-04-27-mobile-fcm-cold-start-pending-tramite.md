# Sesión: Flutter — cold start FCM + cola deep link

## Qué se hizo

- `FirebaseConfig.deliverColdStartTramiteDeepLinkIfAny()`: `getInitialMessage()` y mismo flujo que `onMessageOpenedApp`.
- Notificación local (foreground): `payload` = `tramiteId` + `onDidReceiveNotificationResponse` para navegar al tocar.
- `main.dart`: `_pendingTramiteId`, `_handleTramiteDeepLink`, `_tryNavigatePendingTramite` tras `_loadSession` y `_onLoggedIn` (post-frame); limpieza en logout.

## Prueba manual sugerida

App terminada → tap en push con `data.tramiteId`; con token guardado debe abrir detalle; sin token, login y luego detalle.
