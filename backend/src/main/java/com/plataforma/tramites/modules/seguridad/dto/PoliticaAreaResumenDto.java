package com.plataforma.tramites.modules.seguridad.dto;

/**
 * Política de negocio que incluye al menos un nodo asociado al área del responsable.
 */
public record PoliticaAreaResumenDto(String id, String nombre, int version, String estado) {}
