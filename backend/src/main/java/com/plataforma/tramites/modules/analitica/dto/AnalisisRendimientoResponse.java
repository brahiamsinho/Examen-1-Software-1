package com.plataforma.tramites.modules.analitica.dto;

import java.time.Instant;
import java.util.List;

public record AnalisisRendimientoResponse(
        String id,
        String politicaId,
        List<String> tramiteIds,
        Instant fechaAnalisis,
        double tiempoPromedio,
        String cuelloBotellaDetectado,
        String observacion) {}
