Quiero que actúes como arquitecto de software senior, desarrollador full stack senior, ingeniero DevOps y mentor técnico.

Tu tarea es INICIAR un proyecto desde cero dejando una base limpia, profesional, modular y escalable, sin implementar todavía la lógica real del negocio.

STACK DEL PROYECTO
- Frontend: Angular
- Backend principal: Spring Boot
- Microservicio complementario: FastAPI
- Base de datos: MongoDB
- Cache / soporte: Redis
- Reverse proxy: Nginx
- Contenedorización: Docker + Docker Compose
- Despliegue futuro: Microsoft Azure Virtual Machine

CONTEXTO
El sistema crecerá después hacia una plataforma de gestión de trámites basada en políticas de negocio, con frontend web, backend principal, microservicio inteligente y despliegue en contenedores.  
Pero en esta etapa NO quiero implementar la lógica de negocio real.  
Solo quiero un lienzo limpio y bien estructurado para construir encima.

OBJETIVO DE ESTA ETAPA
Crear el esqueleto técnico del proyecto con todos los servicios base funcionando entre sí, listos para crecer, pero con implementación mínima.

NO QUIERO TODAVÍA
- lógica de negocio real
- entidades finales del dominio
- casos de uso implementados
- base de datos modelada
- colecciones MongoDB reales
- autenticación completa
- permisos finales
- vistas complejas
- machine learning real
- integración avanzada con Azure

SÍ QUIERO
- estructura base limpia
- contenedores funcionando
- servicios conectados
- endpoints health
- frontend mínimo visible
- backend mínimo funcional
- microservicio FastAPI mínimo funcional
- configuración ordenada y profesional
- arquitectura preparada para crecer

ENTREGABLES QUE DEBES GENERAR

1. ESTRUCTURA DEL PROYECTO
Genera una estructura de carpetas profesional y clara, por ejemplo:
- frontend
- backend
- fastapi
- nginx
- docker
- docs

Explica brevemente el propósito de cada carpeta.

2. FRONTEND ANGULAR
Genera un proyecto Angular mínimo con:
- estructura limpia
- layout base vacío o dashboard mínimo
- rutas mínimas
- servicio HTTP base preparado
- variables de entorno
- una vista simple que confirme que el frontend funciona
- sin lógica de negocio real

3. BACKEND SPRING BOOT
Genera un backend Spring Boot mínimo con:
- estructura limpia y modular
- capas organizadas
- endpoint /health o /status
- configuración base
- variables de entorno
- preparado para futura conexión con MongoDB y Redis
- sin modelo de negocio real todavía

4. MICROSERVICIO FASTAPI
Genera un microservicio FastAPI mínimo con:
- estructura limpia
- endpoint /health
- requirements.txt
- variables de entorno
- preparado para futura analítica e IA
- sin modelos de ML todavía

5. MONGODB
Deja preparado el contenedor MongoDB:
- sin colecciones reales
- sin datos de negocio
- con volumen persistente
- con variables de entorno básicas

6. REDIS
Deja preparado el contenedor Redis:
- mínimo y funcional
- sin lógica de cache todavía

7. NGINX
Genera configuración base de Nginx como reverse proxy para:
- frontend Angular
- backend Spring Boot
- microservicio FastAPI

Debe quedar simple, clara y fácil de modificar después.

8. DOCKER
Genera:
- Dockerfile para Angular
- Dockerfile para Spring Boot
- Dockerfile para FastAPI
- configuración necesaria de Nginx
- docker-compose.yml completo
- red interna entre servicios
- volúmenes donde corresponda
- nombres claros para contenedores
- configuración apta para correr localmente y luego migrar a Azure VM

9. VARIABLES DE ENTORNO
Deja preparados:
- archivos .env o ejemplos
- configuración separada por servicio
- puertos
- nombres de host
- credenciales mínimas de ejemplo
- placeholders claros

10. README INICIAL
Genera un README que incluya:
- descripción breve de la arquitectura
- puertos por servicio
- cómo levantar el proyecto
- cómo verificar que todo está funcionando
- comandos básicos

REGLAS DE IMPLEMENTACIÓN
- No inventes lógica de negocio todavía
- No implementes modelos finales todavía
- No agregues dependencias innecesarias
- No sobrecargues la solución
- Mantén nombres consistentes
- Usa buenas prácticas reales
- Piensa en arquitectura modular
- Piensa en mantenibilidad
- Piensa en despliegue futuro en Azure VM
- Todo debe quedar limpio, profesional y listo para crecer

FORMATO DE RESPUESTA
Quiero que me entregues:
1. árbol de carpetas
2. explicación breve de la arquitectura
3. contenido de archivos principales
4. Dockerfiles
5. docker-compose.yml
6. configuración de nginx
7. endpoints mínimos de prueba
8. README inicial
9. pasos exactos para arrancar el proyecto

Cuando tomes decisiones técnicas, justifícalas de forma breve.
Prioriza siempre una solución limpia, seria, escalable y fácil de mantener.