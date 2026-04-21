package com.plataforma.tramites.modules.analitica.dto;

import java.time.Instant;

public record RecomendacionPoliticaResponse(
        String id,
        String politicaId,
        String usuarioId,
        Instant fechaGeneracion,
        String politicaSugerida,
        double probabilidadExito,
        double tiempoEstimado,
        String observacion) {}
