package com.plataforma.tramites.modules.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UsuarioAdminUpdateRequest {

    @Email private String correo;

    @Size(min = 4, max = 120) private String contrasena;

    @Size(max = 80) private String nombres;

    @Size(max = 80) private String apellidos;

    @Size(max = 40) private String telefono;

    private String rolCodigo;

    private Boolean estado;

    /** Hex de {@code areas._id}; vacío o null limpia la asignación. */
    private String areaId;

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

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRolCodigo() {
        return rolCodigo;
    }

    public void setRolCodigo(String rolCodigo) {
        this.rolCodigo = rolCodigo;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }
}
