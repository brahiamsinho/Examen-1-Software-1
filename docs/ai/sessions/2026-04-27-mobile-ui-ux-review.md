## Session: Mobile UI/UX review

- Scope: Flutter mobile UI/UX analysis for login, home, mis tramites, profile, and theme.
- Changes: no code changes.
- Findings (Important):
  - AppBar contrast issue in `mobile/tramites_cliente/lib/features/home/home_screen.dart` (text color fixed white).
  - Missing session-expired handling (401) in `home_screen.dart` and `mis_tramites_screen.dart`.
- Findings (Minor):
  - Consider closing or reusing `ClienteApi` client lifecycle.
  - Format `fechaEntrada` in detalle for better UX.
