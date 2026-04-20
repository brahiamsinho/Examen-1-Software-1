# 2026-04-16 — Bootstrapping Inicial del Proyecto

## Sesión
Fecha: 2026-04-16
Agente: Antigravity
Conversación: a0eac172-f3d3-4baf-887e-f2b3fd58c2e4

## Objetivo de la sesión
Crear esqueleto técnico inicial del proyecto desde cero.
Stack: Angular + Spring Boot + FastAPI + MongoDB + Redis + Nginx + Docker.

## Qué se hizo
- Estructura completa de carpetas y archivos base
- 3 Dockerfiles multi-stage (Angular, Spring Boot, FastAPI)
- docker-compose.yml con 6 servicios orquestados
- Nginx como único punto de entrada (reverse proxy)
- Endpoints /health en Spring Boot y FastAPI
- Frontend Angular 17 standalone con dashboard mínimo visual
- Variables de entorno por servicio sin hardcoding
- Memoria del proyecto inicializada en docs/ai/

## Decisiones clave tomadas
- Angular 17 standalone (sin NgModule)
- Solo Nginx expone puerto al host
- URLs de API relativas en Angular (sin puertos hardcodeados)
- FastAPI como servicio especializado, no microservicio puro
- Dockerfiles multi-stage + non-root user en todos los servicios

## Resultado
Proyecto listo para `docker compose up --build`.
Sin lógica de negocio. Lienzo limpio y profesional.
