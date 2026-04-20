package com.plataforma.tramites.modules.documentos.dto;

import java.time.Instant;

public record DocumentoTramiteDto(
        String id, String tramiteId, String nombreArchivo, String estado, Instant fechaCarga) {
}
