package com.plataforma.tramites.modules.tramites.dto;

import jakarta.validation.constraints.NotBlank;

public class TramiteAprobarRamaParalelaRequest {

    @NotBlank
    private String nodoRamaId;

    public String getNodoRamaId() {
        return nodoRamaId;
    }

    public void setNodoRamaId(String nodoRamaId) {
        this.nodoRamaId = nodoRamaId;
    }
}
