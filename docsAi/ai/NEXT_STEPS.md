# NEXT_STEPS.md
# Próximos Pasos

## Prioridad ALTA (Etapa 1 — Dominio y Base de Datos)
- [ ] Definir el modelo de dominio (entidades: Trámite, Usuario, Política, etc.)
- [ ] Crear colecciones MongoDB con índices necesarios
- [ ] Implementar repositorios Spring Data MongoDB
- [ ] Crear los primeros endpoints CRUD de trámites

## Prioridad ALTA (Etapa 1 — Seguridad)
- [ ] Implementar Spring Security + JWT
- [ ] Crear entidad Usuario con roles (ADMIN, OPERADOR, CIUDADANO)
- [ ] Proteger endpoints con autorización basada en roles
- [ ] Almacenar sesiones/tokens en Redis

## Prioridad MEDIA (Etapa 2 — Frontend)
- [ ] Crear módulo de autenticación (login/logout)
- [ ] Crear módulo de gestión de trámites
- [ ] Implementar guards de rutas Angular
- [ ] Conectar Angular con endpoints reales del backend

## Prioridad MEDIA (Etapa 2 — FastAPI)
- [ ] Definir qué analítica necesita el sistema
- [ ] Conectar FastAPI a MongoDB para consultas analíticas
- [ ] Agregar primer endpoint de análisis de trámites

## Prioridad BAJA (Etapa 3 — Producción)
- [ ] Configurar Azure VM
- [ ] Instalar Docker en la VM
- [ ] Configurar dominio + HTTPS con Certbot
- [ ] Crear variables de entorno de producción seguras
- [ ] Agregar pipeline CI/CD básico (GitHub Actions)

## Deuda técnica conocida
- Angular: aún no tiene `package-lock.json` (se genera al hacer `npm install`)
- Tests: ningún test implementado todavía
- CORS: configuración permisiva en desarrollo, debe restringirse en prod
- Redis: sin autenticación en desarrollo, debe configurarse en prod
