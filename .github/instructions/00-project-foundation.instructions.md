---
name: project-foundation
description: "Use when planning or implementing cross-stack changes in this project. Covers official stack, modular principles, and phased delivery."
applyTo:
  - backend/**
  - fastapi/**
  - frontend/**
  - mobile/**
  - docker/**
  - nginx/**
  - docker-compose.yml
  - README.md
  - docs/ai/**
---

# Project Foundation

## Objetivo del proyecto

Este proyecto implementa una plataforma web y movil para la gestion de tramites basada en politicas de negocio.

## Stack oficial

- Frontend web: Angular
- Backend principal: Spring Boot
- Microservicio IA: FastAPI
- Base de datos: MongoDB
- Cache/soporte: Redis
- Reverse proxy: Nginx
- Contenedorizacion: Docker + Docker Compose
- Despliegue futuro: Azure VM

## Principios obligatorios

- Mantener una base limpia, modular y escalable.
- No inventar tecnologias fuera del stack oficial salvo peticion explicita.
- No cambiar arquitectura sin justificarlo.
- No implementar logica de negocio final si el pedido es solo bootstrap o estructura.
- Respetar los modulos funcionales del proyecto.
- Priorizar claridad, mantenibilidad y consistencia.

## Modulos funcionales base

- seguridad
- politicas
- tramites
- documentos
- seguimiento
- analitica

## Forma de trabajo esperada

- Primero planificar, despues implementar.
- Antes de tocar codigo, explicar brevemente que archivos se crearan o modificaran.
- No hacer cambios masivos sin confirmar la direccion tecnica.
- Preferir entregas por fases y modulos.
