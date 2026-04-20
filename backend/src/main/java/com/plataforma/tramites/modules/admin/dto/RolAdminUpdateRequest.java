package com.plataforma.tramites.modules.admin.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class RolAdminUpdateRequest {

    @Size(max = 120) private String nombre;

    private List<String> permisoCodigos;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getPermisoCodigos() {
        return permisoCodigos;
    }

    public void setPermisoCodigos(List<String> permisoCodigos) {
        this.permisoCodigos = permisoCodigos;
    }
}
