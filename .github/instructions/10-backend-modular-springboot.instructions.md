---
name: backend-modular-springboot
description: "Use when creating or modifying Spring Boot backend code. Enforces modular-by-feature package organization and backend scope boundaries."
applyTo:
  - backend/**/*.java
  - backend/**/*.yml
  - backend/**/*.yaml
  - backend/pom.xml
---

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

## Dentro de cada modulo

Cada modulo debe contener solo lo que le corresponde. Segun necesidad, usar:

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
- Mezclar logica de un modulo dentro de otro sin justificacion.
- Meter logica de IA dentro del backend principal si corresponde al microservicio FastAPI.

## Estilo de implementacion

- Pensar como apps de Django, pero en Spring Boot.
- Cada modulo debe sentirse autonomo y entendible.
- La clase principal del proyecto debe quedar arriba del arbol de paquetes.
- Mantener paquetes y nombres coherentes.

## Alcance inicial del backend

En fases tempranas:

- endpoint /health
- configuracion base
- estructura de modulos
- integracion futura con MongoDB, Redis y FastAPI
- sin logica completa del negocio todavia
