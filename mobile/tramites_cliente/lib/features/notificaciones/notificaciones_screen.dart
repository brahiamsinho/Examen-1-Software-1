import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:tramites_cliente/features/notificaciones/notificacion_cliente_api.dart';

class NotificacionesScreen extends StatefulWidget {
  const NotificacionesScreen({super.key, required this.accessToken});

  final String accessToken;

  @override
  State<NotificacionesScreen> createState() => _NotificacionesScreenState();
}

class _NotificacionesScreenState extends State<NotificacionesScreen> {
  final _api = NotificacionClienteApi();
  NotificacionClientePage? _page;
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
      final p = await _api.listar(accessToken: widget.accessToken, page: _pageNum, size: 20);
      if (!mounted) return;
      setState(() {
        _page = p;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  Future<void> _marcarYAbrir(NotificacionClienteItem n) async {
    if (!n.leida) {
      try {
        await _api.marcarLeida(accessToken: widget.accessToken, notificacionId: n.id);
      } catch (_) {}
    }
    if (!mounted) return;
    if (n.tramiteId.isNotEmpty) {
      context.push('/tramite/${n.tramiteId}');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Notificaciones'),
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
                      if (_page == null || _page!.items.isEmpty)
                        const Padding(
                          padding: EdgeInsets.only(top: 48),
                          child: Center(child: Text('No hay notificaciones.')),
                        )
                      else
                        ..._page!.items.map(
                          (n) => Card(
                            margin: const EdgeInsets.only(bottom: 10),
                            child: ListTile(
                              title: Text(
                                n.tipo,
                                style: TextStyle(
                                  fontWeight: FontWeight.w600,
                                  color: n.leida ? Colors.grey : null,
                                ),
                              ),
                              subtitle: Text(
                                n.mensaje,
                                maxLines: 4,
                                overflow: TextOverflow.ellipsis,
                              ),
                              trailing: n.tramiteId.isNotEmpty
                                  ? const Icon(Icons.chevron_right)
                                  : null,
                              onTap: n.tramiteId.isNotEmpty ? () => _marcarYAbrir(n) : null,
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
