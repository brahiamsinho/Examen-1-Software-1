# NEXT_STEPS.md
# Próximos Pasos

## Prioridad ALTA (Etapa 1 — Dominio y Base de Datos)
- [ ] Definir el modelo de dominio (entidades: Trámite, Usuario, Política, etc.)
- [ ] Crear colecciones MongoDB con índices necesarios
- [ ] Implementar repositorios Spring Data MongoDB
- [ ] Crear los primeros endpoints CRUD de trámites

## Prioridad ALTA (Etapa 1 — Seguridad)
- [x] Implementar base de Spring Security + JWT
- [x] Implementar semilla idempotente de actores demo (roles + usuarios por portal)
- [ ] Crear entidad Usuario completa con permisos/claims de negocio
- [ ] Proteger endpoints con autorización basada en roles
- [ ] Almacenar sesiones/tokens en Redis

## Prioridad MEDIA (Etapa 2 — Frontend)
- [x] Refresh visual base (acceso/login/dashboard/shell) con foco en accesibilidad
- [ ] Completar modulo de autenticacion con recuperacion de sesion, estados vacios y mensajes de error por codigo
- [ ] Crear módulo de gestión de trámites
- [x] Implementar guard base de rutas Angular
- [ ] Conectar Angular con endpoints reales del backend
- [ ] Agregar pruebas unitarias para formularios de login y tarjeta de estado de servicios

## Prioridad MEDIA (Etapa 2 — FastAPI)
- [x] Esqueleto modular: `/health`, settings, middleware de tiempos/request-id, dominios stub
- [ ] Definir contratos API (v1) entre Spring Boot y FastAPI (analítica, recomendaciones)
- [ ] Conectar FastAPI a MongoDB solo para lecturas analíticas (si aplica)
- [ ] Primer endpoint de análisis (datos agregados, sin ML) y pruebas de integración

## Prioridad BAJA (Etapa 3 — Producción)
- [ ] Configurar Azure VM
- [ ] Instalar Docker en la VM
- [ ] Configurar dominio + HTTPS con Certbot
- [ ] Crear variables de entorno de producción seguras
- [ ] Agregar pipeline CI/CD básico (GitHub Actions)

## Deuda técnica conocida
- Tests: ningún test implementado todavía
- CORS: configuración permisiva en desarrollo, debe restringirse en prod
- Redis: sin autenticación en desarrollo, debe configurarse en prod
