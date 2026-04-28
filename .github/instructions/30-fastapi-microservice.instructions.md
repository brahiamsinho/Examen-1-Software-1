---
name: fastapi-microservice
description: "Use when working on FastAPI service code, API contracts, or Python microservice setup. Keeps FastAPI focused on analytics and IA support responsibilities."
applyTo:
  - fastapi/**/*.py
  - fastapi/requirements.txt
  - fastapi/Dockerfile
---

# FastAPI Microservice Rules

## Rol del microservicio

FastAPI es un microservicio de apoyo para:

- analitica avanzada
- recomendaciones inteligentes
- prediccion
- deteccion de cuellos de botella
- logica IA

## Prohibido

- Convertir FastAPI en backend principal del sistema.
- Duplicar logica central del negocio que debe vivir en Spring Boot.
- Acoplar FastAPI innecesariamente a detalles internos del frontend.

## Alcance inicial

En etapas tempranas:

- endpoint /health
- estructura limpia
- requirements.txt
- config base
- preparado para Docker
- preparado para consumo desde Spring Boot

## Mas adelante

FastAPI podra encargarse de:

- recomendacion de politicas
- prediccion de tiempos
- analisis historico
- deteccion de patrones
