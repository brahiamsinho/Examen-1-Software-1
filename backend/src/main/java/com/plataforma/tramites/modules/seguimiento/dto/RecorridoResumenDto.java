package com.plataforma.tramites.modules.seguimiento.dto;

import java.time.Instant;

public record RecorridoResumenDto(
        String id, String tramiteId, String nodoId, String estado, Instant fechaEntrada) {
}
