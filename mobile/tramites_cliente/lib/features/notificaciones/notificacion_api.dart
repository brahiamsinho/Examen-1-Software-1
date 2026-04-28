import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:tramites_cliente/core/api_config.dart';

class NotificacionApi {
  NotificacionApi({http.Client? client}) : _client = client ?? http.Client();

  final http.Client _client;

  Future<void> registrarFcmToken({
    required String accessToken,
    required String fcmToken,
  }) async {
    final uri = Uri.parse('$kApiBaseUrl/api/seguimiento/notificaciones/fcm-token');
    await _client.post(
      uri,
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'Authorization': 'Bearer $accessToken',
      },
      body: jsonEncode({'fcmToken': fcmToken}),
    );
  }
}
