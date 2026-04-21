package com.plataforma.tramites.modules.tramites.dto;

import jakarta.validation.constraints.NotBlank;

public class TramiteActualizarRequest {

    @NotBlank
    private String estado;

    private String nodoActualId;
    private String areaActualId;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNodoActualId() {
        return nodoActualId;
    }

    public void setNodoActualId(String nodoActualId) {
        this.nodoActualId = nodoActualId;
    }

    public String getAreaActualId() {
        return areaActualId;
    }

    public void setAreaActualId(String areaActualId) {
        this.areaActualId = areaActualId;
    }
}
