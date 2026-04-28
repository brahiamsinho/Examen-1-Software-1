import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:tramites_cliente/core/api_config.dart';

class NotificacionClienteItem {
  NotificacionClienteItem({
    required this.id,
    required this.tramiteId,
    required this.mensaje,
    required this.tipo,
    required this.leida,
    this.fechaEnvio,
  });

  final String id;
  final String tramiteId;
  final String mensaje;
  final String tipo;
  final bool leida;
  final String? fechaEnvio;

  factory NotificacionClienteItem.fromJson(Map<String, dynamic> m) {
    return NotificacionClienteItem(
      id: m['id'] as String? ?? '',
      tramiteId: m['tramiteId'] as String? ?? '',
      mensaje: m['mensaje'] as String? ?? '',
      tipo: m['tipo'] as String? ?? '',
      leida: m['leida'] as bool? ?? false,
      fechaEnvio: m['fechaEnvio'] as String?,
    );
  }
}

class NotificacionClientePage {
  NotificacionClientePage({
    required this.items,
    required this.totalElements,
    required this.totalPages,
    required this.number,
  });

  final List<NotificacionClienteItem> items;
  final int totalElements;
  final int totalPages;
  final int number;

  factory NotificacionClientePage.fromJson(Map<String, dynamic> m) {
    final raw = m['content'];
    final list = <NotificacionClienteItem>[];
    if (raw is List) {
      for (final e in raw) {
        if (e is Map<String, dynamic>) {
          list.add(NotificacionClienteItem.fromJson(e));
        }
      }
    }
    return NotificacionClientePage(
      items: list,
      totalElements: (m['totalElements'] as num?)?.toInt() ?? list.length,
      totalPages: (m['totalPages'] as num?)?.toInt() ?? 1,
      number: (m['number'] as num?)?.toInt() ?? 0,
    );
  }
}

class NotificacionClienteApi {
  NotificacionClienteApi({http.Client? client}) : _client = client ?? http.Client();

  final http.Client _client;

  Future<NotificacionClientePage> listar({
    required String accessToken,
    int page = 0,
    int size = 20,
  }) async {
    final uri = Uri.parse('$kApiBaseUrl/api/cliente/seguimiento/notificaciones').replace(
      queryParameters: {'page': '$page', 'size': '$size'},
    );
    final r = await _client.get(uri, headers: {'Authorization': 'Bearer $accessToken'});
    if (r.statusCode < 200 || r.statusCode >= 300) {
      throw Exception('No se pudieron cargar las notificaciones (${r.statusCode})');
    }
    final map = jsonDecode(r.body) as Map<String, dynamic>;
    return NotificacionClientePage.fromJson(map);
  }

  Future<void> marcarLeida({
    required String accessToken,
    required String notificacionId,
  }) async {
    final uri = Uri.parse(
      '$kApiBaseUrl/api/cliente/seguimiento/notificaciones/${Uri.encodeComponent(notificacionId)}/leida',
    );
    final r = await _client.patch(uri, headers: {'Authorization': 'Bearer $accessToken'});
    if (r.statusCode < 200 || r.statusCode >= 300) {
      throw Exception('No se pudo marcar como leída (${r.statusCode})');
    }
  }
}
