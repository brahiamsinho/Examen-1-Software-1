---
name: frontend-angular
description: "Use when modifying Angular application structure, routes, components, services, or environments. Keeps frontend modular and aligned with backend contracts."
applyTo:
  - frontend/**/*.ts
  - frontend/**/*.html
  - frontend/**/*.scss
  - frontend/angular.json
  - frontend/package.json
  - frontend/proxy.conf.json
---

# Frontend Angular Rules

## Regla principal

El frontend debe implementarse en Angular con estructura limpia y preparada para crecer.

## Principios

- Organizar por features o modulos funcionales.
- Mantener separacion clara entre:
  - pages
  - components
  - services
  - models
  - guards si aplican
- Preparar un shell inicial simple antes de implementar pantallas completas.

## Alcance inicial

En fases tempranas:

- layout base
- rutas minimas
- dashboard o shell vacio
- servicio HTTP base
- environment files
- integracion futura con Spring Boot

## Prohibido

- Sobrecargar el frontend al inicio con pantallas de negocio complejas.
- Crear una UI compleja si todavia no estan estables los contratos backend.
- Acoplar el frontend directamente al modelo interno de MongoDB.

## Recordatorio funcional

Mas adelante el frontend debera cubrir:

- administracion
- politicas de negocio
- tramites
- formularios/documentos
- seguimiento/notificaciones
- analitica
