---
name: mongodb-persistence
description: "Use when modeling MongoDB documents or implementing persistence in Spring Data MongoDB. Covers collection design, embedding vs references, and indexing."
applyTo:
  - backend/**/*.java
  - backend/**/*.yml
  - backend/**/*.yaml
  - script.db
  - docker/**/init-mongo.js
---

# MongoDB Persistence Rules

## Regla principal

MongoDB no debe modelarse como si fuera PostgreSQL.

## Principios

- Pensar en colecciones y documentos, no en tablas.
- Definir que datos deben ir embebidos y cuales referenciados segun acceso y crecimiento.
- Evitar traducir el UML 1 a 1 como tablas relacionales.

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

## Reglas de modelado

- PoliticaNegocio puede contener nodos y conexiones si eso simplifica lectura y acceso.
- RecorridoTramite debe permitir trazabilidad temporal por nodo/area.
- Tramite debe soportar logica FIFO usando fechaRegistro o numeroTurno.
- Bitacora y notificaciones deben conservar historial.
- Formularios y documentos deben asociarse al tramite y, cuando aplique, al nodo/etapa.

## Migraciones

- En etapa inicial puede usarse un script init-mongo.js.
- Para evolucion posterior, preferir un mecanismo versionado como Mongock.
- No asumir migraciones SQL clasicas.

## Backend

- Usar Spring Data MongoDB.
- Preferir repositories por modulo.
- Usar MongoTemplate cuando haga falta logica mas compleja de consulta o agregacion.
