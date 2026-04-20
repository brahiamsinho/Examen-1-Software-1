package com.plataforma.tramites.modules.seguridad.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login por portal: el {@code portalRol} debe coincidir con el {@code codigo} del rol del usuario.
 */
public class LoginRequest {

    @NotBlank
    private String correo;

    @NotBlank
    private String contrasena;

    /**
     * Uno de: ADMINISTRADOR, DISENADOR_POLITICAS, RESPONSABLE_AREA, CLIENTE
     */
    @NotBlank
    private String portalRol;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getPortalRol() {
        return portalRol;
    }

    public void setPortalRol(String portalRol) {
        this.portalRol = portalRol;
    }
}
