import 'package:flutter/foundation.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

/// Base del API Spring **sin** barra final (rutas tipo `/api/...` en la raíz del servicio).
///
/// Orden de prioridad:
/// 1. `--dart-define=API_BASE_URL=...` (build-time, para CI/producción)
/// 2. `.env` → `API_BASE_URL` (asset en debug; **obligatorio en dispositivo físico**)
/// 3. Default por plataforma si no hay `.env`:
///    - Android: `http://10.0.2.2:8080` (solo emulador → host)
///    - iOS: `http://127.0.0.1:8080` (simulador → host)
///    - resto: `http://127.0.0.1:8080`
///
/// **Dispositivo físico:** `10.0.2.2` no existe en el teléfono; usá la IPv4 LAN de tu PC
/// (misma WiFi) y el puerto mapeado por Docker (`BACKEND_HOST_PORT`, por defecto 8080).
String get kApiBaseUrl {
  const fromDefine = String.fromEnvironment('API_BASE_URL');
  if (fromDefine.isNotEmpty) return fromDefine;

  final fromEnv = dotenv.env['API_BASE_URL'];
  if (fromEnv != null && fromEnv.isNotEmpty) return fromEnv;

  if (defaultTargetPlatform == TargetPlatform.android) {
    return 'http://10.0.2.2:8080';
  }
  if (defaultTargetPlatform == TargetPlatform.iOS) {
    return 'http://127.0.0.1:8080';
  }
  return 'http://127.0.0.1:8080';
}
