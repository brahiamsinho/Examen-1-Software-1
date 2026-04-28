package com.plataforma.tramites.modules.politicas.dto;

import java.time.Instant;

/** Metadatos de una revisión guardada (listado paginado). */
public record PoliticaRevisionResumenResponse(long revision, Instant guardadoEn, String nombre, int version, String estado) {}
