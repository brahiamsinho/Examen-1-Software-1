# Sesión: test integración `lockVersion` legacy en políticas

- **Qué**: `backend/src/test/java/.../PoliticasDominioServiceLegacyLockMongoIT.java`
- **Por qué**: asegurar el hotfix de `inicializarLockVersionLegacySiHaceFalta` (documento BSON sin `lockVersion` → `PUT`/`reemplazar` no falla, lock optimista pasa a 1)
- **Cómo**: `@DataMongoTest` + `@Import(PoliticasDominioService.class)` + Testcontainers Mongo 7, inserción raw con `MongoTemplate.getCollection("politicas_negocio").insertOne` sin clave `lockVersion`
- **Alcance**: solo backend; mobile sin cambios (otro agente)
