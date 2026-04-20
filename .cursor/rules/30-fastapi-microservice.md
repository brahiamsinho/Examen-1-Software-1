# FastAPI Microservice Rules

## Rol del microservicio

FastAPI es un microservicio de apoyo para:

- analítica avanzada
- recomendaciones inteligentes
- predicción
- detección de cuellos de botella
- lógica IA

## Prohibido

- Convertir FastAPI en backend principal del sistema.
- Duplicar lógica central del negocio que debe vivir en Spring Boot.
- Acoplar FastAPI innecesariamente a detalles internos del frontend.

## Alcance inicial

En etapas tempranas:

- endpoint /health
- estructura limpia
- requirements.txt
- config base
- preparado para Docker
- preparado para consumo desde Spring Boot

## Más adelante

FastAPI podrá encargarse de:

- recomendación de políticas
- predicción de tiempos
- análisis histórico
- detección de patrones
