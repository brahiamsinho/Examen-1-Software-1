package com.plataforma.tramites.modules.politicas.dto;

import java.time.Instant;

public record PoliticaResumenDto(String id, String nombre, int version, String estado, Instant fechaCreacion) {
}
