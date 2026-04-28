import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

/// Navegación a detalle de trámite (ej. desde push con `data.tramiteId`).
typedef TramiteDeepLinkHandler = void Function(String tramiteId);

class FirebaseConfig {
  static final _messaging = FirebaseMessaging.instance;
  static final _localNotifications = FlutterLocalNotificationsPlugin();
  static TramiteDeepLinkHandler? _deepLinkHandler;

  static void registerTramiteDeepLinkHandler(TramiteDeepLinkHandler? handler) {
    _deepLinkHandler = handler;
  }

  static void _openTramiteFromMessage(RemoteMessage message) {
    final tid = message.data['tramiteId'];
    if (tid is String && tid.isNotEmpty) {
      _deepLinkHandler?.call(tid);
    }
  }

  static Future<void> initialize() async {
    await Firebase.initializeApp();

    await _localNotifications.initialize(
      const InitializationSettings(
        android: AndroidInitializationSettings('@mipmap/ic_launcher'),
      ),
      onDidReceiveNotificationResponse: (NotificationResponse response) {
        final payload = response.payload;
        if (payload != null && payload.isNotEmpty) {
          _deepLinkHandler?.call(payload);
        }
      },
    );

    await _messaging.setForegroundNotificationPresentationOptions(
      alert: true,
      badge: true,
      sound: true,
    );

    FirebaseMessaging.onMessage.listen((message) {
      _showLocalNotification(message);
    });

    FirebaseMessaging.onMessageOpenedApp.listen((message) {
      _openTramiteFromMessage(message);
    });
  }

  /// Tras registrar [registerTramiteDeepLinkHandler], entrega el tap en notificación
  /// del sistema cuando la app arrancó desde estado **terminado** (cold start).
  static Future<void> deliverColdStartTramiteDeepLinkIfAny() async {
    final initial = await _messaging.getInitialMessage();
    if (initial != null) {
      _openTramiteFromMessage(initial);
    }
  }

  static Future<String?> getToken() async {
    return _messaging.getToken();
  }

  static Future<void> requestPermission() async {
    await _messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
      provisional: false,
    );
  }

  static void _showLocalNotification(RemoteMessage message) {
    final notification = message.notification;
    if (notification == null) return;

    final tid = message.data['tramiteId'];
    final payload = tid is String && tid.isNotEmpty ? tid : null;

    _localNotifications.show(
      notification.hashCode,
      notification.title,
      notification.body,
      const NotificationDetails(
        android: AndroidNotificationDetails(
          'tramites_channel',
          'Trámites',
          channelDescription: 'Notificaciones de trámites',
          importance: Importance.high,
          priority: Priority.high,
        ),
      ),
      payload: payload,
    );
  }
}
