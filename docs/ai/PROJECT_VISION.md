# PROJECT_VISION.md
# Vision del Proyecto - Plataforma de Gestion de Tramites

## Nombre del sistema
Plataforma de Gestion de Tramites basada en Politicas de Negocio

## Proposito
Sistema web full-stack que permite gestionar tramites institucionales
aplicando reglas y politicas de negocio configurables.
Incluye un microservicio especializado para analitica e integracion futura con IA.

## Vision a largo plazo
- Plataforma multiusuario con roles y permisos
- Motor de reglas de negocio para evaluacion automatica de tramites
- Analitica avanzada e integracion con modelos de IA/ML (FastAPI)
- Desplegable en Azure VM con dominio propio y HTTPS

## Alcance actual
El proyecto ya supero el bootstrapping inicial y tiene flujo de negocio activo en
backend, frontend y mobile. Se mantiene evolucion incremental por modulo.

## Stakeholders
- Usuario final: ciudadano o funcionario que gestiona tramites
- Administrador: configura politicas de negocio
- Planificador: asigna politicas a tramites ingresados
- Responsable de area: ejecuta y avanza tareas del flujo
- Agente IA: analisis y recomendaciones (futuro)

## Restricciones conocidas
- Despliegue inicial en Azure VM (sin Kubernetes por ahora)
- Testing automatizado aun incompleto
- Reglas de flujo avanzado (condiciones complejas y join multi-salto) pendientes
