import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:go_router/go_router.dart';
import 'package:tramites_cliente/core/firebase_config.dart';
import 'package:tramites_cliente/core/secure_storage.dart';
import 'package:tramites_cliente/features/auth/login_screen.dart';
import 'package:tramites_cliente/features/home/home_screen.dart';
import 'package:tramites_cliente/features/home/mis_tramites_screen.dart';
import 'package:tramites_cliente/features/home/tramite_detalle_screen.dart';
import 'package:tramites_cliente/features/notificaciones/notificaciones_screen.dart';
import 'package:tramites_cliente/features/profile/profile_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await dotenv.load(fileName: '.env', isOptional: true);
  await FirebaseConfig.initialize();
  runApp(const TramitesClienteApp());
}

class SessionData {
  SessionData({
    required this.token,
    required this.nombres,
    this.apellidos,
    required this.correo,
    required this.rol,
  });

  final String token;
  final String nombres;
  final String? apellidos;
  final String correo;
  final String rol;
}

class TramitesClienteApp extends StatefulWidget {
  const TramitesClienteApp({super.key});

  @override
  State<TramitesClienteApp> createState() => _TramitesClienteAppState();
}

class _TramitesClienteAppState extends State<TramitesClienteApp> {
  final _storage = SecureStorage();
  SessionData? _session;
  /// Deep link desde FCM / notificación local antes de tener sesión restaurada.
  String? _pendingTramiteId;
  late final GoRouter _router;

  @override
  void initState() {
    super.initState();
    _router = GoRouter(
      initialLocation: '/login',
      redirect: (context, state) {
        final loggedIn = _session != null;
        final loggingIn = state.matchedLocation == '/login';
        if (!loggedIn && !loggingIn) return '/login';
        if (loggedIn && loggingIn) return '/';
        return null;
      },
      routes: [
        GoRoute(
          path: '/login',
          builder: (context, state) => LoginScreen(onLoggedIn: _onLoggedIn),
        ),
        GoRoute(
          path: '/',
          builder: (context, state) => HomeScreen(
            nombres: _session!.nombres,
            accessToken: _session!.token,
            onLogout: _onLogout,
          ),
        ),
        GoRoute(
          path: '/mis-tramites',
          builder: (context, state) => MisTramitesScreen(accessToken: _session!.token),
        ),
        GoRoute(
          path: '/perfil',
          builder: (context, state) => ProfileScreen(
            nombres: _session!.nombres,
            apellidos: _session!.apellidos,
            correo: _session!.correo,
            rol: _session!.rol,
            onLogout: _onLogout,
          ),
        ),
        GoRoute(
          path: '/notificaciones',
          builder: (context, state) => NotificacionesScreen(accessToken: _session!.token),
        ),
        GoRoute(
          path: '/tramite/:tramiteId',
          builder: (context, state) => TramiteDetalleScreen(
            accessToken: _session!.token,
            tramiteId: state.pathParameters['tramiteId']!,
          ),
        ),
      ],
    );
    FirebaseConfig.registerTramiteDeepLinkHandler(_handleTramiteDeepLink);
    FirebaseConfig.deliverColdStartTramiteDeepLinkIfAny();
    _loadSession();
  }

  void _handleTramiteDeepLink(String tid) {
    if (tid.isEmpty || !mounted) return;
    if (_session == null) {
      _pendingTramiteId = tid;
      return;
    }
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || _session == null) return;
      _router.go('/tramite/$tid');
    });
  }

  void _tryNavigatePendingTramite() {
    final tid = _pendingTramiteId;
    if (tid == null || tid.isEmpty || _session == null || !mounted) return;
    _pendingTramiteId = null;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || _session == null) return;
      _router.go('/tramite/$tid');
    });
  }

  Future<void> _loadSession() async {
    final token = await _storage.getToken();
    if (token != null && token.isNotEmpty) {
      final info = await _storage.getUserInfo();
      if (mounted) {
        setState(() {
          _session = SessionData(
            token: token,
            nombres: info['nombres'] ?? 'Cliente',
            apellidos: info['apellidos'],
            correo: info['correo'] ?? '',
            rol: info['rol'] ?? 'CLIENTE',
          );
        });
        _router.go('/');
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) _tryNavigatePendingTramite();
        });
      }
    }
  }

  Future<void> _onLoggedIn(String token, String nombres, String correo, String rol, String? apellidos) async {
    await _storage.saveSession(
      token: token,
      nombres: nombres,
      apellidos: apellidos,
      correo: correo,
      rol: rol,
    );
    if (mounted) {
      setState(() {
        _session = SessionData(
          token: token,
          nombres: nombres,
          apellidos: apellidos,
          correo: correo,
          rol: rol,
        );
      });
      _router.go('/');
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _tryNavigatePendingTramite();
      });
    }
  }

  Future<void> _onLogout() async {
    await _storage.clearSession();
    if (mounted) {
      setState(() {
        _session = null;
        _pendingTramiteId = null;
      });
      _router.go('/login');
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Trámites — Cliente',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF0F172A),
          brightness: Brightness.light,
          surface: const Color(0xFFFAFAFA),
        ),
        useMaterial3: true,
        inputDecorationTheme: InputDecorationTheme(
          filled: false,
          alignLabelWithHint: true,
          contentPadding: const EdgeInsets.symmetric(vertical: 14),
          border: UnderlineInputBorder(
            borderSide: BorderSide(color: Colors.grey.shade400),
          ),
          enabledBorder: UnderlineInputBorder(
            borderSide: BorderSide(color: Colors.grey.shade400),
          ),
          focusedBorder: const UnderlineInputBorder(
            borderSide: BorderSide(color: Color(0xFF0F172A), width: 1.5),
          ),
          errorBorder: UnderlineInputBorder(
            borderSide: BorderSide(color: Colors.red.shade400),
          ),
          focusedErrorBorder: UnderlineInputBorder(
            borderSide: BorderSide(color: Colors.red.shade600, width: 1.5),
          ),
        ),
      ),
      routerConfig: _router,
    );
  }
}
