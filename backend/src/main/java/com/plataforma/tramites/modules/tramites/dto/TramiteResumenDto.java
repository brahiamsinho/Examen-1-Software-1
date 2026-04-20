package com.plataforma.tramites.modules.tramites.dto;

import java.time.Instant;

public record TramiteResumenDto(String id, String codigo, String asunto, String estado, Instant fechaRegistro) {
}
