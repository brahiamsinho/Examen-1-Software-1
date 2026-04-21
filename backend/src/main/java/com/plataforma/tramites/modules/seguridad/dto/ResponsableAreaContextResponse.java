package com.plataforma.tramites.modules.seguridad.dto;

import java.util.List;

/**
 * Contexto operativo del portal responsable: área asignada y políticas donde participa el departamento.
 */
public record ResponsableAreaContextResponse(
        boolean tieneArea,
        String areaId,
        String areaNombre,
        String areaDescripcion,
        List<PoliticaAreaResumenDto> politicasEnLasQueParticipaElArea) {}
