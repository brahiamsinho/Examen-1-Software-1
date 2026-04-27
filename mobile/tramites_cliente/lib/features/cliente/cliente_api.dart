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

/// Resumen alineado a `TramiteResponse` del backend.
class TramiteClienteResumen {
  TramiteClienteResumen({
    required this.id,
    required this.codigo,
    required this.asunto,
    required this.estado,
    this.politicaId,
    this.nodoActualId,
    this.areaActualId,
  });

  final String id;
  final String codigo;
  final String asunto;
  final String estado;
  final String? politicaId;
  final String? nodoActualId;
  final String? areaActualId;

  factory TramiteClienteResumen.fromJson(Map<String, dynamic> m) {
    return TramiteClienteResumen(
      id: m['id'] as String? ?? '',
      codigo: m['codigo'] as String? ?? '',
      asunto: m['asunto'] as String? ?? '',
      estado: m['estado'] as String? ?? '',
      politicaId: m['politicaId'] as String?,
      nodoActualId: m['nodoActualId'] as String?,
      areaActualId: m['areaActualId'] as String?,
    );
  }
}

class TramiteClientePage {
  TramiteClientePage({
    required this.content,
    required this.totalElements,
    required this.totalPages,
    required this.number,
  });

  final List<TramiteClienteResumen> content;
  final int totalElements;
  final int totalPages;
  final int number;

  factory TramiteClientePage.fromJson(Map<String, dynamic> m) {
    final raw = m['content'];
    final list = <TramiteClienteResumen>[];
    if (raw is List) {
      for (final e in raw) {
        if (e is Map<String, dynamic>) {
          list.add(TramiteClienteResumen.fromJson(e));
        }
      }
    }
    return TramiteClientePage(
      content: list,
      totalElements: (m['totalElements'] as num?)?.toInt() ?? list.length,
      totalPages: (m['totalPages'] as num?)?.toInt() ?? 1,
      number: (m['number'] as num?)?.toInt() ?? 0,
    );
  }
}

class RecorridoCliente {
  RecorridoCliente({
    required this.nodoId,
    this.areaId,
    this.estado,
    this.observacion,
    this.fechaEntrada,
  });

  final String? nodoId;
  final String? areaId;
  final String? estado;
  final String? observacion;
  final String? fechaEntrada;

  factory RecorridoCliente.fromJson(Map<String, dynamic> m) {
    return RecorridoCliente(
      nodoId: m['nodoId'] as String?,
      areaId: m['areaId'] as String?,
      estado: m['estado'] as String?,
      observacion: m['observacion'] as String?,
      fechaEntrada: m['fechaEntrada'] as String?,
    );
  }
}

class ClienteTramiteDetalle {
  ClienteTramiteDetalle({required this.tramite, required this.recorridos});

  final TramiteClienteResumen tramite;
  final List<RecorridoCliente> recorridos;

  factory ClienteTramiteDetalle.fromJson(Map<String, dynamic> m) {
    final t = m['tramite'];
    final tramite = t is Map<String, dynamic>
        ? TramiteClienteResumen.fromJson(t)
        : TramiteClienteResumen(id: '', codigo: '', asunto: '', estado: '');
    final raw = m['recorridos'];
    final recs = <RecorridoCliente>[];
    if (raw is List) {
      for (final e in raw) {
        if (e is Map<String, dynamic>) {
          recs.add(RecorridoCliente.fromJson(e));
        }
      }
    }
    return ClienteTramiteDetalle(tramite: tramite, recorridos: recs);
  }
}

class ClienteApi {
  ClienteApi({http.Client? client}) : _client = client ?? http.Client();

  final http.Client _client;

  Future<TramiteClientePage> listarMisTramites({
    required String accessToken,
    int page = 0,
    int size = 20,
  }) async {
    final uri = Uri.parse('$kApiBaseUrl/api/cliente/tramites').replace(
      queryParameters: {'page': '$page', 'size': '$size'},
    );
    final r = await _client.get(uri, headers: {'Authorization': 'Bearer $accessToken'});
    if (r.statusCode < 200 || r.statusCode >= 300) {
      throw ClienteApiException('No se pudo cargar el listado (${r.statusCode})', statusCode: r.statusCode);
    }
    final map = jsonDecode(r.body) as Map<String, dynamic>;
    return TramiteClientePage.fromJson(map);
  }

  Future<ClienteTramiteDetalle> obtenerDetalleTramite({
    required String accessToken,
    required String tramiteId,
  }) async {
    final uri = Uri.parse('$kApiBaseUrl/api/cliente/tramites/${Uri.encodeComponent(tramiteId)}');
    final r = await _client.get(uri, headers: {'Authorization': 'Bearer $accessToken'});
    if (r.statusCode < 200 || r.statusCode >= 300) {
      String? msg;
      try {
        final map = jsonDecode(r.body) as Map<String, dynamic>?;
        final m = map?['message'];
        if (m is String) msg = m;
      } catch (_) {}
      throw ClienteApiException(msg ?? 'Trámite no encontrado (${r.statusCode})', statusCode: r.statusCode);
    }
    final map = jsonDecode(r.body) as Map<String, dynamic>;
    return ClienteTramiteDetalle.fromJson(map);
  }

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
