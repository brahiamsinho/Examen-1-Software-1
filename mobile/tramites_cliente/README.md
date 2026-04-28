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
| Dispositivo físico (misma red) | `http://IP_DE_TU_PC:8080` (Compose publica `backend` en `BACKEND_HOST_PORT`, por defecto 8080) |

## Configuración por entorno

### Opción 1: Archivo `.env` (recomendado para debug)

1. Copiá `.env.example` → `.env`:
   ```bash
   cp .env.example .env
   ```
2. Editá `.env` y cambiá `API_BASE_URL`:
   ```
   API_BASE_URL=http://192.168.1.100:8080
   ```
3. Ejecutá normal: `flutter run`

> `.env` está en `.gitignore`, no se commitea.

### Opción 2: `--dart-define` (build-time)

```bash
flutter run --dart-define=API_BASE_URL=http://192.168.1.100:8080
```

### Opción 3: Default

Si no hay `.env` ni `--dart-define`, usa `http://10.0.2.2:8080` (Android emulator).

## Debug inalámbrico con dispositivo físico

### Paso 1: Encontrá la IP de tu PC

**Windows:**
```powershell
ipconfig
```
Buscá la IPv4 de tu adaptador WiFi (ej: `192.168.1.100`).

### Paso 2: Configurá `.env`

```
API_BASE_URL=http://192.168.1.100:8080
```

### Paso 3: Asegurate que Spring Boot escuche en todas las interfaces

En `backend/src/main/resources/application.yml`:
```yaml
server:
  address: 0.0.0.0
```

O ejecutá con:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.address=0.0.0.0"
```

### Paso 4: Conectá tu teléfono por WiFi debugging

1. Activá **Developer Options** en tu teléfono (7 taps en Build Number).
2. Activá **Wireless debugging**.
3. En Android Studio → **Device Manager** → **Pair using QR code** o **Pair using pairing code**.
4. Una vez conectado, tu teléfono aparece como dispositivo de debug.

### Paso 5: Ejecutá

```bash
cd mobile/tramites_cliente
flutter pub get
flutter run
```

El teléfono y tu PC deben estar en la **misma red WiFi**.

### Firewall de Windows

Si no conecta, permití el puerto 8080 en el firewall:
```powershell
New-NetFirewallRule -DisplayName "Spring Boot 8080" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow
```

## Push / deep link al trámite

El backend envía FCM con `data.tramiteId`. La app navega a `/tramite/:id` cuando:

- la app estaba en **segundo plano** y el usuario abre el push (`onMessageOpenedApp`);
- la app arrancó **desde cero** tras tocar el push (`getInitialMessage`);
- en **foreground**, al tocar la notificación **local** (mismo `tramiteId` en `payload`).

Si aún no hay sesión (cold start sin token), el id queda en cola hasta restaurar sesión desde almacenamiento o completar login.

## Comandos

```bash
cd mobile/tramites_cliente
flutter pub get
flutter run
```

Usuario semilla (si `APP_DEV_AUTH_SEED_ENABLED=true` en Spring): `cliente@tramites.local` / `demo123`.

## Nota sobre `script.db`

Cuando agregues `script.db` al repositorio, alineá colecciones y roles con ese esquema; los códigos de rol actuales son: `ADMINISTRADOR`, `DISENADOR_POLITICAS`, `RESPONSABLE_AREA`, `CLIENTE`.
