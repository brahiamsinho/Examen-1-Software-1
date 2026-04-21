package com.plataforma.tramites.modules.tramites.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class RecorridoTramiteRequest {

    @NotBlank
    private String nodoId;

    private String areaId;
    private String usuarioId;

    @NotBlank
    private String estado;

    private String observacion;

    /** Si es null se usa Instant.now() al registrar entrada. */
    private Instant fechaEntrada;

    public String getNodoId() {
        return nodoId;
    }

    public void setNodoId(String nodoId) {
        this.nodoId = nodoId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Instant getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(Instant fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }
}
