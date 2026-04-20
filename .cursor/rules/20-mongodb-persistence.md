# MongoDB Persistence Rules

## Regla principal

MongoDB no debe modelarse como si fuera PostgreSQL.

## Principios

- Pensar en colecciones y documentos, no en tablas.
- Definir qué datos deben ir embebidos y cuáles referenciados según acceso y crecimiento.
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
- RecorridoTramite debe permitir trazabilidad temporal por nodo/área.
- Tramite debe soportar lógica FIFO usando fechaRegistro o numeroTurno.
- Bitacora y notificaciones deben conservar historial.
- Formularios y documentos deben asociarse al trámite y, cuando aplique, al nodo/etapa.

## Migraciones

- En etapa inicial puede usarse un script init-mongo.js.
- Para evolución posterior, preferir un mecanismo versionado como Mongock.
- No asumir migraciones SQL clásicas.

## Backend

- Usar Spring Data MongoDB.
- Preferir repositories por módulo.
- Usar MongoTemplate cuando haga falta lógica más compleja de consulta o agregación.
