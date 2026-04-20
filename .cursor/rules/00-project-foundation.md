# Project Foundation

## Objetivo del proyecto

Este proyecto implementa una plataforma web y móvil para la gestión de trámites basada en políticas de negocio.

## Stack oficial

- Frontend web: Angular
- Backend principal: Spring Boot
- Microservicio IA: FastAPI
- Base de datos: MongoDB
- Cache/soporte: Redis
- Reverse proxy: Nginx
- Contenedorización: Docker + Docker Compose
- Despliegue futuro: Azure VM

## Principios obligatorios

- Mantener una base limpia, modular y escalable.
- No inventar tecnologías fuera del stack oficial salvo petición explícita.
- No cambiar arquitectura sin justificarlo.
- No implementar lógica de negocio final si el pedido es solo bootstrap o estructura.
- Respetar los módulos funcionales del proyecto.
- Priorizar claridad, mantenibilidad y consistencia.

## Módulos funcionales base

- seguridad
- politicas
- tramites
- documentos
- seguimiento
- analitica

## Forma de trabajo esperada

- Primero planificar, después implementar.
- Antes de tocar código, explicar brevemente qué archivos se crearán o modificarán.
- No hacer cambios masivos sin confirmar la dirección técnica.
- Preferir entregas por fases y módulos.
