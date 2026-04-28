---
name: mongodb-modeling
description: Guía reutilizable para modelar persistencia en MongoDB dentro de este proyecto.
---

# MongoDB Modeling Skill

## Cuándo usar esta skill

Usar esta skill cuando se necesite:

- convertir un modelo conceptual UML a persistencia MongoDB
- decidir embebidos vs referencias
- proponer colecciones
- definir índices
- proponer validaciones
- preparar init-mongo.js
- conectar el diseño con Spring Data MongoDB

## Reglas del proyecto

- MongoDB no debe modelarse como si fuera PostgreSQL
- no traducir el UML 1 a 1 como tablas
- pensar según patrones de acceso y crecimiento de datos
- respetar la arquitectura modular del backend Spring Boot

## Colecciones base esperadas

- usuarios
- roles
- permisos
- areas
- politicas_negocio
- tramites
- recorridos_tramite
- formularios_tramite
- documentos_tramite
- notificaciones
- bitacora
- analisis_rendimiento
- recomendaciones_politica

## Criterios de modelado

### Embebidos

Usar embebidos cuando:

- el dato pertenece claramente al agregado principal
- se consulta siempre junto
- no crecerá sin límite

### Referencias

Usar referencias cuando:

- el dato crece mucho
- necesita historial largo
- se consulta de forma separada
- se comparte entre agregados

## Reglas específicas del dominio

- PoliticaNegocio puede contener nodos y conexiones
- Tramite debe soportar FIFO usando fechaRegistro o numeroTurno
- RecorridoTramite debe guardar entrada, salida, estado y observación
- Bitacora y Notificacion deben preservar historial
- Formularios y Documentos deben asociarse al trámite y, cuando aplique, al nodo

## Salida esperada

Cuando se use esta skill, entregar:

1. colecciones propuestas
2. embebidos vs referencias
3. índices
4. validaciones
5. ejemplo de init-mongo.js
6. recomendaciones para Spring Data MongoDB
