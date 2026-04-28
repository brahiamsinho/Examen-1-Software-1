import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:tramites_cliente/features/cliente/cliente_api.dart';

class TramiteDetalleScreen extends StatefulWidget {
  const TramiteDetalleScreen({
    super.key,
    required this.accessToken,
    required this.tramiteId,
  });

  final String accessToken;
  final String tramiteId;

  @override
  State<TramiteDetalleScreen> createState() => _TramiteDetalleScreenState();
}

class _TramiteDetalleScreenState extends State<TramiteDetalleScreen> {
  final _api = ClienteApi();
  ClienteTramiteDetalle? _detalle;
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _cargar();
  }

  Future<void> _cargar() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final d = await _api.obtenerDetalleTramite(
        accessToken: widget.accessToken,
        tramiteId: widget.tramiteId,
      );
      if (!mounted) return;
      setState(() {
        _detalle = d;
        _loading = false;
      });
    } on ClienteApiException catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.message;
        _loading = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _error = 'Error de red o servidor.';
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Detalle del trámite'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.canPop() ? context.pop() : context.go('/mis-tramites'),
        ),
      ),
      body: RefreshIndicator(
        onRefresh: _cargar,
        child: _loading
            ? ListView(
                physics: const AlwaysScrollableScrollPhysics(),
                children: const [
                  SizedBox(height: 120),
                  Center(child: CircularProgressIndicator()),
                ],
              )
            : _error != null
                ? ListView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.all(20),
                    children: [
                      Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
                      const SizedBox(height: 16),
                      FilledButton(onPressed: _cargar, child: const Text('Reintentar')),
                    ],
                  )
                : _detalle == null
                    ? const SizedBox.shrink()
                    : ListView(
                        padding: const EdgeInsets.all(20),
                        children: [
                          Text(_detalle!.tramite.codigo, style: Theme.of(context).textTheme.titleLarge),
                          const SizedBox(height: 8),
                          Text('Asunto: ${_detalle!.tramite.asunto}'),
                          Text('Estado: ${_detalle!.tramite.estado}'),
                          Text('Nodo actual: ${_detalle!.tramite.nodoActualId ?? '—'}'),
                          Text('Área actual: ${_detalle!.tramite.areaActualId ?? '—'}'),
                          Text('Política: ${_detalle!.tramite.politicaId ?? 'pendiente de planificación'}'),
                          const SizedBox(height: 16),
                          Text('Recorridos', style: Theme.of(context).textTheme.titleMedium),
                          const SizedBox(height: 8),
                          if (_detalle!.recorridos.isEmpty)
                            const Text('Sin movimientos registrados.')
                          else
                            ..._detalle!.recorridos.map(
                              (r) => Padding(
                                padding: const EdgeInsets.only(bottom: 10),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      r.fechaEntrada ?? '—',
                                      style: Theme.of(context).textTheme.labelSmall,
                                    ),
                                    Text('Nodo: ${r.nodoId ?? '—'} · ${r.estado ?? ''}'),
                                    if (r.observacion != null && r.observacion!.isNotEmpty)
                                      Text(r.observacion!, style: Theme.of(context).textTheme.bodySmall),
                                  ],
                                ),
                              ),
                            ),
                        ],
                      ),
      ),
    );
  }
}
