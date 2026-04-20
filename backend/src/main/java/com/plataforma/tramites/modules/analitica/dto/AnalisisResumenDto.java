package com.plataforma.tramites.modules.analitica.dto;

import java.time.Instant;

public record AnalisisResumenDto(
        String id, String politicaId, Instant fechaAnalisis, Double tiempoPromedio, String cuelloBotellaDetectado) {
}
