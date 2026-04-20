package com.plataforma.tramites.shared.dto;

/**
 * Respuesta mínima de “módulo vivo” durante la fase de bootstrap (sin lógica de negocio).
 */
public record ModuleStatusResponse(String module, String phase) {
}
