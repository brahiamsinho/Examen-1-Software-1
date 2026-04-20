# tramites_cliente (Flutter)

App para el **Cliente / usuario**: seguimiento de trámite. Usa el mismo endpoint de login que Angular:

`POST /api/auth/login` con cuerpo JSON:

```json
{
  "correo": "cliente@tramites.local",
  "contrasena": "demo123",
  "portalRol": "CLIENTE"
}
```

## Base URL del backend

| Entorno | URL sugerida |
|--------|----------------|
| Android emulator | `http://10.0.2.2:8080` |
| iOS simulator | `http://localhost:8080` |
| Dispositivo físico (misma red) | `http://IP_DE_TU_PC:8080` (Spring expuesto al host) |

Editá `lib/core/api_config.dart` o pasá `--dart-define=API_BASE_URL=...` al ejecutar.

## Comandos

```bash
cd mobile/tramites_cliente
flutter pub get
flutter run
```

Usuario semilla (si `APP_DEV_AUTH_SEED_ENABLED=true` en Spring): `cliente@tramites.local` / `demo123`.

## Nota sobre `script.db`

Cuando agregues `script.db` al repositorio, alineá colecciones y roles con ese esquema; los códigos de rol actuales son: `ADMINISTRADOR`, `DISENADOR_POLITICAS`, `RESPONSABLE_AREA`, `CLIENTE`.
