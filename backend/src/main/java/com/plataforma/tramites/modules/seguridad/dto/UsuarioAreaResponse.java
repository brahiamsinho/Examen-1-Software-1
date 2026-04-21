package com.plataforma.tramites.modules.seguridad.dto;

/**
 * Resumen de usuario perteneciente a un área ({@code usuarios.areaId} en script.db).
 * Expuesto a diseñadores de políticas para asignar responsables en nodos.
 */
public record UsuarioAreaResponse(String id, String correo, String nombres, String apellidos) {}
