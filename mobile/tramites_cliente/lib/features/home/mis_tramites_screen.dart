import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:tramites_cliente/features/cliente/cliente_api.dart';

class MisTramitesScreen extends StatefulWidget {
  const MisTramitesScreen({
    super.key,
    required this.accessToken,
  });

  final String accessToken;

  @override
  State<MisTramitesScreen> createState() => _MisTramitesScreenState();
}

class _MisTramitesScreenState extends State<MisTramitesScreen> {
  final _api = ClienteApi();
  TramiteClientePage? _page;
  bool _loading = true;
  String? _error;
  int _pageNum = 0;

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
      final p = await _api.listarMisTramites(
        accessToken: widget.accessToken,
        page: _pageNum,
        size: 20,
      );
      if (!mounted) return;
      setState(() {
        _page = p;
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

  Future<void> _abrirDetalle(String id) async {
    try {
      final d = await _api.obtenerDetalleTramite(
        accessToken: widget.accessToken,
        tramiteId: id,
      );
      if (!mounted) return;
      await showModalBottomSheet<void>(
        context: context,
        isScrollControlled: true,
        showDragHandle: true,
        builder: (ctx) => _DetalleSheet(detalle: d),
      );
    } on ClienteApiException catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mis trámites'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/'),
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
                : ListView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.all(16),
                    children: [
                      if (_page != null && _page!.totalElements > 0)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 12),
                          child: Text(
                            '${_page!.totalElements} expediente(s)',
                            style: Theme.of(context).textTheme.titleSmall,
                          ),
                        ),
                      if (_page == null || _page!.content.isEmpty)
                        const Padding(
                          padding: EdgeInsets.only(top: 48),
                          child: Center(child: Text('Todavía no tenés trámites registrados.')),
                        )
                      else
                        ..._page!.content.map(
                          (t) => Card(
                            margin: const EdgeInsets.only(bottom: 10),
                            child: ListTile(
                              title: Text(t.codigo, style: const TextStyle(fontWeight: FontWeight.w600)),
                              subtitle: Text(
                                '${t.asunto}\nEstado: ${t.estado}'
                                '${t.politicaId != null ? '' : '\n(Sin política asignada aún)'}',
                              ),
                              isThreeLine: true,
                              trailing: const Icon(Icons.chevron_right),
                              onTap: () => _abrirDetalle(t.id),
                            ),
                          ),
                        ),
                      if (_page != null && _pageNum < _page!.totalPages - 1)
                        Padding(
                          padding: const EdgeInsets.only(top: 8),
                          child: OutlinedButton(
                            onPressed: () {
                              setState(() => _pageNum += 1);
                              _cargar();
                            },
                            child: const Text('Siguiente página'),
                          ),
                        ),
                    ],
                  ),
      ),
    );
  }
}

class _DetalleSheet extends StatelessWidget {
  const _DetalleSheet({required this.detalle});

  final ClienteTramiteDetalle detalle;

  @override
  Widget build(BuildContext context) {
    final t = detalle.tramite;
    return DraggableScrollableSheet(
      expand: false,
      initialChildSize: 0.65,
      minChildSize: 0.35,
      maxChildSize: 0.92,
      builder: (context, scroll) {
        return ListView(
          controller: scroll,
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
          children: [
            Text(t.codigo, style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 8),
            Text('Asunto: ${t.asunto}'),
            Text('Estado: ${t.estado}'),
            Text('Nodo actual: ${t.nodoActualId ?? '—'}'),
            Text('Área actual: ${t.areaActualId ?? '—'}'),
            Text('Política: ${t.politicaId ?? 'pendiente de planificación'}'),
            const SizedBox(height: 16),
            Text('Recorridos', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            if (detalle.recorridos.isEmpty)
              const Text('Sin movimientos registrados.')
            else
              ...detalle.recorridos.map(
                (r) => Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        r.fechaEntrada != null ? r.fechaEntrada! : '—',
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
        );
      },
    );
  }
}
