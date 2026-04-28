# Sesión 2026-04-27 — Implementación IA/ML

## Resumen
Se implementó el pipeline completo de Inteligencia Artificial para la plataforma de trámites:
- 10 endpoints ML en FastAPI
- 3 endpoints proxy en Spring Boot
- 1 componente Angular (dashboard cuellos botella)
- Pipeline Google Colab (export/import modelo)

## Decisiones técnicas
1. Arquitectura desacoplada: FastAPI = IA especializada, Spring Boot = negocio principal
2. Predicción asistida (humano confirma), no autónoma
3. Graceful degradation: si FastAPI cae, planificador sigue funcionando
4. Modelos guardados en volumen Docker `ml-models` para persistencia
5. Google Colab como herramienta de entrenamiento externo (GPU gratis)

## Entregables
- 10 endpoints IA funcionales y testeados
- Documentación actualizada en docs/ai/
- Docker config completo (volumes, env vars, depends_on)

## Próximos pasos
- Rebuild final Docker con Dockerfile actualizado
- Probar flujo completo con datos reales
- Posible: integración OCR con Azure Document Intelligence
