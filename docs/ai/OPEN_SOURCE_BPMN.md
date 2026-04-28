# Referencias open source — BPMN, motor de flujo y modelado

Este proyecto **no** integra aún un motor BPMN 2.0 completo; el flujo ejecutable sigue siendo el modelo propio (`politicas_negocio` + `TramiteFlujoService`). Las referencias siguientes sirven para **evaluar** evolución hacia estándar BPMN, swimlanes visuales o formularios externos.

## Motores / plataformas (JVM, embebibles)

| Proyecto | Licencia | Notas |
|----------|----------|--------|
| [Operaton](https://github.com/operaton/operaton) | Apache-2.0 | Motor BPMN 2.0 (continuidad open source de Camunda 7); integración Spring Boot, REST, Cockpit. Útil si en el futuro se externaliza la **ejecución** del flujo. |
| [CIB seven](https://github.com/cibseven/cibseven/) | Apache-2.0 | Plataforma BPMN derivada del ecosistema Camunda; similar casos de uso a Operaton. |
| [Imixs-Workflow](https://www.imixs.org/) | EPL-2.0 | BPMN 2.0 orientado a procesos humanos; Jakarta EE / microservicios. |

## Modelado visual en web

| Proyecto | Licencia | Notas |
|----------|----------|--------|
| [bpmn-js](https://github.com/bpmn-io/bpmn-js) (bpmn.io) | MIT | Editor/visor BPMN 2.0 en el navegador; encaja si se reemplaza o complementa AntV X6 con diagramas **estándar BPMN**. |

## Formularios externos (Google)

No hace falta librería propia: **Google Forms** expone URL `https://docs.google.com/forms/...`. En este repo el nodo de política almacena `formularioExternoUrl` (HTTPS); el responsable abre el enlace desde el panel de flujo.

## Decisión actual (2026-04)

- **Versionado**: snapshots en Mongo (`politicas_negocio_revisiones`) en cada POST/PUT de política, sin sustituir el `version` de negocio ni el `lockVersion` optimista.
- **BPMN frontend completo (actual)**: modelador `bpmn-js` activo en Angular; se guarda `bpmnXml` y se adapta a `nodos`/`conexiones` para compatibilidad de ejecución.
- **Motor BPMN completo**: valor en trazabilidad estándar y tooling; coste en migración de datos, formación y operación — evaluar cuando el dominio lo justifique.
