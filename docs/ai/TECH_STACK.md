# TECH_STACK.md
# Stack Tecnologico

## Frontend
| Tech | Version | Rol |
|---|---|---|
| Angular | 17 | SPA framework |
| TypeScript | 5.x | Lenguaje frontend |
| RxJS | 7.x | Reactividad |
| AntV X6 | 2.x | Modelado de grafo de politicas |
| Nginx | 1.25 | Servidor del build/proxy |

## Backend principal
| Tech | Version | Rol |
|---|---|---|
| Spring Boot | 3.2.x | Framework backend |
| Java | 21 LTS | Lenguaje |
| Spring Data MongoDB | incluido | Persistencia documental |
| Spring Security + JWT | incluido | AuthN/AuthZ |
| Spring Data Redis | incluido | Cache/infra |
| Maven | 3.9+ | Build |

## Servicio especializado
| Tech | Version | Rol |
|---|---|---|
| FastAPI | 0.111+ | API asincrona especializada |
| Python | 3.12 | Lenguaje |
| Pydantic | 2.x | Validacion |
| Uvicorn | 0.29+ | ASGI server |

## Infraestructura
| Tech | Version | Rol |
|---|---|---|
| MongoDB | 7.x | Base documental principal |
| Redis | 7.x | Infra de soporte |
| Docker | 24+ | Contenedorizacion |
| Docker Compose | v2 | Orquestacion local |

## Criterios de stack
- Sin hardcode de host/puerto en cliente: se usan prefijos proxied por Nginx
- Separacion de responsabilidades para escalar sin sobreingenieria
- Compatibilidad local + Docker + nube mediante variables de entorno
