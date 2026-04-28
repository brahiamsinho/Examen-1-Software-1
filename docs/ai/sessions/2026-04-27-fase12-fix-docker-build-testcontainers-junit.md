# Sesión 2026-04-27 - Fase 12 (fix Docker build Testcontainers JUnit)

## Problema reportado

`docker compose up -d --build` fallaba en la imagen `backend` durante:

- `RUN mvn clean package -DskipTests -q`

Error principal:

- `package org.testcontainers.junit.jupiter does not exist`
- símbolos `@Testcontainers` y `@Container` no encontrados.

## Causa raíz

Aunque se usa `-DskipTests`, Maven igualmente ejecuta `testCompile`, por lo que las clases de test deben compilar.
Faltaba en `pom.xml` la dependencia que aporta anotaciones JUnit 5 para Testcontainers.

## Solución aplicada

- Archivo: `backend/pom.xml`
- Se agregó:
  - `org.testcontainers:junit-jupiter` (scope `test`)

## Verificación

- Comando:
  - `./mvnw -q clean package -DskipTests`
- Resultado:
  - verde (build backend exitoso).

## Impacto en experiencia de usuario

- **Visible en pantalla:** No directo.
- **Impacto usuario:** Alto indirecto.
- Beneficio:
  - desbloquea build/deploy local con Docker Compose y evita que el sistema quede caído por error de compilación.
