import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:tramites_cliente/core/api_config.dart';

class InicioTramiteResult {
  InicioTramiteResult({
    required this.tramiteId,
    required this.codigoTramite,
    required this.documentoId,
    required this.politicaId,
    required this.nodoIngresoId,
    required this.nodoIngresoNombre,
    required this.rutaArchivoGuardada,
  });

  final String tramiteId;
  final String codigoTramite;
  final String documentoId;
  final String? politicaId;
  final String nodoIngresoId;
  final String? nodoIngresoNombre;
  final String? rutaArchivoGuardada;
}

class ClienteApiException implements Exception {
  ClienteApiException(this.message, {this.statusCode});
  final String message;
  final int? statusCode;
}

class ClienteApi {
  ClienteApi({http.Client? client}) : _client = client ?? http.Client();

  final http.Client _client;

  /// Inicia trámite con archivo; entra por atención al cliente.
  Future<InicioTramiteResult> iniciarTramiteConDocumento({
    required String accessToken,
    required String filePath,
    String? asunto,
  }) async {
    final uri = Uri.parse('$kApiBaseUrl/api/cliente/tramites/inicio-con-documento');
    final request = http.MultipartRequest('POST', uri);
    request.headers['Authorization'] = 'Bearer $accessToken';
    if (asunto != null && asunto.trim().isNotEmpty) {
      request.fields['asunto'] = asunto.trim();
    }
    final mf = await http.MultipartFile.fromPath('archivo', filePath);
    request.files.add(mf);
    final streamed = await _client.send(request);
    final body = await streamed.stream.toBytes();
    if (streamed.statusCode < 200 || streamed.statusCode >= 300) {
      String? msg;
      try {
        final map = jsonDecode(utf8.decode(body)) as Map<String, dynamic>?;
        final m = map?['message'];
        if (m is String) msg = m;
      } catch (_) {}
      throw ClienteApiException(
        msg ?? 'Error al enviar el documento (${streamed.statusCode})',
        statusCode: streamed.statusCode,
      );
    }
    final map = jsonDecode(utf8.decode(body)) as Map<String, dynamic>;
    return InicioTramiteResult(
      tramiteId: map['tramiteId'] as String,
      codigoTramite: map['codigoTramite'] as String? ?? '',
      documentoId: map['documentoId'] as String? ?? '',
      politicaId: map['politicaId'] as String?,
      nodoIngresoId: map['nodoIngresoId'] as String? ?? '',
      nodoIngresoNombre: map['nodoIngresoNombre'] as String?,
      rutaArchivoGuardada: map['rutaArchivoGuardada'] as String?,
    );
  }

}
