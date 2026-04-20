# Backend Modular Spring Boot

## Regla principal

El backend en Spring Boot debe usar arquitectura modular por feature, no una estructura global por capas.

## Estructura obligatoria

Usar esta base:

- shared
- modules/seguridad
- modules/politicas
- modules/tramites
- modules/documentos
- modules/seguimiento
- modules/analitica

## Dentro de cada módulo

Cada módulo debe contener solo lo que le corresponde. Según necesidad, usar:

- controller
- dto
- entity o document
- repository
- service
- mapper
- config
- exception

## Prohibido

- Crear carpetas globales tipo:
  - controllers/
  - services/
  - repositories/
  - entities/
    para toda la app.
- Mezclar lógica de un módulo dentro de otro sin justificación.
- Meter lógica de IA dentro del backend principal si corresponde al microservicio FastAPI.

## Estilo de implementación

- Pensar como apps de Django, pero en Spring Boot.
- Cada módulo debe sentirse autónomo y entendible.
- La clase principal del proyecto debe quedar arriba del árbol de paquetes.
- Mantener paquetes y nombres coherentes.

## Alcance inicial del backend

En fases tempranas:

- endpoint /health
- configuración base
- estructura de módulos
- integración futura con MongoDB, Redis y FastAPI
- sin lógica completa del negocio todavía
