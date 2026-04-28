import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class SecureStorage {
  SecureStorage()
      : _storage = const FlutterSecureStorage(
          aOptions: AndroidOptions(
            encryptedSharedPreferences: true,
          ),
          iOptions: IOSOptions(
            accessibility: KeychainAccessibility.first_unlock_this_device,
          ),
        );

  final FlutterSecureStorage _storage;

  static const _keyToken = 'access_token';
  static const _keyNombres = 'nombres';
  static const _keyApellidos = 'apellidos';
  static const _keyCorreo = 'correo';
  static const _keyRol = 'rol_codigo';

  Future<void> saveSession({
    required String token,
    required String nombres,
    String? apellidos,
    required String correo,
    required String rol,
  }) async {
    await _storage.write(key: _keyToken, value: token);
    await _storage.write(key: _keyNombres, value: nombres);
    await _storage.write(key: _keyApellidos, value: apellidos ?? '');
    await _storage.write(key: _keyCorreo, value: correo);
    await _storage.write(key: _keyRol, value: rol);
  }

  Future<String?> getToken() => _storage.read(key: _keyToken);

  Future<Map<String, String?>> getUserInfo() async {
    final values = await _storage.readAll();
    return {
      'nombres': values[_keyNombres],
      'apellidos': values[_keyApellidos],
      'correo': values[_keyCorreo],
      'rol': values[_keyRol],
    };
  }

  Future<void> clearSession() async {
    await _storage.deleteAll();
  }
}
