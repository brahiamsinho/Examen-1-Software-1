package com.plataforma.tramites.modules.seguimiento.dto;

import java.time.Instant;

public record BitacoraEventoResponse(
        String id,
        String usuarioId,
        String accion,
        String descripcion,
        Instant fechaHora,
        String entidadTipo,
        String entidadId,
        String tramiteId,
        String politicaId) {}
