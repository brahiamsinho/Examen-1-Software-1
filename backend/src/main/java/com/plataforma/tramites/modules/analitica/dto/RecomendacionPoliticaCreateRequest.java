package com.plataforma.tramites.modules.analitica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecomendacionPoliticaCreateRequest {

    @NotBlank
    private String politicaId;

    @NotBlank
    private String usuarioId;

    @NotBlank
    private String politicaSugerida;

    @NotNull
    private Double probabilidadExito;

    @NotNull
    private Double tiempoEstimado;

    private String observacion;

    public String getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(String politicaId) {
        this.politicaId = politicaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getPoliticaSugerida() {
        return politicaSugerida;
    }

    public void setPoliticaSugerida(String politicaSugerida) {
        this.politicaSugerida = politicaSugerida;
    }

    public Double getProbabilidadExito() {
        return probabilidadExito;
    }

    public void setProbabilidadExito(Double probabilidadExito) {
        this.probabilidadExito = probabilidadExito;
    }

    public Double getTiempoEstimado() {
        return tiempoEstimado;
    }

    public void setTiempoEstimado(Double tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
