package com.plataforma.tramites.modules.planificador.dto;

import jakarta.validation.constraints.NotBlank;

public class PlanificadorAsignarPoliticaRequest {

    @NotBlank
    private String politicaId;

    public String getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(String politicaId) {
        this.politicaId = politicaId;
    }
}
