/// Base del API Spring **sin** barra final.
/// En Android emulator usá `http://10.0.2.2:8080`.
const String kApiBaseUrl = String.fromEnvironment(
  'API_BASE_URL',
  defaultValue: 'http://10.0.2.2:8080',
);
