# PROJECT_VISION.md
# Visión del Proyecto — Plataforma de Gestión de Trámites

## Nombre del sistema
Plataforma de Gestión de Trámites basada en Políticas de Negocio

## Propósito
Sistema web full-stack que permite gestionar trámites institucionales
aplicando reglas y políticas de negocio configurables.
Incluye un microservicio especializado para analítica e integración futura con IA.

## Visión a largo plazo
- Plataforma multiusuario con roles y permisos
- Motor de reglas de negocio para evaluación automática de trámites
- Analítica avanzada e integración con modelos de IA/ML (FastAPI)
- Desplegable en Azure VM con dominio propio y HTTPS

## Alcance actual (Etapa 0 - Bootstrapping)
Solo estructura base. Sin lógica de negocio real.
Todos los servicios levantados y conectados entre sí.

## Stakeholders
- Usuario final: ciudadano o funcionario que gestiona trámites
- Administrador: configura políticas de negocio
- Agente IA: analiza datos y asiste en evaluaciones (futuro)

## Restricciones conocidas
- Despliegue inicial en Azure VM (no Kubernetes por ahora)
- Sin autenticación implementada todavía
- Sin modelo de dominio definido todavía
