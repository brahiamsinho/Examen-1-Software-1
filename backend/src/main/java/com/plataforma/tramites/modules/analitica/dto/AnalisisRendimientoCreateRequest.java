package com.plataforma.tramites.modules.analitica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AnalisisRendimientoCreateRequest {

    @NotBlank
    private String politicaId;

    private List<String> tramiteIds;

    @NotNull
    private Double tiempoPromedio;

    @NotBlank
    private String cuelloBotellaDetectado;

    private String observacion;

    public String getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(String politicaId) {
        this.politicaId = politicaId;
    }

    public List<String> getTramiteIds() {
        return tramiteIds;
    }

    public void setTramiteIds(List<String> tramiteIds) {
        this.tramiteIds = tramiteIds;
    }

    public Double getTiempoPromedio() {
        return tiempoPromedio;
    }

    public void setTiempoPromedio(Double tiempoPromedio) {
        this.tiempoPromedio = tiempoPromedio;
    }

    public String getCuelloBotellaDetectado() {
        return cuelloBotellaDetectado;
    }

    public void setCuelloBotellaDetectado(String cuelloBotellaDetectado) {
        this.cuelloBotellaDetectado = cuelloBotellaDetectado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
