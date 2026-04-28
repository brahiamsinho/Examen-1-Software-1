# Sesión: Fase A — cierre de flujo y notificaciones al cliente

## Objetivo

Cerrar el gap entre el flujo real (ingreso → planificación → responsables → fin) y lo que el **cliente** percibe: estado terminal explícito, notificación al asignar política, historial in-app + push con `tramiteId`, y UX móvil mínima (lista + detalle + deep link desde FCM en background).

## Cambios técnicos

- `PoliticaNodoTerminalResolver`: `FIN` → resultado aprobado; `RECHAZO` → rechazo.
- `ClienteTramiteEventoNotificacionService`: FCM + persistencia `notificaciones`.
- `TramiteFlujoService`: `assertFlujoNoFinalizado`, `aplicarEstadoTerminalYNotificarCliente` tras `avanzar` y al completar join paralelo.
- `TramitesService.asignarPoliticaDesdeIngreso`: notificación post-asignación.
- `PoliticasDominioService` + Angular: tipo de nodo `RECHAZO`.
- `ClienteSeguimientoController`: API cliente segura por JWT.
- `SeguimientoService`: tipo `POLITICA_ASIGNADA`; `marcarNotificacionLeidaParaUsuario`.
- Flutter: pantallas, API, router, `FirebaseConfig.registerTramiteDeepLinkHandler` + `onMessageOpenedApp`.

## Pendiente cercano

- Cold start: `getInitialMessage` + sesión async en `SecureStorage`.
- Badge contador no leídas en AppBar.
