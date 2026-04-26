import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:tramites_cliente/features/cliente/cliente_api.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({
    super.key,
    required this.nombres,
    required this.accessToken,
    required this.onLogout,
  });

  final String nombres;
  final String accessToken;
  final VoidCallback onLogout;

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _api = ClienteApi();
  final _asunto = TextEditingController();
  bool _sending = false;
  String? _error;
  String? _success;
  String? _rutaArchivo;
  String? _nombreArchivo;

  @override
  void dispose() {
    _asunto.dispose();
    super.dispose();
  }

  Future<void> _elegirArchivo() async {
    setState(() {
      _error = null;
      _success = null;
    });
    final r = await FilePicker.platform.pickFiles(
      type: FileType.any,
      withData: false,
    );
    if (r == null || r.files.isEmpty) return;
    final f = r.files.first;
    final p = f.path;
    if (p == null || p.isEmpty) {
      if (!mounted) return;
      setState(() {
        _error = 'No se pudo leer la ruta del archivo en este dispositivo.';
      });
      return;
    }
    if (!mounted) return;
    setState(() {
      _rutaArchivo = p;
      _nombreArchivo = f.name;
    });
  }

  Future<void> _enviar() async {
    if (_rutaArchivo == null) {
      setState(() {
        _error = 'Elegí un documento (PDF, imagen, etc.).';
      });
      return;
    }
    setState(() {
      _sending = true;
      _error = null;
      _success = null;
    });
    try {
      final res = await _api.iniciarTramiteConDocumento(
        accessToken: widget.accessToken,
        filePath: _rutaArchivo!,
        asunto: _asunto.text,
      );
      if (!mounted) return;
      setState(() {
        _success =
            'Trámite ${res.codigoTramite} creado. Ingreso: ${res.nodoIngresoNombre ?? res.nodoIngresoId}. Luego lo asigna planificación.';
        _sending = false;
        _rutaArchivo = null;
        _nombreArchivo = null;
      });
    } on ClienteApiException catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.message;
        _sending = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _error = 'Error al enviar el documento (red o servidor).';
        _sending = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Nuevo trámite'),
        actions: [
          TextButton(
            onPressed: widget.onLogout,
            child: const Text('Salir', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text('Hola, ${widget.nombres}', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          Text(
            'Subí un documento. El expediente ingresa primero a atención al cliente y luego planificación define la política de negocio.',
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(height: 20),
          TextField(
            controller: _asunto,
            decoration: const InputDecoration(
              labelText: 'Asunto (opcional)',
            ),
            textInputAction: TextInputAction.next,
            maxLength: 500,
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: _sending ? null : _elegirArchivo,
            icon: const Icon(Icons.attach_file),
            label: const Text('Elegir documento'),
          ),
          if (_nombreArchivo != null)
            Padding(
              padding: const EdgeInsets.only(top: 8),
              child: Text('Archivo: $_nombreArchivo', style: Theme.of(context).textTheme.bodySmall),
            ),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: _sending ? null : _enviar,
            child: _sending
                ? const SizedBox(
                    width: 22,
                    height: 22,
                    child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                  )
                : const Text('Enviar e iniciar trámite'),
          ),
          if (_error != null) ...[
            const SizedBox(height: 16),
            Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
          ],
          if (_success != null) ...[
            const SizedBox(height: 16),
            Text(_success!, style: const TextStyle(color: Color(0xFF0F172A))),
          ],
        ],
      ),
    );
  }
}
