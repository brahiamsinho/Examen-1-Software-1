import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:tramites_cliente/features/auth/login_screen.dart';
import 'package:tramites_cliente/features/home/home_screen.dart';

String? _token;
String _nombres = '';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const TramitesClienteApp());
}

final _router = GoRouter(
  initialLocation: '/login',
  redirect: (context, state) {
    final loggedIn = _token != null && _token!.isNotEmpty;
    final loggingIn = state.matchedLocation == '/login';
    if (!loggedIn && !loggingIn) return '/login';
    if (loggedIn && loggingIn) return '/';
    return null;
  },
  routes: [
    GoRoute(
      path: '/login',
      builder: (context, state) => LoginScreen(
        onLoggedIn: (token, nombres) {
          _token = token;
          _nombres = nombres;
          GoRouter.of(context).go('/');
        },
      ),
    ),
    GoRoute(
      path: '/',
      builder: (context, state) => HomeScreen(
        nombres: _nombres.isEmpty ? 'Cliente' : _nombres,
        onLogout: () {
          _token = null;
          _nombres = '';
          GoRouter.of(context).go('/login');
        },
      ),
    ),
  ],
);

class TramitesClienteApp extends StatelessWidget {
  const TramitesClienteApp({super.key});

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
