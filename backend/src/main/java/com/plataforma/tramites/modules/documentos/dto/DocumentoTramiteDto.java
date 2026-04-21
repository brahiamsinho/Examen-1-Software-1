package com.plataforma.tramites.modules.documentos.dto;

import java.time.Instant;

public record DocumentoTramiteDto(
        String id,
        String tramiteId,
        String nodoId,
        String nombreArchivo,
        String tipoArchivo,
        String rutaArchivo,
        String estado,
        Instant fechaCarga) {}
