# Próximos pasos sugeridos

1. Mobile Flutter: tras login, persistir JWT de forma segura y rehidratar sesión al abrir la app (hoy el token vive solo en memoria).
2. Persistencia adicional del grafo (export BPMN/XML u optimización de sync incremental); el catálogo y el editor ya consumen REST de políticas y el modelado X6 colabora en vivo vía WebSocket.
3. Añadir pruebas unitarias del `ApiService`, del login por portal y, si aplica, del flujo de import/export BPMN.
4. Revisar CORS y JWT en despliegue Nginx + Spring (ya hay CORS dev y stateless JWT).
5. Completar artefactos PUDS en `docs/ai/` (visión, arquitectura, trazabilidad) si el curso lo exige.
