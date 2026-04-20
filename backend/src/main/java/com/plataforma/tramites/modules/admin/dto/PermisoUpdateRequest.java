package com.plataforma.tramites.modules.admin.dto;

import jakarta.validation.constraints.Size;

public class PermisoUpdateRequest {

    @Size(max = 160) private String nombre;

    @Size(max = 400) private String descripcion;

    @Size(max = 64) private String modulo;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }
}
