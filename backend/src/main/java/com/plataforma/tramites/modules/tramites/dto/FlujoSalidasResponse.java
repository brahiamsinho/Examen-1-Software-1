package com.plataforma.tramites.modules.tramites.dto;

import java.util.List;

/**
 * Salidas desde el nodo actual del trámite más metadatos del nodo (formulario externo, carril BPMN).
 */
public record FlujoSalidasResponse(
        List<SalidaFlujoDto> salidas,
        String nodoActualId,
        String nodoActualNombre,
        String nodoActualCarrilBpmn,
        String formularioExternoUrl) {}
