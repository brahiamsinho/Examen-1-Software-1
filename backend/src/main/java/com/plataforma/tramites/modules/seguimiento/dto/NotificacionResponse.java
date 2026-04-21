package com.plataforma.tramites.modules.seguimiento.dto;

import java.time.Instant;

public record NotificacionResponse(
        String id,
        String tramiteId,
        String usuarioId,
        String mensaje,
        String tipo,
        Instant fechaEnvio,
        boolean leida) {}
