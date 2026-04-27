# Próximos pasos sugeridos

1. Validar en navegador que el diseñador ya no emite `areaId=[object Object]` al abrir/editar/guardar/recargar una política (incluyendo nodos existentes y nuevos).
2. Añadir prueba de regresión frontend para normalización `ObjectId`→`string` en mapper/serializador del diseñador.
3. Backend: evaluación de condiciones en aristas (`condicion`) y avance secuencial guiado por API o tarea.
4. Backend: join en paralelo multi-salto (BFS) o nodo explícito tipo sincronización en el modelo de política.
5. Mobile Flutter: refinar **Mis trámites** (paginación prev/next, pull-to-refresh) si hace falta para demo.
6. Mantener `docs/ai/` como única fuente canónica y registrar nuevas sesiones solo en `docs/ai/sessions/`.
