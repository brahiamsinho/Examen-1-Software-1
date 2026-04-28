package com.plataforma.tramites.modules.planificador.dto;

import jakarta.validation.constraints.NotBlank;

public class RegistrarDecisionIaRequest {

    @NotBlank
    private String policyIdSugerida;

    private double scoreConfianza;

    private boolean aceptada;

    private String tramiteId;

    public String getPolicyIdSugerida() {
        return policyIdSugerida;
    }

    public void setPolicyIdSugerida(String policyIdSugerida) {
        this.policyIdSugerida = policyIdSugerida;
    }

    public double getScoreConfianza() {
        return scoreConfianza;
    }

    public void setScoreConfianza(double scoreConfianza) {
        this.scoreConfianza = scoreConfianza;
    }

    public boolean isAceptada() {
        return aceptada;
    }

    public void setAceptada(boolean aceptada) {
        this.aceptada = aceptada;
    }

    public String getTramiteId() {
        return tramiteId;
    }

    public void setTramiteId(String tramiteId) {
        this.tramiteId = tramiteId;
    }
}
