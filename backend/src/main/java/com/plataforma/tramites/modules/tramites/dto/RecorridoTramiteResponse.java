package com.plataforma.tramites.modules.tramites.dto;

import java.time.Instant;

public record RecorridoTramiteResponse(
        String id,
        String tramiteId,
        String nodoId,
        String areaId,
        String usuarioId,
        Instant fechaEntrada,
        Instant fechaSalida,
        String estado,
        String observacion) {}
