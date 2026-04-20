# Frontend Angular Rules

## Regla principal

El frontend debe implementarse en Angular con estructura limpia y preparada para crecer.

## Principios

- Organizar por features o módulos funcionales.
- Mantener separación clara entre:
  - pages
  - components
  - services
  - models
  - guards si aplican
- Preparar un shell inicial simple antes de implementar pantallas completas.

## Alcance inicial

En fases tempranas:

- layout base
- rutas mínimas
- dashboard o shell vacío
- servicio HTTP base
- environment files
- integración futura con Spring Boot

## Prohibido

- Sobrecargar el frontend al inicio con pantallas de negocio complejas.
- Crear una UI compleja si todavía no están estables los contratos backend.
- Acoplar el frontend directamente al modelo interno de MongoDB.

## Recordatorio funcional

Más adelante el frontend deberá cubrir:

- administración
- políticas de negocio
- trámites
- formularios/documentos
- seguimiento/notificaciones
- analítica
