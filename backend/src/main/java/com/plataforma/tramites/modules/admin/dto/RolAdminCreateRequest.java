package com.plataforma.tramites.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RolAdminCreateRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "codigo debe ser MAYUSCULAS, numeros o guion bajo")
    @Size(max = 64)
    private String codigo;

    @NotBlank @Size(max = 120) private String nombre;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
