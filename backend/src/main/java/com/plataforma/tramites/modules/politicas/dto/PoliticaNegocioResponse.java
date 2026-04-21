package com.plataforma.tramites.modules.politicas.dto;

import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;

import java.time.Instant;
import java.util.List;

public record PoliticaNegocioResponse(
        String id,
        String nombre,
        String descripcion,
        int version,
        String estado,
        Instant fechaCreacion,
        List<NodoPoliticaEmbeddable> nodos,
        List<ConexionFlujoEmbeddable> conexiones) {}
