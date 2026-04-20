import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:tramites_cliente/core/api_config.dart';

class AuthRepository {
  AuthRepository({http.Client? client}) : _client = client ?? http.Client();

  final http.Client _client;

  Future<LoginResult> login({
    required String correo,
    required String contrasena,
  }) async {
    final uri = Uri.parse('$kApiBaseUrl/api/auth/login');
    final res = await _client.post(
      uri,
      headers: {'Content-Type': 'application/json; charset=utf-8'},
      body: jsonEncode({
        'correo': correo,
        'contrasena': contrasena,
        'portalRol': 'CLIENTE',
      }),
    );
    if (res.statusCode < 200 || res.statusCode >= 300) {
      String msg = 'Error ${res.statusCode}';
      try {
        final body = jsonDecode(res.body) as Map<String, dynamic>;
        final m = body['message'];
        if (m is String) msg = m;
      } catch (_) {}
      throw AuthException(msg);
    }
    final map = jsonDecode(res.body) as Map<String, dynamic>;
    final token = map['accessToken'] as String?;
    if (token == null || token.isEmpty) {
      throw AuthException('Respuesta inválida del servidor.');
    }
    return LoginResult(
      accessToken: token,
      rolCodigo: map['rolCodigo'] as String? ?? 'CLIENTE',
      nombres: map['nombres'] as String? ?? '',
    );
  }
}

class LoginResult {
  LoginResult({
    required this.accessToken,
    required this.rolCodigo,
    required this.nombres,
  });

  final String accessToken;
  final String rolCodigo;
  final String nombres;
}

class AuthException implements Exception {
  AuthException(this.message);
  final String message;
}
