package com.plataforma.tramites.modules.tramites.dto;

import java.time.Instant;

public record TramiteResponse(
        String id,
        String codigo,
        String asunto,
        String descripcion,
        Instant fechaRegistro,
        String prioridad,
        String estado,
        int numeroTurno,
        String politicaId,
        String clienteId,
        String nodoActualId,
        String areaActualId) {}
