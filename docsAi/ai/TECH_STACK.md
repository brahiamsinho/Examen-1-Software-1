# TECH_STACK.md
# Stack Tecnológico

## Frontend
| Tech | Versión | Rol |
|---|---|---|
| Angular | 17 | SPA framework |
| TypeScript | 5.4 | Lenguaje |
| RxJS | 7.8 | Programación reactiva |
| Nginx (container) | 1.25 | Servidor del build |

## Backend Principal
| Tech | Versión | Rol |
|---|---|---|
| Spring Boot | 3.2.4 | Framework backend |
| Java | 21 (LTS) | Lenguaje |
| Spring Data MongoDB | incluido | Acceso a MongoDB |
| Spring Data Redis | incluido | Acceso a Redis |
| Spring Actuator | incluido | Health, métricas |
| Maven | 3.9 | Build tool |

## Microservicio
| Tech | Versión | Rol |
|---|---|---|
| FastAPI | 0.111 | Framework REST async |
| Python | 3.12 | Lenguaje |
| Pydantic | 2.7 | Validación de datos |
| pydantic-settings | 2.2 | Configuración por variables de entorno |
| Uvicorn | 0.29 | ASGI server |

## Infraestructura
| Tech | Versión | Rol |
|---|---|---|
| MongoDB | 7.0 | Base de datos documental |
| Redis | 7.2 | Caché / sesiones |
| Nginx | 1.25 | Reverse proxy principal |
| Docker | 24+ | Contenedorización |
| Docker Compose | v2 | Orquestación local |

## Despliegue futuro
| Tech | Rol |
|---|---|
| Azure VM | Host de producción |
| Certbot + Let's Encrypt | HTTPS |

## Decisiones de versión
- Java 21: LTS, virtual threads (Project Loom), mejoras modernas
- Python 3.12: mejor performance, type system mejorado
- Angular 17: standalone components, nueva sintaxis @if/@for, SSR mejorado
- MongoDB 7: transacciones mejoradas, mejor performance
