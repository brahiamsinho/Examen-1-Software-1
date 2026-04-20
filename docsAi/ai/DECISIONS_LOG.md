# DECISIONS_LOG.md
# Registro de Decisiones Técnicas

---

## [2026-04-16] Bootstrapping inicial

### DEC-001: Angular 17 con standalone components
**Decisión:** Usar Angular 17 standalone API (sin NgModule).
**Razón:** NgModule es legacy desde Angular 17. Standalone es el estándar actual, más simple y alineado con el roadmap de Angular.
**Consecuencia:** `app.config.ts` reemplaza `app.module.ts`. Todos los componentes son `standalone: true`.

---

### DEC-002: Spring Boot 3 + Java 21
**Decisión:** Java 21 LTS con Spring Boot 3.2.x.
**Razón:** Java 21 es LTS hasta 2029. Spring Boot 3 requiere Java 17+ y trae mejoras de performance y virtual threads.
**Consecuencia:** Imagen Docker `eclipse-temurin:21-jre-alpine` (~190MB vs ~500MB de JDK).

---

### DEC-003: Solo Nginx expone puerto al host
**Decisión:** El docker-compose solo expone el puerto 80 de Nginx al host.
**Razón:** Seguridad. Los servicios internos (Spring Boot, FastAPI, MongoDB, Redis) no deben ser accesibles desde el exterior directamente.
**Consecuencia:** Todo el tráfico pasa por Nginx como único punto de entrada. Fácil de agregar HTTPS después.

---

### DEC-004: Angular llama APIs a través de rutas relativas (/backend/, /fastapi/)
**Decisión:** El frontend no hardcodea puertos ni IPs. Usa rutas relativas que Nginx proxea.
**Razón:** El frontend puede desplegarse en cualquier dominio o IP sin cambiar el código. Nginx reescribe las rutas.
**Consecuencia:** `environment.ts` tiene `/backend` y `/fastapi` como base URLs.

---

### DEC-005: FastAPI como servicio especializado, no microservicio puro
**Decisión:** FastAPI NO es un microservicio autónomo en el sentido estricto. Es un servicio especializado Python dentro del mismo compose.
**Razón:** Microservicios puros con mensajería (Kafka, RabbitMQ) sería sobreingeniería en esta etapa.
**Consecuencia:** Comunicación síncrona HTTP entre Spring Boot y FastAPI cuando sea necesario.

---

### DEC-006: Dockerfiles multi-stage
**Decisión:** Todos los Dockerfiles usan multi-stage build.
**Razón:** Reduce el tamaño de la imagen final. El stage de build tiene todas las herramientas de compilación; el stage de runtime solo tiene el artefacto final.
**Consecuencia:** Imágenes más pequeñas, menos superficie de ataque, builds más limpios.

---

### DEC-007: Usuario non-root en contenedores
**Decisión:** Todos los Dockerfiles crean y usan un usuario no-root (`appuser`).
**Razón:** Buena práctica de seguridad. Contenedores no deben correr como root en producción.
