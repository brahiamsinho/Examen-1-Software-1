# Sesión: revisiones de política + carril/formulario BPMN

## Implementado

- Mongo `politicas_negocio_revisiones` + `PoliticaNegocioRevisionRepository`; registro en `crear`/`reemplazar`; borrado en cascada al eliminar política.
- Campos en nodo: `formularioExternoUrl`, `carrilBpmn` (backend + Angular mapper + inspector).
- `FlujoSalidasResponse` y UI responsable (enlace HTTPS).
- Documentación `OPEN_SOURCE_BPMN.md` con referencias a Operaton, CIB seven, Imixs, bpmn-js.

## No incluido (siguiente ola)

- UI catálogo para listar/compare revisiones.
- Motor BPMN 2.0 embebido ni import/export BPMN XML.
- Swimlanes gráficas en X6 (solo etiqueta textual).
