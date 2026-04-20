package com.plataforma.tramites.modules.seguridad.dto;

/**
 * DTO de lectura futura; no exponer entidades/documentos en controladores.
 */
public record UsuarioPublicoDto(String id, String correo, String nombres) {
}
