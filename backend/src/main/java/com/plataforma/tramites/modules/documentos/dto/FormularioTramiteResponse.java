package com.plataforma.tramites.modules.documentos.dto;

import java.time.Instant;

public record FormularioTramiteResponse(
        String id,
        String tramiteId,
        String nodoId,
        String titulo,
        String tipo,
        String contenido,
        Instant fechaRegistro) {}
